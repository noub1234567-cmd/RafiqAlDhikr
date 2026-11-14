package com.rafiqaldhikr.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rafiqaldhikr.app.di.AppContainer
import com.rafiqaldhikr.app.ui.AppViewModelFactory
import com.rafiqaldhikr.app.ui.components.ProfileSwitcherSheet
import com.rafiqaldhikr.app.ui.screens.adhkar.AdhkarScreen
import com.rafiqaldhikr.app.ui.screens.adhkar.AdhkarViewModel
import com.rafiqaldhikr.app.ui.screens.dashboard.DashboardScreen
import com.rafiqaldhikr.app.ui.screens.dashboard.DashboardViewModel
import com.rafiqaldhikr.app.ui.screens.dua.DuaScreen
import com.rafiqaldhikr.app.ui.screens.dua.DuaViewModel
import com.rafiqaldhikr.app.ui.screens.fasting.FastingScreen
import com.rafiqaldhikr.app.ui.screens.fasting.FastingViewModel
import com.rafiqaldhikr.app.ui.screens.history.HistoryScreen
import com.rafiqaldhikr.app.ui.screens.history.HistoryViewModel
import com.rafiqaldhikr.app.ui.screens.memorization.MemorizationScreen
import com.rafiqaldhikr.app.ui.screens.memorization.MemorizationViewModel
import com.rafiqaldhikr.app.ui.screens.more.MoreScreen
import com.rafiqaldhikr.app.ui.screens.profiles.ProfilesScreen
import com.rafiqaldhikr.app.ui.screens.profiles.ProfilesViewModel
import com.rafiqaldhikr.app.ui.screens.quran.QuranScreen
import com.rafiqaldhikr.app.ui.screens.quran.QuranViewModel
import com.rafiqaldhikr.app.ui.screens.seasons.SeasonsScreen
import com.rafiqaldhikr.app.ui.screens.seasons.SeasonsViewModel
import com.rafiqaldhikr.app.ui.screens.settings.SettingsScreen
import com.rafiqaldhikr.app.ui.screens.settings.SettingsViewModel
import com.rafiqaldhikr.app.ui.screens.statistics.StatisticsScreen
import com.rafiqaldhikr.app.ui.screens.statistics.StatisticsViewModel
import com.rafiqaldhikr.app.ui.screens.tasbeeh.TasbeehScreen
import com.rafiqaldhikr.app.ui.screens.tasbeeh.TasbeehViewModel
import com.rafiqaldhikr.app.ui.screens.worship.WorshipActsScreen
import com.rafiqaldhikr.app.ui.screens.worship.WorshipActsViewModel
import kotlinx.coroutines.launch

private data class BottomTab(
    val navRoute: NavRoute,
    val icon: ImageVector
)

private val bottomTabs = listOf(
    BottomTab(NavRoute.Dashboard, Icons.Default.Home),
    BottomTab(NavRoute.Adhkar, Icons.Default.AutoAwesome),
    BottomTab(NavRoute.Quran, Icons.Default.MenuBook),
    BottomTab(NavRoute.Fasting, Icons.Default.CalendarMonth),
    BottomTab(NavRoute.More, Icons.Default.Apps)
)

