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

data class NearbyState(
    val count: Int = 0,
    val availability: NearbyAvailability = NearbyAvailability.Checking,
) {
    val needsPermission: Boolean get() = availability == NearbyAvailability.PermissionNeeded

    val needsBluetooth: Boolean get() = availability == NearbyAvailability.BluetoothOff

    fun statusLine(): String = when (availability) {
        NearbyAvailability.Ready -> if (count > 0) "$count nearby" else "searching"
        NearbyAvailability.BluetoothOff -> "needs Bluetooth"
        NearbyAvailability.PermissionNeeded -> "needs permission"
        NearbyAvailability.Unavailable -> "Nearby unavailable"
        NearbyAvailability.Checking -> "searching"
    }
}
