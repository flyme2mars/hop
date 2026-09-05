package com.flyme2mars.hop.data.nearby

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothStatusCodes
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import com.flyme2mars.hop.data.HopPost
import com.flyme2mars.hop.data.HopProfile
import com.flyme2mars.hop.data.HopSyncCodec
import com.flyme2mars.hop.data.NearbyAvailability
import com.flyme2mars.hop.data.NearbyPeer
import com.flyme2mars.hop.data.NearbyState
import java.util.ArrayDeque
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class HopNearbyController(
    private val context: Context,
    private val scope: CoroutineScope,
    private val floorProvider: () -> String,
    private val selfIdProvider: () -> String,
    private val profileProvider: () -> HopProfile,
    private val snapshotProvider: suspend () -> List<HopPost>,
    private val ingestRemote: suspend (List<HopPost>) -> Unit,
) {
    private val appContext = context.applicationContext
    private val tracker = PeerPresenceTracker()
    private val _state = MutableStateFlow(NearbyState())
    val state: StateFlow<NearbyState> = _state.asStateFlow()

    private val started = AtomicBoolean(false)
    private val connecting = AtomicBoolean(false)
    private val lastSyncAt = ConcurrentHashMap<String, Long>()
    private val devicesByPeer = ConcurrentHashMap<String, BluetoothDevice>()
    private val pending = ArrayDeque<BluetoothDevice>()

    @Volatile
    private var snapshotBytes: ByteArray = ByteArray(0)

    private var pruneJob: Job? = null
    private var snapshotJob: Job? = null
    private var resyncJob: Job? = null
    private var scanner: BluetoothLeScanner? = null
    private var advertiser: BluetoothLeAdvertiser? = null
    private var gattServer: BluetoothGattServer? = null
    private var activeGatt: BluetoothGatt? = null
    private var activePeerId: String? = null
    private var inboundAssembler = HopSyncFramer.Assembler()
    private var outboundChunks: List<ByteArray> = emptyList()
    private var outboundIndex = 0
    private var receiverRegistered = false
    private var lastPublishedPeers: List<NearbyPeer> = emptyList()
    private var lastPublishedAvailability: NearbyAvailability? = null
    private var lastPublishedSearching: Boolean? = null
    private var writeOwnAfterRead = false

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != BluetoothAdapter.ACTION_STATE_CHANGED) return
            publish(force = true)
            if (started.get() && canUseRadio()) {
                startRadioLocked()
            } else if (adapter()?.isEnabled != true) {
                stopRadioLocked()
                tracker.prune()
                publish(force = true)
            }
        }
    }

    fun start() {
        if (!started.compareAndSet(false, true)) {
            publish(force = true)
            if (canUseRadio()) startRadioLocked()
            return
        }
        registerReceiver()
        publish(force = true)
        pruneJob = scope.launch {
            while (isActive) {
                val changed = tracker.prune()
                if (changed) publish()
                delay(5_000)
            }
        }
        snapshotJob = scope.launch {
            while (isActive) {
                refreshSnapshot()
                delay(8_000)
            }
        }
        resyncJob = scope.launch {
            delay(3_000)
            while (isActive) {
                enqueueKnownPeers()
                drainQueue()
                delay(12_000)
            }
        }
        if (canUseRadio()) startRadioLocked()
        scope.launch { refreshSnapshot() }
    }

    fun stop() {
        started.set(false)
        pruneJob?.cancel()
        pruneJob = null
        snapshotJob?.cancel()
        snapshotJob = null
        resyncJob?.cancel()
        resyncJob = null
        stopRadioLocked()
        unregisterReceiver()
        pending.clear()
        tracker.clear()
        devicesByPeer.clear()
        lastSyncAt.clear()
        publish(force = true)
    }

    fun onPermissionsChanged() {
        publish(force = true)
        if (started.get() && canUseRadio()) {
            startRadioLocked()
        } else if (!NearbyPermissions.hasAll(appContext)) {
            stopRadioLocked()
            publish(force = true)
        }
    }

    fun notifyBoardChanged() {
        scope.launch { refreshSnapshot() }
    }

    fun onFloorChanged() {
        tracker.clear()
        devicesByPeer.clear()
        lastSyncAt.clear()
        if (started.get() && canUseRadio()) {
            startAdvertising(adapter() ?: return)
        }
        publish(force = true)
    }

    private suspend fun refreshSnapshot() {
        snapshotBytes = runCatching {
            HopSyncCodec.encode(
                posts = snapshotProvider(),
                selfId = selfIdProvider(),
                profile = profileProvider(),
            )
        }.getOrDefault(ByteArray(0))
    }

    private fun canUseRadio(): Boolean =
        started.get() &&
            NearbyPermissions.hasAll(appContext) &&
            adapter()?.isEnabled == true &&
            hasBleFeature()

    private fun currentAvailability(): NearbyAvailability {
        if (!hasBleFeature()) return NearbyAvailability.Unavailable
        if (!NearbyPermissions.hasAll(appContext)) return NearbyAvailability.PermissionNeeded
        val adapter = adapter() ?: return NearbyAvailability.Unavailable
        if (!adapter.isEnabled) return NearbyAvailability.BluetoothOff
        return NearbyAvailability.Ready
    }

    private fun publish(force: Boolean = false) {
        val availability = currentAvailability()
        val peers = if (availability == NearbyAvailability.Ready && started.get()) tracker.peers() else emptyList()
        val searching = availability == NearbyAvailability.Ready && started.get() && peers.isEmpty()
        if (
            !force &&
            peers == lastPublishedPeers &&
            availability == lastPublishedAvailability &&
            searching == lastPublishedSearching
        ) {
            return
        }
        lastPublishedPeers = peers
        lastPublishedAvailability = availability
        lastPublishedSearching = searching
        _state.value = NearbyState(peers = peers, availability = availability, searching = searching)
    }

    private fun hasBleFeature(): Boolean =
        appContext.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)

    private fun adapter(): BluetoothAdapter? {
        val manager = appContext.getSystemService(BluetoothManager::class.java)
        return manager?.adapter
    }

    @SuppressLint("MissingPermission")
    private fun startRadioLocked() {
        if (!NearbyPermissions.hasAll(appContext)) return
        val adapter = adapter() ?: return
        if (!adapter.isEnabled) return
        startGattServer()
        startAdvertising(adapter)
        startScanning(adapter)
    }

    @SuppressLint("MissingPermission")
    private fun stopRadioLocked() {
        runCatching { scanner?.stopScan(scanCallback) }
        scanner = null
        runCatching { advertiser?.stopAdvertising(advertiseCallback) }
        advertiser = null
        finishClient(activeGatt)
        runCatching { gattServer?.close() }
        gattServer = null
        connecting.set(false)
    }

    @SuppressLint("MissingPermission")
    private fun startAdvertising(adapter: BluetoothAdapter) {
        if (!adapter.isMultipleAdvertisementSupported) return
        val next = adapter.bluetoothLeAdvertiser ?: return
        runCatching { advertiser?.stopAdvertising(advertiseCallback) }
        advertiser = next
        val payload = HopBleIds.presencePayload(floorProvider(), selfIdProvider())
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setConnectable(true)
            .setTimeout(0)
            .build()
        // Legacy 31-byte advertise cannot hold a 128-bit UUID and a 12-byte
        // identity payload together. UUID stays in the primary packet; identity
        // goes in the scan response so receivers only mark a full valid payload.
        val advertiseData = AdvertiseData.Builder()
            .addServiceUuid(ParcelUuid(HopBleIds.SERVICE_UUID))
            .setIncludeDeviceName(false)
            .setIncludeTxPowerLevel(false)
            .build()
        val scanResponse = AdvertiseData.Builder()
            .addServiceData(ParcelUuid(HopBleIds.SERVICE_UUID), payload)
            .setIncludeDeviceName(false)
            .setIncludeTxPowerLevel(false)
            .build()
        runCatching { next.startAdvertising(settings, advertiseData, scanResponse, advertiseCallback) }
    }

    @SuppressLint("MissingPermission")
    private fun startScanning(adapter: BluetoothAdapter) {
        val next = adapter.bluetoothLeScanner ?: return
        runCatching { scanner?.stopScan(scanCallback) }
        scanner = next
        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(HopBleIds.SERVICE_UUID))
            .build()
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        runCatching { next.startScan(listOf(filter), settings, scanCallback) }
    }

    @SuppressLint("MissingPermission")
    private fun startGattServer() {
        if (gattServer != null) return
        val manager = appContext.getSystemService(BluetoothManager::class.java) ?: return
        val server = runCatching { manager.openGattServer(appContext, gattServerCallback) }.getOrNull() ?: return
        val service = BluetoothGattService(HopBleIds.SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        val characteristic = BluetoothGattCharacteristic(
            HopBleIds.SYNC_UUID,
            BluetoothGattCharacteristic.PROPERTY_READ or
                BluetoothGattCharacteristic.PROPERTY_WRITE or
                BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE or
                BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            BluetoothGattCharacteristic.PERMISSION_READ or BluetoothGattCharacteristic.PERMISSION_WRITE,
        )
        characteristic.addDescriptor(
            BluetoothGattDescriptor(
                CCCD_UUID,
                BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE,
            ),
        )
        @Suppress("DEPRECATION")
        characteristic.value = snapshotBytes
        service.addCharacteristic(characteristic)
        runCatching { server.addService(service) }
        gattServer = server
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartFailure(errorCode: Int) {
            publish(force = true)
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result ?: return
            val device = result.device ?: return
            val payload = result.scanRecord?.getServiceData(ParcelUuid(HopBleIds.SERVICE_UUID))
            val floor = floorProvider()
            val selfId = selfIdProvider()
            val evaluation = HopBleIds.evaluate(payload, floor, selfId)
            if (!evaluation.accepted) {
                Log.d(TAG, "drop ${device.address}: ${evaluation.reason}")
                return
            }
            val peerId = evaluation.peerId
            devicesByPeer[peerId] = device
            val added = tracker.mark(peerId)
            Log.d(
                TAG,
                if (added) {
                    "add peer $peerId from ${device.address}: ${evaluation.reason}"
                } else {
                    "refresh peer $peerId from ${device.address}"
                },
            )
            if (added) publish()
            enqueue(device, peerId)
            drainQueue()
        }

        override fun onScanFailed(errorCode: Int) {
            if (!hasBleFeature()) publish(force = true)
        }
    }

    private fun enqueue(device: BluetoothDevice, peerId: String) {
        val last = lastSyncAt[peerId] ?: 0L
        if (System.currentTimeMillis() - last < RESYNC_MS) return
        val address = device.address ?: return
        val already = pending.any { it.address == address } || activeGatt?.device?.address == address
        if (already) return
        pending.addLast(device)
    }

    private fun enqueueKnownPeers() {
        devicesByPeer.forEach { (peerId, device) ->
            enqueue(device, peerId)
        }
    }

    @SuppressLint("MissingPermission")
    private fun drainQueue() {
        if (!started.get() || !canUseRadio()) return
        if (!connecting.compareAndSet(false, true)) return
        val next = if (pending.isEmpty()) null else pending.removeFirst()
        if (next == null) {
            connecting.set(false)
            return
        }
        inboundAssembler.reset()
        outboundChunks = emptyList()
        outboundIndex = 0
        writeOwnAfterRead = false
        activePeerId = devicesByPeer.entries.firstOrNull { it.value.address == next.address }?.key
        activeGatt = runCatching {
            next.connectGatt(appContext, false, gattClientCallback, BluetoothDevice.TRANSPORT_LE)
        }.getOrNull()
        if (activeGatt == null) {
            connecting.set(false)
            drainQueue()
        } else {
            scope.launch {
                delay(12_000)
                if (activeGatt?.device?.address == next.address) {
                    finishClient(activeGatt)
                    drainQueue()
                }
            }
        }
    }

    private val gattClientCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                runCatching { gatt.requestMtu(517) }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                markSynced(gatt)
                val wasActive = activeGatt === gatt
                finishClient(gatt)
                if (wasActive) drainQueue()
            }
        }

        @SuppressLint("MissingPermission")
        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            runCatching { gatt.discoverServices() }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            val characteristic = syncCharacteristic(gatt)
            if (characteristic == null) {
                finishClient(gatt)
                return
            }
            enableNotify(gatt, characteristic)
            runCatching { gatt.readCharacteristic(characteristic) }
        }

        @Deprecated("Deprecated in Java")
        @Suppress("DEPRECATION")
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int,
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                handleRemoteBytes(characteristic.value)
            }
            requestRemoteDump(gatt, characteristic)
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int,
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                handleRemoteBytes(value)
            }
            requestRemoteDump(gatt, characteristic)
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
        ) {
            if (handleFramed(value)) {
                writeOwnSnapshot(gatt, characteristic)
            }
        }

        @Deprecated("Deprecated in Java")
        @Suppress("DEPRECATION")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
        ) {
            if (handleFramed(characteristic.value)) {
                writeOwnSnapshot(gatt, characteristic)
            }
        }

        @SuppressLint("MissingPermission")
        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int,
        ) {
            if (writeOwnAfterRead) {
                writeOwnAfterRead = false
                writeOwnSnapshot(gatt, characteristic)
                return
            }
            if (status == BluetoothGatt.GATT_SUCCESS && outboundIndex < outboundChunks.size) {
                writeNextChunk(gatt, characteristic)
            } else {
                markSynced(gatt)
                finishClient(gatt)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableNotify(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        gatt.setCharacteristicNotification(characteristic, true)
        val descriptor = characteristic.getDescriptor(CCCD_UUID) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
        } else {
            @Suppress("DEPRECATION")
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            @Suppress("DEPRECATION")
            gatt.writeDescriptor(descriptor)
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestRemoteDump(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        writeOwnAfterRead = true
        val requested = writeValue(gatt, characteristic, HopSyncFramer.request())
        if (!requested) writeOwnSnapshot(gatt, characteristic)
    }

    private fun handleFramed(bytes: ByteArray?): Boolean {
        val payload = bytes ?: return false
        val assembled = inboundAssembler.add(payload) ?: return false
        handleRemoteBytes(assembled)
        return true
    }

    @SuppressLint("MissingPermission")
    private fun writeOwnSnapshot(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        val body = snapshotBytes
        outboundChunks = HopSyncFramer.chunk(body, chunkSizeFor(gatt))
        outboundIndex = 0
        if (outboundChunks.isEmpty()) {
            markSynced(gatt)
            finishClient(gatt)
            return
        }
        writeNextChunk(gatt, characteristic)
    }

    @SuppressLint("MissingPermission")
    private fun writeNextChunk(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        if (outboundIndex >= outboundChunks.size) {
            markSynced(gatt)
            finishClient(gatt)
            return
        }
        val chunk = outboundChunks[outboundIndex]
        outboundIndex += 1
        val wrote = writeValue(gatt, characteristic, chunk)
        if (!wrote) {
            markSynced(gatt)
            finishClient(gatt)
        }
    }

    @SuppressLint("MissingPermission")
    private fun writeValue(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
    ): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            gatt.writeCharacteristic(
                characteristic,
                value,
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT,
            ) == BluetoothStatusCodes.SUCCESS
        } else {
            @Suppress("DEPRECATION")
            characteristic.value = value
            @Suppress("DEPRECATION")
            characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            @Suppress("DEPRECATION")
            gatt.writeCharacteristic(characteristic)
        }
    }

    private fun chunkSizeFor(gatt: BluetoothGatt): Int {
        val mtu = runCatching {
            val method = gatt.javaClass.methods.firstOrNull { it.name == "getMtu" && it.parameterCount == 0 }
            method?.invoke(gatt) as? Int
        }.getOrNull() ?: 23
        return (mtu - 3 - HopSyncFramer.HEADER_SIZE).coerceIn(20, 180)
    }

    private fun syncCharacteristic(gatt: BluetoothGatt): BluetoothGattCharacteristic? =
        gatt.getService(HopBleIds.SERVICE_UUID)?.getCharacteristic(HopBleIds.SYNC_UUID)

    private val gattServerCallback = object : BluetoothGattServerCallback() {
        private val inbound = ConcurrentHashMap<String, HopSyncFramer.Assembler>()

        @SuppressLint("MissingPermission")
        override fun onCharacteristicReadRequest(
            device: BluetoothDevice,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic,
        ) {
            val server = gattServer ?: return
            val body = snapshotBytes
            val slice = if (offset >= body.size) ByteArray(0) else body.copyOfRange(offset, body.size)
            runCatching {
                server.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, slice)
            }
        }

        @SuppressLint("MissingPermission")
        override fun onDescriptorWriteRequest(
            device: BluetoothDevice,
            requestId: Int,
            descriptor: BluetoothGattDescriptor,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?,
        ) {
            if (responseNeeded) {
                runCatching {
                    gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value)
                }
            }
        }

        @SuppressLint("MissingPermission")
        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?,
        ) {
            val payload = value ?: ByteArray(0)
            if (HopSyncFramer.isRequest(payload)) {
                notifyChunks(device, characteristic, snapshotBytes)
            } else {
                val assembler = inbound.getOrPut(device.address ?: device.toString()) {
                    HopSyncFramer.Assembler()
                }
                val assembled = assembler.add(payload)
                if (assembled != null) handleRemoteBytes(assembled)
            }
            if (responseNeeded) {
                runCatching {
                    gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun notifyChunks(
        device: BluetoothDevice,
        characteristic: BluetoothGattCharacteristic,
        body: ByteArray,
    ) {
        val server = gattServer ?: return
        val chunks = HopSyncFramer.chunk(body, HopSyncFramer.DEFAULT_CHUNK)
        chunks.forEach { chunk ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                server.notifyCharacteristicChanged(device, characteristic, false, chunk)
            } else {
                @Suppress("DEPRECATION")
                characteristic.value = chunk
                @Suppress("DEPRECATION")
                server.notifyCharacteristicChanged(device, characteristic, false)
            }
        }
    }

    private fun handleRemoteBytes(bytes: ByteArray?) {
        val payload = bytes ?: return
        val framed = inboundAssembler.add(payload)
        val body = framed ?: payload
        applyRemoteProfile(body)
        val posts = HopSyncCodec.decode(body)
        if (posts.isEmpty()) return
        scope.launch { runCatching { ingestRemote(posts) } }
    }

    private fun applyRemoteProfile(body: ByteArray) {
        val me = HopSyncCodec.decodeMe(body) ?: return
        if (me.id == selfIdProvider() || HopBleIds.peerIdFromSelfId(me.id) == HopBleIds.peerIdFromSelfId(selfIdProvider())) {
            Log.d(TAG, "drop ME profile: own selfId")
            return
        }
        val peerId = HopBleIds.peerIdFromSelfId(me.id)
        val changed = tracker.updateIdentity(peerId, me.name, me.room)
        Log.d(TAG, "identity $peerId name=${me.name} room=${me.room} changed=$changed")
        if (changed) publish()
    }

    private fun markSynced(gatt: BluetoothGatt?) {
        val address = gatt?.device?.address
        val peerId = activePeerId
            ?: devicesByPeer.entries.firstOrNull { it.value.address == address }?.key
            ?: address
        if (!peerId.isNullOrBlank()) {
            lastSyncAt[peerId] = System.currentTimeMillis()
        }
    }

    @SuppressLint("MissingPermission")
    private fun finishClient(gatt: BluetoothGatt?) {
        if (gatt == null) {
            connecting.set(false)
            activeGatt = null
            activePeerId = null
            return
        }
        runCatching { gatt.disconnect() }
        runCatching { gatt.close() }
        if (activeGatt === gatt) {
            activeGatt = null
            activePeerId = null
        }
        connecting.set(false)
        inboundAssembler.reset()
    }

    private fun registerReceiver() {
        if (receiverRegistered) return
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            appContext.registerReceiver(bluetoothReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            appContext.registerReceiver(bluetoothReceiver, filter)
        }
        receiverRegistered = true
    }

    private fun unregisterReceiver() {
        if (!receiverRegistered) return
        runCatching { appContext.unregisterReceiver(bluetoothReceiver) }
        receiverRegistered = false
    }

    private companion object {
        const val TAG = "HopNearby"
        const val RESYNC_MS = 15_000L
        val CCCD_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }
}
