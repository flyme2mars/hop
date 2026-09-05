package com.flyme2mars.hop.data.nearby

import java.security.MessageDigest
import java.util.UUID

object HopBleIds {
    val SERVICE_UUID: UUID = UUID.fromString("8b1e4c5a-6f20-4d11-9c3a-7a21e0f10b01")
    val SYNC_UUID: UUID = UUID.fromString("8b1e4c5a-6f20-4d11-9c3a-7a21e0f10b02")

    fun presencePayload(floor: String, selfId: String): ByteArray {
        return floorToken(floor) + selfToken(selfId)
    }

    fun floorToken(floor: String): ByteArray = sha(floor.trim().lowercase(), 2)

    fun selfToken(selfId: String): ByteArray = sha(selfId, 8)

    fun sameFloor(payload: ByteArray?, floor: String): Boolean {
        if (payload == null || payload.size < 2) return false
        val expected = floorToken(floor)
        return payload[0] == expected[0] && payload[1] == expected[1]
    }

    fun isSelf(payload: ByteArray?, selfId: String): Boolean {
        if (payload == null || payload.size < 10) return false
        val expected = selfToken(selfId)
        return payload.copyOfRange(2, 10).contentEquals(expected)
    }

    fun peerId(payload: ByteArray?): String? {
        if (payload == null || payload.size < 10) return null
        return payload.copyOfRange(2, 10).joinToString("") { byte ->
            "%02x".format(byte.toInt() and 0xFF)
        }
    }

    private fun sha(value: String, length: Int): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray(Charsets.UTF_8))
        return digest.copyOf(length)
    }
}
