package com.flyme2mars.hop.data

enum class PostKind {
    Offer,
    Ask,
    Note,
}

enum class PostFilter {
    All,
    Offer,
    Ask,
    Note,
}

enum class BlackoutStatus {
    None,
    Ok,
    Help,
}

enum class NearbyAvailability {
    Checking,
    Ready,
    BluetoothOff,
    PermissionNeeded,
    Unavailable,
}

data class HopProfile(
    val name: String = "",
    val room: String = "",
    val floor: String = "",
)

data class HopPost(
    val id: String,
    val kind: PostKind,
    val title: String,
    val body: String,
    val authorName: String,
    val authorRoom: String,
    val authorId: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long = createdAtMillis,
    val claimed: Boolean = false,
)

data class HopPrefs(
    val profile: HopProfile = HopProfile(),
    val onboarded: Boolean = false,
    val keepScreenOn: Boolean = true,
    val selfId: String = "",
    val seeded: Boolean = false,
)

data class BlackoutSession(
    val startedAtMillis: Long,
    val status: BlackoutStatus = BlackoutStatus.None,
)

data class NearbyPeer(
    val id: String,
    val name: String = "",
    val room: String = "",
) {
    fun label(): String {
        val named = name.trim()
        val roomLabel = room.trim()
        return when {
            named.isNotBlank() && roomLabel.isNotBlank() -> "$named · $roomLabel"
            named.isNotBlank() -> named
            roomLabel.isNotBlank() -> "Phone nearby · $roomLabel"
            else -> "Phone nearby · ${shortId()}"
        }
    }

    fun shortId(): String = id.take(6).ifBlank { "????" }
}

data class NearbyState(
    val peers: List<NearbyPeer> = emptyList(),
    val availability: NearbyAvailability = NearbyAvailability.Checking,
    val searching: Boolean = false,
) {
    val count: Int get() = peers.size

    val needsPermission: Boolean get() = availability == NearbyAvailability.PermissionNeeded

    val needsBluetooth: Boolean get() = availability == NearbyAvailability.BluetoothOff

    fun statusLine(): String = when (availability) {
        NearbyAvailability.Ready -> when {
            peers.isNotEmpty() -> "$count nearby"
            searching -> "searching"
            else -> "Nobody nearby"
        }
        NearbyAvailability.BluetoothOff -> "needs Bluetooth"
        NearbyAvailability.PermissionNeeded -> "needs permission"
        NearbyAvailability.Unavailable -> "Nearby unavailable"
        NearbyAvailability.Checking -> "searching"
    }
}
