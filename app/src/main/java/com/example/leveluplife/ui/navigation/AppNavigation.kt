package com.example.leveluplife.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.leveluplife.AppContainer
import com.example.leveluplife.ui.auth.LoginScreen
import com.example.leveluplife.ui.auth.LoginViewModel
import com.example.leveluplife.ui.dashboard.DashboardScreen
import com.example.leveluplife.ui.disciplines.DisciplineDetailScreen
import com.example.leveluplife.ui.disciplines.DisciplineDetailViewModel

object Routes {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val DISCIPLINE_DETAIL = "discipline/{disciplineId}"

    fun disciplineDetail(id: String) = "discipline/$id"
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
            DashboardScreen(
                container = container,
                onLoggedOut = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.DASHBOARD) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(
            route = Routes.DISCIPLINE_DETAIL,
            arguments = listOf(navArgument("disciplineId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val disciplineId = backStackEntry.arguments?.getString("disciplineId").orEmpty()
            val vm: DisciplineDetailViewModel = viewModel(
                factory = DisciplineDetailViewModel.Factory(
                    disciplineId = disciplineId,
                    repository = container.disciplineRepository,
                    tokenStore = container.tokenStore,
                ),
            )
            DisciplineDetailScreen(
                viewModel = vm,
                onNavigateBack = { navController.popBackStack() },
                onCreateHabit = { /* Habit creation out of scope — wire when screen is ready */ },
            )
        }
    }
}
