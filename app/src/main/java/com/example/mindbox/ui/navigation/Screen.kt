package com.example.mindbox.ui.navigation

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Home : Screen("home")
    object Input : Screen("input")
    object Search : Screen("search")
    object EntryDetail : Screen("entry/{entryId}") {
        fun createRoute(entryId: Long) = "entry/$entryId"
    }
    object EventDetail : Screen("event/{eventId}") {
        fun createRoute(eventId: Long) = "event/$eventId"
    }
    object People : Screen("people")
    object Organizations : Screen("organizations")
    object Settings : Screen("settings")
}
