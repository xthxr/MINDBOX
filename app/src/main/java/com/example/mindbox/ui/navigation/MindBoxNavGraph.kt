package com.example.mindbox.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.mindbox.ui.screen.auth.AuthScreen
import com.example.mindbox.ui.screen.home.HomeScreen
import com.example.mindbox.ui.screen.input.InputScreen
import com.example.mindbox.ui.screen.search.SearchScreen
import com.example.mindbox.ui.screen.detail.EntryDetailScreen
import com.example.mindbox.ui.screen.people.PeopleScreen
import com.example.mindbox.ui.screen.organizations.OrganizationsScreen
import com.example.mindbox.ui.screen.settings.SettingsScreen

@Composable
fun MindBoxNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Auth.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Auth.route) {
            AuthScreen(
                onAuthSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToInput = { navController.navigate(Screen.Input.route) },
                onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                onNavigateToEntry = { id -> navController.navigate(Screen.EntryDetail.createRoute(id)) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToPeople = { navController.navigate(Screen.People.route) },
                onNavigateToOrgs = { navController.navigate(Screen.Organizations.route) }
            )
        }

        composable(Screen.Input.route) {
            InputScreen(
                onNavigateBack = { navController.popBackStack() },
                onSaveComplete = { navController.popBackStack() }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                onNavigateToDetail = { id, type ->
                    navController.navigate(Screen.EntryDetail.createRoute(id))
                }
            )
        }

        composable(
            route = Screen.EntryDetail.route,
            arguments = listOf(
                navArgument("entryId") { type = NavType.LongType }
            )
        ) {
            EntryDetailScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.People.route) {
            PeopleScreen()
        }

        composable(Screen.Organizations.route) {
            OrganizationsScreen()
        }

        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}
