package com.example.leveluplife.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.leveluplife.AppContainer
import com.example.leveluplife.ui.auth.LoginScreen
import com.example.leveluplife.ui.auth.LoginViewModel
import com.example.leveluplife.ui.categories.CategoriesScreen
import com.example.leveluplife.ui.categories.CategoriesViewModel
import com.example.leveluplife.ui.home.HomeScreen
import com.example.leveluplife.ui.home.HomeViewModel

object Routes {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val CATEGORIES = "categories"
}

@Composable
fun AppNavigation(
    container: AppContainer,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val startDestination = if (container.authRepository.isLoggedIn()) {
        Routes.DASHBOARD
    } else {
        Routes.LOGIN
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable(Routes.LOGIN) {
            val vm: LoginViewModel = viewModel(
                factory = LoginViewModel.Factory(container.authRepository),
            )
            LoginScreen(
                viewModel = vm,
                themeController = container.themeController,
                onLoggedIn = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToRegister = {
                    // Registro fuera de alcance de esta tarea.
                },
            )
        }
        composable(Routes.DASHBOARD) {
            val vm: HomeViewModel = viewModel(
                factory = HomeViewModel.Factory(container.habitRepository),
            )
            HomeScreen(
                viewModel = vm,
                onLoggedOut = {
                    container.authRepository.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.DASHBOARD) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onOpenCategories = {
                    navController.navigate(Routes.CATEGORIES) {
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(Routes.CATEGORIES) {
            val vm: CategoriesViewModel = viewModel(
                factory = CategoriesViewModel.Factory(container.habitCategoryRepository),
            )
            CategoriesScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onCategoryClick = {
                    // Navegación al detalle de categoría: fuera de alcance de esta tarea.
                },
            )
        }
    }
}
