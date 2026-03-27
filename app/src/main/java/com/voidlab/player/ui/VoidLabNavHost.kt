package com.voidlab.player.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.voidlab.player.ui.screens.*
import com.voidlab.player.ui.viewmodels.EQViewModel
import com.voidlab.player.ui.viewmodels.LibraryViewModel
import com.voidlab.player.ui.viewmodels.PlayerViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object NowPlaying : Screen("now_playing", "NOW PLAYING", Icons.Default.PlayCircle)
    object Library : Screen("library", "LIBRARY", Icons.Default.LibraryMusic)
    object Equalizer : Screen("equalizer", "EQUALIZER", Icons.Default.Tune)
    object Visualizer : Screen("visualizer", "VISUALIZER", Icons.Default.Waves)
    object Settings : Screen("settings", "SETTINGS", Icons.Default.Settings)
}

@Composable
fun VoidLabNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    // CRITICAL: Create PlayerViewModel ONCE at top level
    // All screens share the SAME instance
    val playerViewModel: PlayerViewModel = hiltViewModel()
    
    val items = listOf(
        Screen.NowPlaying,
        Screen.Library,
        Screen.Equalizer,
        Screen.Visualizer,
        Screen.Settings
    )
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.NowPlaying.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.NowPlaying.route) {
                NowPlayingScreen(viewModel = playerViewModel)
            }
            
            composable(Screen.Library.route) {
                val libraryViewModel: LibraryViewModel = hiltViewModel()
                LibraryScreen(
                    libraryViewModel = libraryViewModel,
                    playerViewModel = playerViewModel
                )
            }
            
            composable(Screen.Equalizer.route) {
                val eqViewModel: EQViewModel = hiltViewModel()
                EqualizerScreen(viewModel = eqViewModel)
            }
            
            composable(Screen.Visualizer.route) {
                VisualizerScreen(viewModel = playerViewModel)
            }
            
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}
