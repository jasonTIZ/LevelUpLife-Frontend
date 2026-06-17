package com.example.leveluplife.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.leveluplife.AppContainer
import com.example.leveluplife.R
import com.example.leveluplife.data.network.dto.HabitTaskDto
import com.example.leveluplife.ui.auth.LoginScreen
import com.example.leveluplife.ui.auth.LoginViewModel
import com.example.leveluplife.ui.createtask.CreateHabitTaskScreen
import com.example.leveluplife.ui.createtask.CreateHabitTaskViewModel
import com.example.leveluplife.ui.habitdetail.HabitDetailScreen
import com.example.leveluplife.ui.habitdetail.HabitDetailViewModel
import com.example.leveluplife.ui.habittaskdetail.HabitTaskDetailScreen
import com.example.leveluplife.ui.habittaskdetail.HabitTaskDetailViewModel
import com.example.leveluplife.ui.updatetask.UpdateHabitTaskScreen
import com.example.leveluplife.ui.updatetask.UpdateHabitTaskViewModel
import com.example.leveluplife.ui.home.HomeScreen
import com.example.leveluplife.ui.home.HomeViewModel
import com.example.leveluplife.ui.profile.ProfileScreen
import com.example.leveluplife.ui.profile.ProfileViewModel
import com.example.leveluplife.ui.settings.SettingsScreen
import com.example.leveluplife.ui.settings.SettingsViewModel
import kotlinx.serialization.json.Json

object Routes {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"
    const val HABIT_DETAIL = "habit_detail/{habitId}"
    const val CREATE_HABIT_TASK = "create_habit_task?habitId={habitId}"
    const val HABIT_TASK_DETAIL = "habit_task_detail/{taskId}"
    const val EDIT_HABIT_TASK = "edit_habit_task/{taskId}"

    fun habitDetail(id: Int) = "habit_detail/$id"
    fun createHabitTask(habitId: Int = -1) = "create_habit_task?habitId=$habitId"
    fun habitTaskDetail(taskId: Int) = "habit_task_detail/$taskId"
    fun editHabitTask(taskId: Int) = "edit_habit_task/$taskId"

    const val ARG_CREATED_TASK_JSON = "created_task_json"
    const val ARG_TASK_SUCCESS_KIND = "task_success_kind"
    const val TASK_SUCCESS_CREATED = "created"
    const val TASK_SUCCESS_UPDATED = "updated"
    const val ARG_SHOW_CONFIRMATION = "show_confirmation"
    const val ARG_DEACTIVATION_MESSAGE = "deactivation_message"
    const val ARG_SESSION_EXPIRED_MESSAGE = "session_expired_message"
}

@Composable
fun AppNavigation(
    container: AppContainer,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    json: Json = Json { ignoreUnknownKeys = true },
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
                onHabitClick = { habitId ->
                    navController.navigate(Routes.habitDetail(habitId))
                },
                onCreateTask = {
                    navController.navigate(Routes.createHabitTask())
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
        composable(
            route = Routes.HABIT_DETAIL,
            arguments = listOf(navArgument("habitId") { type = NavType.IntType }),
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getInt("habitId") ?: return@composable
            val vm: HabitDetailViewModel = viewModel(
                factory = HabitDetailViewModel.Factory(container.habitRepository, habitId),
            )
            HabitDetailScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onTaskClick = { task ->
                    navController.navigate(Routes.habitTaskDetail(task.id))
                    navController.currentBackStackEntry?.savedStateHandle?.set(
                        Routes.ARG_CREATED_TASK_JSON,
                        json.encodeToString(HabitTaskDto.serializer(), task),
                    )
                },
            )
        }
        composable(
            route = Routes.CREATE_HABIT_TASK,
            arguments = listOf(
                navArgument("habitId") {
                    type = NavType.IntType
                    defaultValue = -1
                },
            ),
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getInt("habitId") ?: -1
            val vm: CreateHabitTaskViewModel = viewModel(
                factory = CreateHabitTaskViewModel.Factory(
                    container.habitRepository,
                    container.habitTaskRepository,
                    habitId.takeIf { it > 0 },
                ),
            )
            CreateHabitTaskScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onTaskCreated = { task ->
                    val taskJson = json.encodeToString(HabitTaskDto.serializer(), task)
                    navController.navigate(Routes.habitTaskDetail(task.id)) {
                        popUpTo(Routes.CREATE_HABIT_TASK) { inclusive = true }
                    }
                    navController.currentBackStackEntry?.savedStateHandle?.set(
                        Routes.ARG_CREATED_TASK_JSON,
                        taskJson,
                    )
                    navController.currentBackStackEntry?.savedStateHandle?.set(
                        Routes.ARG_TASK_SUCCESS_KIND,
                        Routes.TASK_SUCCESS_CREATED,
                    )
                },
            )
        }
        composable(
            route = Routes.EDIT_HABIT_TASK,
            arguments = listOf(navArgument("taskId") { type = NavType.IntType }),
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getInt("taskId") ?: return@composable
            val vm: UpdateHabitTaskViewModel = viewModel(
                factory = UpdateHabitTaskViewModel.Factory(
                    taskId,
                    container.habitRepository,
                    container.habitTaskRepository,
                ),
            )
            UpdateHabitTaskScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onTaskUpdated = { task ->
                    val taskJson = json.encodeToString(HabitTaskDto.serializer(), task)
                    navController.navigate(Routes.habitTaskDetail(task.id)) {
                        popUpTo(Routes.editHabitTask(taskId)) { inclusive = true }
                    }
                    navController.currentBackStackEntry?.savedStateHandle?.set(
                        Routes.ARG_CREATED_TASK_JSON,
                        taskJson,
                    )
                    navController.currentBackStackEntry?.savedStateHandle?.set(
                        Routes.ARG_TASK_SUCCESS_KIND,
                        Routes.TASK_SUCCESS_UPDATED,
                    )
                },
            )
        }
        composable(
            route = Routes.HABIT_TASK_DETAIL,
            arguments = listOf(navArgument("taskId") { type = NavType.IntType }),
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getInt("taskId") ?: return@composable
            val taskJson = backStackEntry.savedStateHandle.get<String>(Routes.ARG_CREATED_TASK_JSON)
                ?: navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.get<String>(Routes.ARG_CREATED_TASK_JSON)
            val task = taskJson?.let {
                runCatching { json.decodeFromString(HabitTaskDto.serializer(), it) }.getOrNull()
            }
            val successKind = backStackEntry.savedStateHandle.get<String>(Routes.ARG_TASK_SUCCESS_KIND)
                ?: navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.get<String>(Routes.ARG_TASK_SUCCESS_KIND)
            val successMessageRes = when (successKind) {
                Routes.TASK_SUCCESS_UPDATED -> R.string.update_task_success
                Routes.TASK_SUCCESS_CREATED -> R.string.create_task_success_message
                else -> null
            }
            val vm: HabitTaskDetailViewModel = viewModel(
                factory = HabitTaskDetailViewModel.Factory(
                    taskId = taskId,
                    habitTaskRepository = container.habitTaskRepository,
                    habitRepository = container.habitRepository,
                    initialTask = task?.takeIf { it.id == taskId },
                ),
            )
            HabitTaskDetailScreen(
                viewModel = vm,
                successMessageRes = successMessageRes,
                onBack = { navController.popBackStack() },
                onDone = { navController.popBackStack(Routes.DASHBOARD, inclusive = false) },
                onEdit = { loadedTask ->
                    navController.navigate(Routes.editHabitTask(loadedTask.id))
                },
            )
        }
    }
}
