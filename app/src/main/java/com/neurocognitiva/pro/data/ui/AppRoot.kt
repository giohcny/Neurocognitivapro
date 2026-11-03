package com.neurocognitiva.pro.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

object Routes {
    const val PATIENTS = "patients"
    const val PROFILE = "profile/{id}"
    const val CATALOG = "catalog"
    const val PROGRESS = "progress"
    const val REPORT = "report"
    const val SETTINGS = "settings"
    const val GAME = "game/{activityId}/{level}"

    fun profile(id: Long) = "profile/$id"
    fun game(activityId: String, level: Int) = "game/$activityId/$level"
}

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val TABS = listOf(
    Tab(Routes.PATIENTS, "Pacientes", Icons.Default.People),
    Tab(Routes.CATALOG, "Actividades", Icons.Default.SportsEsports),
    Tab(Routes.PROGRESS, "Evolución", Icons.Default.Timeline),
    Tab(Routes.REPORT, "Informe", Icons.Default.Description)
)

/** Navegación entre pestañas principales conservando el estado de cada una. */
fun NavController.goToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun AppRoot(vm: MainViewModel = viewModel()) {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        vm.events.collect { message ->
            snackbar.currentSnackbarData?.dismiss()
            snackbar.showSnackbar(message)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        // Los encabezados propios absorben la barra de estado; aquí solo se reserva la de navegación.
        contentWindowInsets = WindowInsets.navigationBars,
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (TABS.any { it.route == route }) {
                NavigationBar {
                    TABS.forEach { tab ->
                        NavigationBarItem(
                            selected = route == tab.route,
                            onClick = { nav.goToTab(tab.route) },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { inner ->
        NavHost(
            navController = nav,
            startDestination = Routes.CATALOG,
            modifier = Modifier.padding(inner)
        ) {
            composable(Routes.PATIENTS) { PatientsScreen(vm, nav) }
            composable(
                Routes.PROFILE,
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { entry ->
                ProfileScreen(vm, nav, entry.arguments?.getLong("id") ?: -1L)
            }
            composable(Routes.CATALOG) { CatalogScreen(vm, nav) }
            composable(Routes.PROGRESS) { ProgressScreen(vm, nav) }
            composable(Routes.REPORT) { ReportScreen(vm, nav) }
            composable(Routes.SETTINGS) { SettingsScreen(vm, nav) }
            composable(
                Routes.GAME,
                arguments = listOf(
                    navArgument("activityId") { type = NavType.StringType },
                    navArgument("level") { type = NavType.IntType }
                )
            ) { entry ->
                GameScreen(
                    vm = vm,
                    nav = nav,
                    activityId = entry.arguments?.getString("activityId") ?: "corsi_matrix",
                    startLevel = entry.arguments?.getInt("level") ?: 1
                )
            }
        }
    }
}
