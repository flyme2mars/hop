package com.flyme2mars.hop.data.nearby

object HopSyncFramer {
    const val HEADER_SIZE = 8
    const val DEFAULT_CHUNK = 180
    const val REQUEST: Byte = 0x52
    private const val MAGIC0: Byte = 0x48
    private const val MAGIC1: Byte = 0x32

    fun request(): ByteArray = byteArrayOf(REQUEST)

    fun isRequest(bytes: ByteArray): Boolean = bytes.size == 1 && bytes[0] == REQUEST

    fun chunk(payload: ByteArray, maxChunkPayload: Int = DEFAULT_CHUNK): List<ByteArray> {
        val size = maxChunkPayload.coerceAtLeast(1)
        if (payload.isEmpty()) return listOf(frame(total = 1, index = 0, payload = ByteArray(0)))
        val total = (payload.size + size - 1) / size
        return (0 until total).map { index ->
            val start = index * size
            val end = minOf(start + size, payload.size)
            frame(total = total, index = index, payload = payload.copyOfRange(start, end))
        }
    }

    fun frame(total: Int, index: Int, payload: ByteArray): ByteArray {
        val out = ByteArray(HEADER_SIZE + payload.size)
        out[0] = MAGIC0
        out[1] = MAGIC1
        writeU16(out, 2, total)
        writeU16(out, 4, index)
        writeU16(out, 6, payload.size)
        if (payload.isNotEmpty()) {
            payload.copyInto(out, HEADER_SIZE)
        }
        return out
    }

    fun parse(bytes: ByteArray): Frame? {
        if (bytes.size < HEADER_SIZE) return null
        if (bytes[0] != MAGIC0 || bytes[1] != MAGIC1) return null
        val total = readU16(bytes, 2)
        val index = readU16(bytes, 4)
        val length = readU16(bytes, 6)
        if (total <= 0 || index < 0 || index >= total) return null
        if (bytes.size < HEADER_SIZE + length) return null
        return Frame(
            total = total,
            index = index,
            payload = bytes.copyOfRange(HEADER_SIZE, HEADER_SIZE + length),
        )
    }

    class Assembler {
        private val parts = sortedMapOf<Int, ByteArray>()
        private var expected = -1

        fun add(bytes: ByteArray): ByteArray? {
            val frame = parse(bytes) ?: return null
            if (expected < 0) {
                expected = frame.total
            } else if (frame.total != expected) {
                reset()
                expected = frame.total
            }
            parts[frame.index] = frame.payload
            if (expected > 0 && parts.size == expected) {
                val assembled = parts.values.fold(ByteArray(0)) { acc, chunk -> acc + chunk }
                reset()
                return assembled
            }
            return null
        }

        fun reset() {
            parts.clear()
            expected = -1
        }
    }

    data class Frame(
        val total: Int,
        val index: Int,
        val payload: ByteArray,
    )

    private fun writeU16(target: ByteArray, offset: Int, value: Int) {
        target[offset] = ((value ushr 8) and 0xFF).toByte()
        target[offset + 1] = (value and 0xFF).toByte()
    }

    private fun readU16(source: ByteArray, offset: Int): Int {
        val high = source[offset].toInt() and 0xFF
        val low = source[offset + 1].toInt() and 0xFF
        return (high shl 8) or low
    }
}
