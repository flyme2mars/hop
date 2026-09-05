package com.flyme2mars.hop.data.nearby

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
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
import com.flyme2mars.hop.data.HopPost
import com.flyme2mars.hop.data.HopSyncCodec
import com.flyme2mars.hop.data.NearbyAvailability
import com.flyme2mars.hop.data.NearbyState
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
    private val snapshotProvider: suspend () -> List<HopPost>,
    private val ingestRemote: suspend (List<HopPost>) -> Unit,
) {
    private val appContext = context.applicationContext
    private val tracker = PeerPresenceTracker()
    private val _state = MutableStateFlow(NearbyState())
    val state: StateFlow<NearbyState> = _state.asStateFlow()

    private val started = AtomicBoolean(false)
    private val connecting = AtomicBoolean(false)
    private val lastConnectAt = ConcurrentHashMap<String, Long>()

    @Volatile
    private var snapshotBytes: ByteArray = ByteArray(0)

    private var pruneJob: Job? = null
    private var snapshotJob: Job? = null
    private var scanner: BluetoothLeScanner? = null
    private var advertiser: BluetoothLeAdvertiser? = null
    private var gattServer: BluetoothGattServer? = null
    private var activeGatt: BluetoothGatt? = null
    private var receiverRegistered = false

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != BluetoothAdapter.ACTION_STATE_CHANGED) return
            publishAvailability()
            if (started.get() && adapter()?.isEnabled == true && NearbyPermissions.hasAll(appContext)) {
                startRadioLocked()
            } else if (adapter()?.isEnabled != true) {
                stopRadioLocked()
                tracker.prune()
                publishCount()
            }
        }
    }

    fun start() {
        if (!started.compareAndSet(false, true)) {
            publishAvailability()
            if (canUseRadio()) startRadioLocked()
            return
        }
        registerReceiver()
        publishAvailability()
        pruneJob = scope.launch {
            while (isActive) {
                tracker.prune()
                publishCount()
                delay(2_000)
            }
        }
        snapshotJob = scope.launch {
            while (isActive) {
                refreshSnapshot()
                delay(5_000)
            }
        }
        if (canUseRadio()) startRadioLocked()
    }

    fun stop() {
        started.set(false)
        pruneJob?.cancel()
        pruneJob = null
        snapshotJob?.cancel()
        snapshotJob = null
        stopRadioLocked()
        unregisterReceiver()
        _state.value = NearbyState(count = 0, availability = currentAvailability())
    }

    fun onPermissionsChanged() {
        publishAvailability()
        if (started.get() && canUseRadio()) {
            startRadioLocked()
        } else if (!NearbyPermissions.hasAll(appContext)) {
            stopRadioLocked()
            _state.value = NearbyState(count = 0, availability = NearbyAvailability.PermissionNeeded)
        }
    }

    fun notifyBoardChanged() {
        scope.launch { refreshSnapshot() }
    }

    private suspend fun refreshSnapshot() {
        val encoded = runCatching { HopSyncCodec.encode(snapshotProvider()) }.getOrDefault(ByteArray(0))
        snapshotBytes = encoded
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

    private fun publishAvailability() {
        val availability = currentAvailability()
        val count = if (availability == NearbyAvailability.Ready) tracker.count() else 0
        _state.value = NearbyState(count = count, availability = availability)
    }

    private fun publishCount() {
        val availability = currentAvailability()
        _state.value = NearbyState(
            count = if (availability == NearbyAvailability.Ready) tracker.count() else 0,
            availability = availability,
        )
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
        startGattServer(adapter)
        startAdvertising(adapter)
        startScanning(adapter)
    }

    @SuppressLint("MissingPermission")
    private fun stopRadioLocked() {
        runCatching { scanner?.stopScan(scanCallback) }
        scanner = null
        runCatching { advertiser?.stopAdvertising(advertiseCallback) }
        advertiser = null
        runCatching { activeGatt?.close() }
        activeGatt = null
        val manager = appContext.getSystemService(BluetoothManager::class.java)
        runCatching { gattServer?.close() }
        gattServer = null
    }

    @SuppressLint("MissingPermission")
    private fun startAdvertising(adapter: BluetoothAdapter) {
        if (!adapter.isMultipleAdvertisementSupported) return
        val next = adapter.bluetoothLeAdvertiser ?: return
        advertiser = next
        val payload = HopBleIds.presencePayload(floorProvider(), selfIdProvider())
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
            .setConnectable(true)
            .setTimeout(0)
            .build()
        val data = AdvertiseData.Builder()
            .addServiceUuid(ParcelUuid(HopBleIds.SERVICE_UUID))
            .addServiceData(ParcelUuid(HopBleIds.SERVICE_UUID), payload)
            .setIncludeDeviceName(false)
            .setIncludeTxPowerLevel(false)
            .build()
        runCatching { next.startAdvertising(settings, data, advertiseCallback) }
    }

    @SuppressLint("MissingPermission")
    private fun startScanning(adapter: BluetoothAdapter) {
        val next = adapter.bluetoothLeScanner ?: return
        scanner = next
        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(HopBleIds.SERVICE_UUID))
            .build()
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
            .build()
        runCatching { next.startScan(listOf(filter), settings, scanCallback) }
    }

    @SuppressLint("MissingPermission")
    private fun startGattServer(adapter: BluetoothAdapter) {
        if (gattServer != null) return
        val manager = appContext.getSystemService(BluetoothManager::class.java) ?: return
        val server = runCatching { manager.openGattServer(appContext, gattServerCallback) }.getOrNull() ?: return
        val service = BluetoothGattService(HopBleIds.SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        val characteristic = BluetoothGattCharacteristic(
            HopBleIds.SYNC_UUID,
            BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_READ or BluetoothGattCharacteristic.PERMISSION_WRITE,
        )
        characteristic.value = snapshotBytes
        service.addCharacteristic(characteristic)
        runCatching { server.addService(service) }
        gattServer = server
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartFailure(errorCode: Int) {
            // Scan-only presence still counts other advertisers.
            publishAvailability()
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result ?: return
            val payload = result.scanRecord?.getServiceData(ParcelUuid(HopBleIds.SERVICE_UUID))
            val floor = floorProvider()
            val selfId = selfIdProvider()
            if (!HopBleIds.sameFloor(payload, floor)) return
            if (HopBleIds.isSelf(payload, selfId)) return
            val peerId = result.device?.address.orEmpty()
            if (peerId.isBlank()) return
            tracker.mark(peerId)
            publishCount()
            maybeConnect(result.device)
        }

        override fun onScanFailed(errorCode: Int) {
            if (!hasBleFeature()) {
                _state.value = NearbyState(count = 0, availability = NearbyAvailability.Unavailable)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun maybeConnect(device: BluetoothDevice?) {
        device ?: return
        if (!NearbyPermissions.hasAll(appContext)) return
        val address = device.address ?: return
        val now = System.currentTimeMillis()
        val last = lastConnectAt[address] ?: 0L
        if (now - last < CONNECT_COOLDOWN_MS) return
        if (!connecting.compareAndSet(false, true)) return
        lastConnectAt[address] = now
        activeGatt = runCatching {
            device.connectGatt(appContext, false, gattClientCallback, BluetoothDevice.TRANSPORT_LE)
        }.getOrNull()
        if (activeGatt == null) connecting.set(false)
    }

    private val gattClientCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                runCatching { gatt.discoverServices() }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                finishClient(gatt)
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            val characteristic = gatt.getService(HopBleIds.SERVICE_UUID)
                ?.getCharacteristic(HopBleIds.SYNC_UUID)
            if (characteristic == null) {
                finishClient(gatt)
                return
            }
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
            writeOwnSnapshot(gatt, characteristic)
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
            writeOwnSnapshot(gatt, characteristic)
        }

        @SuppressLint("MissingPermission")
        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int,
        ) {
            finishClient(gatt)
        }
    }

    @SuppressLint("MissingPermission")
    private fun writeOwnSnapshot(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        val payload = snapshotBytes
        val wrote = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            gatt.writeCharacteristic(
                characteristic,
                payload,
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT,
            ) == BluetoothStatusCodes.SUCCESS
        } else {
            @Suppress("DEPRECATION")
            characteristic.value = payload
            @Suppress("DEPRECATION")
            gatt.writeCharacteristic(characteristic)
        }
        if (!wrote) finishClient(gatt)
    }

    private val gattServerCallback = object : BluetoothGattServerCallback() {
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
        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?,
        ) {
            if (value != null) handleRemoteBytes(value)
            if (responseNeeded) {
                runCatching {
                    gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value)
                }
            }
        }
    }

    private fun handleRemoteBytes(bytes: ByteArray?) {
        val payload = bytes ?: return
        val posts = HopSyncCodec.decode(payload)
        if (posts.isEmpty()) return
        scope.launch { runCatching { ingestRemote(posts) } }
    }

    @SuppressLint("MissingPermission")
    private fun finishClient(gatt: BluetoothGatt) {
        runCatching { gatt.disconnect() }
        runCatching { gatt.close() }
        if (activeGatt === gatt) activeGatt = null
        connecting.set(false)
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
        const val CONNECT_COOLDOWN_MS = 45_000L
    }
}