@Composable
fun RafiqNavHost(container: AppContainer) {
    val navController = rememberNavController()
    val factory = remember(container) { AppViewModelFactory(container) }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val profiles by container.profileRepository.profilesFlow.collectAsStateWithLifecycle(emptyList())
    val activeProfile by container.profileRepository.activeProfileFlow.collectAsStateWithLifecycle(null)
    var showProfileSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val showBottomBar = currentRoute in bottomTabs.map { it.navRoute.route }

    val navigateToRoute: (String) -> Unit = { route ->
        if (route in bottomTabs.map { it.navRoute.route }) {
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        } else {
            navController.navigate(route) { launchSingleTop = true }
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomTabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.navRoute.route,
                            onClick = { navigateToRoute(tab.navRoute.route) },
                            icon = { Icon(tab.icon, contentDescription = tab.navRoute.labelAr) },
                            label = { Text(tab.navRoute.labelAr) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).consumeWindowInsets(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = NavRoute.Dashboard.route
            ) {
                composable(NavRoute.Dashboard.route) {
                    val vm: DashboardViewModel = viewModel(factory = factory)
                    DashboardScreen(
                        viewModel = vm,
                        onOpenProfileSwitcher = { showProfileSheet = true },
                        onNavigate = navigateToRoute
                    )
                }
                composable(NavRoute.Adhkar.route) {
                    val vm: AdhkarViewModel = viewModel(factory = factory)
                    AdhkarScreen(
                        viewModel = vm,
                        onOpenProfileSwitcher = { showProfileSheet = true }
                    )
                }
                composable(NavRoute.Quran.route) {
                    val vm: QuranViewModel = viewModel(factory = factory)
                    QuranScreen(
                        viewModel = vm,
                        onOpenProfileSwitcher = { showProfileSheet = true },
                        onOpenMemorization = { navigateToRoute(NavRoute.Memorization.route) }
                    )
                }
                composable(NavRoute.Fasting.route) {
                    val vm: FastingViewModel = viewModel(factory = factory)
                    FastingScreen(
                        viewModel = vm,
                        onOpenProfileSwitcher = { showProfileSheet = true }
                    )
                }
                composable(NavRoute.More.route) {
                    MoreScreen(
                        activeProfile = activeProfile,
                        onOpenProfileSwitcher = { showProfileSheet = true },
                        onNavigate = navigateToRoute
                    )
                }
                composable(NavRoute.Memorization.route) {
                    val vm: MemorizationViewModel = viewModel(factory = factory)
                    MemorizationScreen(viewModel = vm, onBack = { navController.popBackStack() })
                }
                composable(NavRoute.Tasbeeh.route) {
                    val vm: TasbeehViewModel = viewModel(factory = factory)
                    TasbeehScreen(
                        viewModel = vm,
                        onBack = { navController.popBackStack() },
                        onOpenProfileSwitcher = { showProfileSheet = true }
                    )
                }
                composable(NavRoute.Dua.route) {
                    val vm: DuaViewModel = viewModel(factory = factory)
                    DuaScreen(viewModel = vm, onBack = { navController.popBackStack() })
                }
                composable(NavRoute.WorshipActs.route) {
                    val vm: WorshipActsViewModel = viewModel(factory = factory)
                    WorshipActsScreen(viewModel = vm, onBack = { navController.popBackStack() })
                }
                composable(NavRoute.Seasons.route) {
                    val vm: SeasonsViewModel = viewModel(factory = factory)
                    SeasonsScreen(viewModel = vm, onBack = { navController.popBackStack() })
                }
                composable(NavRoute.History.route) {
                    val vm: HistoryViewModel = viewModel(factory = factory)
                    HistoryScreen(viewModel = vm, onBack = { navController.popBackStack() })
                }
                composable(NavRoute.Statistics.route) {
                    val vm: StatisticsViewModel = viewModel(factory = factory)
                    StatisticsScreen(viewModel = vm, onBack = { navController.popBackStack() })
                }
                composable(NavRoute.Profiles.route) {
                    val vm: ProfilesViewModel = viewModel(factory = factory)
                    ProfilesScreen(viewModel = vm, onBack = { navController.popBackStack() })
                }
                composable(NavRoute.Settings.route) {
                    val vm: SettingsViewModel = viewModel(factory = factory)
                    SettingsScreen(viewModel = vm, onBack = { navController.popBackStack() })
                }
            }
        }
    }

    if (showProfileSheet) {
        ProfileSwitcherSheet(
            profiles = profiles,
            activeProfileId = activeProfile?.id ?: -1L,
            onSelectProfile = { id ->
                scope.launch { container.profileRepository.switchProfile(id) }
            },
            onManageProfiles = { navigateToRoute(NavRoute.Profiles.route) },
            onDismiss = { showProfileSheet = false }
        )
    }
}
