package com.example.leveluplife.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.leveluplife.AppContainer
import com.example.leveluplife.ui.auth.LoginScreen
import com.example.leveluplife.ui.auth.LoginViewModel
import com.example.leveluplife.ui.home.HomeScreen
import com.example.leveluplife.ui.home.HomeViewModel
import com.example.leveluplife.ui.profile.ProfileScreen
import com.example.leveluplife.ui.profile.ProfileViewModel

object Routes {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val PROFILE = "profile"
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

    LaunchedEffect(container.sessionEvents) {
        container.sessionEvents.expired.collect {
            container.authRepository.logout()
            navController.navigate(Routes.LOGIN) {
                popUpTo(Routes.DASHBOARD) { inclusive = true }
                launchSingleTop = true
            }
        }
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
                profileCache = container.profileCache,
                onOpenProfile = {
                    navController.navigate(Routes.PROFILE) { launchSingleTop = true }
                },
            )
        }
        composable(Routes.PROFILE) {
            val vm: ProfileViewModel = viewModel(
                factory = ProfileViewModel.Factory(
                    container.profileRepository,
                    container.profileCache,
                    container.profileAvatarStorage,
                ),
            )
            ProfileScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onLogout = {
                    container.authRepository.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.DASHBOARD) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }
    }
}
