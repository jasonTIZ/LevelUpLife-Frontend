package com.example.leveluplife.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.leveluplife.R
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
import com.example.leveluplife.ui.settings.SettingsScreen
import com.example.leveluplife.ui.settings.SettingsViewModel

object Routes {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"

    const val ARG_DEACTIVATION_MESSAGE = "deactivation_message"
    const val ARG_SESSION_EXPIRED_MESSAGE = "session_expired_message"
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

    val sessionExpiredMessage = stringResource(R.string.login_session_expired_redirect)

    LaunchedEffect(container.sessionEvents, sessionExpiredMessage) {
        container.sessionEvents.expired.collect {
            container.authRepository.logout()
            navController.navigate(Routes.LOGIN) {
                popUpTo(Routes.DASHBOARD) { inclusive = true }
                launchSingleTop = true
            }
            runCatching {
                navController.getBackStackEntry(Routes.LOGIN)
                    .savedStateHandle
                    .set(Routes.ARG_SESSION_EXPIRED_MESSAGE, sessionExpiredMessage)
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable(Routes.LOGIN) { backStackEntry ->
            val infoMessage = backStackEntry.savedStateHandle
                .get<String>(Routes.ARG_DEACTIVATION_MESSAGE)
                ?: backStackEntry.savedStateHandle
                    .get<String>(Routes.ARG_SESSION_EXPIRED_MESSAGE)
            val vm: LoginViewModel = viewModel(
                factory = LoginViewModel.Factory(container.authRepository),
            )
            LoginScreen(
                viewModel = vm,
                themeController = container.themeController,
                infoMessage = infoMessage,
                onInfoMessageShown = {
                    backStackEntry.savedStateHandle.remove<String>(Routes.ARG_DEACTIVATION_MESSAGE)
                    backStackEntry.savedStateHandle.remove<String>(Routes.ARG_SESSION_EXPIRED_MESSAGE)
                },
                onLoggedIn = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToRegister = {},
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
                onOpenSettings = {
                    navController.navigate(Routes.SETTINGS) { launchSingleTop = true }
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
        composable(Routes.SETTINGS) {
            val vm: SettingsViewModel = viewModel(
                factory = SettingsViewModel.Factory(container.playerRepository),
            )
            SettingsScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onAccountDeactivated = { message ->
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.DASHBOARD) { inclusive = true }
                        launchSingleTop = true
                    }
                    runCatching {
                        navController.getBackStackEntry(Routes.LOGIN)
                            .savedStateHandle
                            .set(Routes.ARG_DEACTIVATION_MESSAGE, message)
                    }
                },
            )
        }
    }
}
