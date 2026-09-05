package com.flyme2mars.hop.data.nearby

import java.security.MessageDigest
import java.util.UUID

enum class NearbyVerdict {
    Accept,
    DropMissingPayload,
    DropShortPayload,
    DropWrongFloor,
    DropSelf,
    DropBlankId,
}

data class NearbyEvaluation(
    val verdict: NearbyVerdict,
    val peerId: String = "",
    val reason: String,
) {
    val accepted: Boolean get() = verdict == NearbyVerdict.Accept
}

object HopBleIds {
    val SERVICE_UUID: UUID = UUID.fromString("8b1e4c5a-6f20-4d11-9c3a-7a21e0f10b01")
    val SYNC_UUID: UUID = UUID.fromString("8b1e4c5a-6f20-4d11-9c3a-7a21e0f10b02")

    const val FLOOR_TOKEN_BYTES = 4
    const val SELF_TOKEN_BYTES = 8
    const val PAYLOAD_SIZE = FLOOR_TOKEN_BYTES + SELF_TOKEN_BYTES

    fun presencePayload(floor: String, selfId: String): ByteArray {
        return floorToken(floor) + selfToken(selfId)
    }

    fun floorToken(floor: String): ByteArray = sha(floor.trim().lowercase(), FLOOR_TOKEN_BYTES)

    fun selfToken(selfId: String): ByteArray = sha(selfId, SELF_TOKEN_BYTES)

    fun evaluate(payload: ByteArray?, floor: String, selfId: String): NearbyEvaluation {
        if (payload == null) {
            return NearbyEvaluation(NearbyVerdict.DropMissingPayload, reason = "no service data")
        }
        if (payload.size != PAYLOAD_SIZE) {
            return NearbyEvaluation(
                NearbyVerdict.DropShortPayload,
                reason = "payload size ${payload.size} != $PAYLOAD_SIZE",
            )
        }
        if (!sameFloor(payload, floor)) {
            return NearbyEvaluation(NearbyVerdict.DropWrongFloor, reason = "floor token mismatch")
        }
        if (isSelf(payload, selfId)) {
            return NearbyEvaluation(NearbyVerdict.DropSelf, reason = "own advertise")
        }
        val id = peerId(payload).orEmpty()
        if (id.isBlank()) {
            return NearbyEvaluation(NearbyVerdict.DropBlankId, reason = "blank peer id")
        }
        return NearbyEvaluation(NearbyVerdict.Accept, peerId = id, reason = "accepted $id")
    }

    fun sameFloor(payload: ByteArray?, floor: String): Boolean {
        if (payload == null || payload.size < FLOOR_TOKEN_BYTES) return false
        return payload.copyOfRange(0, FLOOR_TOKEN_BYTES).contentEquals(floorToken(floor))
    }

    fun isSelf(payload: ByteArray?, selfId: String): Boolean {
        if (payload == null || payload.size != PAYLOAD_SIZE) return false
        return payload.copyOfRange(FLOOR_TOKEN_BYTES, PAYLOAD_SIZE).contentEquals(selfToken(selfId))
    }

    fun peerId(payload: ByteArray?): String? {
        if (payload == null || payload.size != PAYLOAD_SIZE) return null
        return tokenHex(payload.copyOfRange(FLOOR_TOKEN_BYTES, PAYLOAD_SIZE))
    }

    fun peerIdFromSelfId(selfId: String): String = tokenHex(selfToken(selfId))

    private fun tokenHex(bytes: ByteArray): String =
        bytes.joinToString("") { byte -> "%02x".format(byte.toInt() and 0xFF) }

    private fun sha(value: String, length: Int): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray(Charsets.UTF_8))
        return digest.copyOf(length)
    }
}
