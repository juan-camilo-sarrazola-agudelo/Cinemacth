package com.example.cinemacth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cinemacth.ui.screens.*
import com.example.cinemacth.ui.theme.CinemacthTheme
import com.example.cinemacth.ui.viewmodel.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CinemacthTheme {
                MainApp()
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Cine", Icons.Default.Home)
    object Search : Screen("search", "Buscar", Icons.Default.Search)
    object Users : Screen("users", "Usuarios", Icons.Default.AccountCircle)
    object Favorites : Screen("favorites", "Lista", Icons.Default.Favorite)
    object Detail : Screen("detail/{movieId}", "Detalle", Icons.Default.Home) {
        fun createRoute(movieId: Int) = "detail/$movieId"
    }
}

@Composable
fun MainApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val items = listOf(
        Screen.Home,
        Screen.Search,
        Screen.Users,
        Screen.Favorites
    )

    Scaffold(
        bottomBar = {
            if (items.any { it.route == currentDestination?.route }) {
                NavigationBar {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
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
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                val viewModel: MovieViewModel = hiltViewModel()
                HomeScreen(viewModel) { movieId ->
                    navController.navigate(Screen.Detail.createRoute(movieId))
                }
            }
            composable(Screen.Search.route) {
                val viewModel: MovieViewModel = hiltViewModel()
                SearchScreen(viewModel) { movieId ->
                    navController.navigate(Screen.Detail.createRoute(movieId))
                }
            }
            composable(Screen.Users.route) {
                val viewModel: ApiTesterViewModel = hiltViewModel()
                ApiTesterScreen(viewModel)
            }
            composable(Screen.Favorites.route) {
                val viewModel: FavoritesViewModel = hiltViewModel()
                FavoritesScreen(viewModel) { movieId ->
                    navController.navigate(Screen.Detail.createRoute(movieId))
                }
            }
            composable(
                route = Screen.Detail.route,
                arguments = listOf(navArgument("movieId") { type = NavType.IntType })
            ) { backStackEntry ->
                val movieId = backStackEntry.arguments?.getInt("movieId") ?: 0
                val viewModel: MovieDetailViewModel = hiltViewModel()
                MovieDetailScreen(
                    movieId = movieId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
