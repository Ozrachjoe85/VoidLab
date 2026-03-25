package com.voidlab.player.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.voidlab.player.ui.screens.*

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object NowPlaying : Screen("now_playing", "NOW PLAYING", Icons.Default.MusicNote)
    object Library : Screen("library", "LIBRARY", Icons.Default.LibraryMusic)
    object Equalizer : Screen("equalizer", "EQUALIZER", Icons.Default.Equalizer)
    object Visualizer : Screen("visualizer", "VISUALIZER", Icons.Default.GraphicEq)
    object Settings : Screen("settings", "SETTINGS", Icons.Default.Settings)
}

@Composable
fun VoidLabNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    val items = listOf(
        Screen.NowPlaying,
        Screen.Library,
        Screen.Equalizer,
