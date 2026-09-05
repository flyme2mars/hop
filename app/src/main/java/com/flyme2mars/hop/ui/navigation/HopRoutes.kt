package com.flyme2mars.hop.ui.navigation

sealed interface HopRoute

data object LaunchRoute : HopRoute

data object HomeRoute : HopRoute

data object BlackoutRoute : HopRoute

enum class HomeTab {
    Floor,
    History,
    Settings,
}
