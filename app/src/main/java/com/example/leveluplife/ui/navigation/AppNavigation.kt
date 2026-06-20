package com.example.leveluplife.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.example.leveluplife.data.auth.SessionEvent
import com.example.leveluplife.data.habits.HabitRepository
import com.example.leveluplife.data.network.dto.HabitTaskDto
import com.example.leveluplife.data.network.dto.RewardItemDto
import com.example.leveluplife.notifications.TaskReminderScheduler
import com.example.leveluplife.ui.auth.LoginScreen
import com.example.leveluplife.ui.auth.LoginViewModel
import com.example.leveluplife.ui.auth.RegisterScreen
import com.example.leveluplife.ui.auth.RegisterViewModel
import com.example.leveluplife.ui.categories.CategoriesScreen
import com.example.leveluplife.ui.categories.CategoriesViewModel
import com.example.leveluplife.ui.createtask.CreateHabitTaskScreen
import com.example.leveluplife.ui.createtask.CreateHabitTaskViewModel
import com.example.leveluplife.ui.habit.CreateHabitScreen
import com.example.leveluplife.ui.habit.CreateHabitViewModel
import com.example.leveluplife.ui.coach.CoachScreen
import com.example.leveluplife.ui.coach.CoachViewModel
import com.example.leveluplife.ui.inventory.InventoryScreen
import com.example.leveluplife.ui.inventory.InventoryViewModel
import com.example.leveluplife.ui.store.StoreDetailScreen
import com.example.leveluplife.ui.store.StoreDetailViewModel
import com.example.leveluplife.ui.store.StoreScreen
import com.example.leveluplife.ui.store.StoreViewModel
import com.example.leveluplife.ui.evidence.EvidenceGalleryScreen
import com.example.leveluplife.ui.evidence.EvidenceGalleryViewModel
import com.example.leveluplife.ui.habitdetail.HabitDetailScreen
import com.example.leveluplife.ui.habitdetail.HabitDetailViewModel
import com.example.leveluplife.ui.habittaskdetail.HabitTaskDetailScreen
import com.example.leveluplife.ui.habittaskdetail.HabitTaskDetailViewModel
import com.example.leveluplife.ui.updatetask.UpdateHabitTaskScreen
import com.example.leveluplife.ui.updatetask.UpdateHabitTaskViewModel
import com.example.leveluplife.ui.home.HomeScreen
import com.example.leveluplife.ui.home.HomeViewModel
import com.example.leveluplife.ui.pomodoro.PomodoroScreen
import com.example.leveluplife.ui.pomodoro.PomodoroViewModel
import com.example.leveluplife.ui.profile.ProfileScreen
import com.example.leveluplife.ui.profile.ProfileViewModel
import com.example.leveluplife.ui.settings.SettingsScreen
import com.example.leveluplife.ui.settings.SettingsViewModel
import com.example.leveluplife.ui.components.showLulSnackbar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val DASHBOARD = "dashboard"
    const val CREATE_HABIT = "create_habit"
    const val CATEGORIES = "categories"
    const val POMODORO = "pomodoro"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"
    const val HABIT_DETAIL = "habit_detail/{habitId}"
    const val CREATE_HABIT_TASK = "create_habit_task?habitId={habitId}"
    const val HABIT_TASK_DETAIL = "habit_task_detail/{taskId}"
    const val TASK_EVIDENCES = "task_evidences/{taskId}?isCompleted={isCompleted}&evidence={evidence}"
    const val EDIT_HABIT_TASK = "edit_habit_task/{taskId}"
    const val COACH = "coach"
    const val STORE = "store"
    const val STORE_DETAIL = "store_detail"
    const val INVENTORY = "inventory"
    const val ARG_STORE_ITEM_JSON = "store_item_json"

    fun habitDetail(id: Int) = "habit_detail/$id"
    fun createHabitTask(habitId: Int = -1) = "create_habit_task?habitId=$habitId"
    fun habitTaskDetail(taskId: Int) = "habit_task_detail/$taskId"
    fun taskEvidences(taskId: Int, isCompleted: Boolean = false, evidence: String = "") =
        "task_evidences/$taskId?isCompleted=$isCompleted&evidence=$evidence"
    fun editHabitTask(taskId: Int) = "edit_habit_task/$taskId"

    const val ARG_REFRESH_HABITS = "refresh_habits"
    const val ARG_REFRESH_PLAYER = "refresh_player"
    const val ARG_CREATED_TASK_JSON = "created_task_json"
    const val ARG_TASK_SUCCESS_KIND = "task_success_kind"
    const val TASK_SUCCESS_CREATED = "created"
    const val TASK_SUCCESS_UPDATED = "updated"
    const val ARG_SHOW_CONFIRMATION = "show_confirmation"
    const val ARG_TASK_DEACTIVATED_MESSAGE = "task_deactivated_message"
    const val ARG_DEACTIVATION_MESSAGE = "deactivation_message"
    const val ARG_SESSION_EXPIRED_MESSAGE = "session_expired_message"
    const val ARG_REGISTER_SUCCESS_MESSAGE = "register_success_message"
}

private fun NavHostController.navigateToLoginFromRoot() {
    navigate(Routes.LOGIN) {
        popUpTo(Routes.DASHBOARD) { inclusive = true }
        launchSingleTop = true
    }
}

private fun NavHostController.setLoginSavedMessage(key: String, message: String) {
    runCatching {
        getBackStackEntry(Routes.LOGIN)
            .savedStateHandle
            .set(key, message)
    }
}

@Composable
fun AppNavigation(
    container: AppContainer,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    json: Json = Json { ignoreUnknownKeys = true },
    deepLinkTaskId: Int? = null,
    onDeepLinkHandled: () -> Unit = {},
) {
    val startDestination = if (container.authRepository.isLoggedIn()) {
        Routes.DASHBOARD
    } else {
        Routes.LOGIN
    }

    val sessionExpiredMessage = stringResource(R.string.login_session_expired_redirect)
    val defaultDeactivationMessage = stringResource(R.string.settings_deactivate_success)
    val forbiddenMessage = stringResource(R.string.session_forbidden_message)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(
        container.sessionEvents,
        sessionExpiredMessage,
        defaultDeactivationMessage,
        forbiddenMessage,
    ) {
        container.sessionEvents.events.collect { event ->
            when (event) {
                SessionEvent.SESSION_EXPIRED -> {
                    navController.navigateToLoginFromRoot()
                    navController.setLoginSavedMessage(
                        Routes.ARG_SESSION_EXPIRED_MESSAGE,
                        sessionExpiredMessage,
                    )
                }
                SessionEvent.SESSION_LOGOUT -> {
                    navController.navigateToLoginFromRoot()
                }
                SessionEvent.SESSION_ACCOUNT_DEACTIVATED -> {
                    val message = container.sessionEvents.consumePendingDeactivationMessage()
                        ?: defaultDeactivationMessage
                    navController.navigateToLoginFromRoot()
                    navController.setLoginSavedMessage(Routes.ARG_DEACTIVATION_MESSAGE, message)
                }
                SessionEvent.SESSION_FORBIDDEN -> {
                    snackbarHostState.showLulSnackbar(forbiddenMessage)
                }
                SessionEvent.SESSION_LOGIN_SUCCESS -> Unit
            }
        }
    }

    LaunchedEffect(deepLinkTaskId) {
        val taskId = deepLinkTaskId ?: return@LaunchedEffect
        if (container.authRepository.isLoggedIn()) {
            navController.navigate(Routes.habitTaskDetail(taskId)) {
                launchSingleTop = true
            }
        }
        onDeepLinkHandled()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier,
    ) { padding ->
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.padding(padding),
    ) {
        composable(Routes.LOGIN) { backStackEntry ->
            val infoMessage = backStackEntry.savedStateHandle
                .get<String>(Routes.ARG_DEACTIVATION_MESSAGE)
                ?: backStackEntry.savedStateHandle
                    .get<String>(Routes.ARG_SESSION_EXPIRED_MESSAGE)
                ?: backStackEntry.savedStateHandle
                    .get<String>(Routes.ARG_REGISTER_SUCCESS_MESSAGE)
            val vm: LoginViewModel = viewModel(
                factory = LoginViewModel.Factory(container.authRepository, container.habitRepository as HabitRepository),
            )
            LoginScreen(
                viewModel = vm,
                themeController = container.themeController,
                infoMessage = infoMessage,
                onInfoMessageShown = {
                    backStackEntry.savedStateHandle.remove<String>(Routes.ARG_DEACTIVATION_MESSAGE)
                    backStackEntry.savedStateHandle.remove<String>(Routes.ARG_SESSION_EXPIRED_MESSAGE)
                    backStackEntry.savedStateHandle.remove<String>(Routes.ARG_REGISTER_SUCCESS_MESSAGE)
                },
                onLoggedIn = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER) { launchSingleTop = true }
                },
            )
        }
        composable(Routes.REGISTER) {
            val vm: RegisterViewModel = viewModel(
                factory = RegisterViewModel.Factory(container.authRepository),
            )
            RegisterScreen(
                viewModel = vm,
                themeController = container.themeController,
                onRegisteredAndLoggedIn = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToLogin = { successMessage ->
                    navController.popBackStack()
                    if (!successMessage.isNullOrBlank()) {
                        runCatching {
                            navController.getBackStackEntry(Routes.LOGIN)
                                .savedStateHandle
                                .set(Routes.ARG_REGISTER_SUCCESS_MESSAGE, successMessage)
                        }
                    }
                },
            )
        }
        composable(Routes.DASHBOARD) { backStackEntry ->
            val vm: HomeViewModel = viewModel(
                factory = HomeViewModel.Factory(
                    container.habitRepository as HabitRepository,
                    container.profileRepository,
                    container.profileCache,
                ),
            )
  val shouldRefresh by backStackEntry.savedStateHandle
      .getStateFlow(Routes.ARG_REFRESH_HABITS, false)
      .collectAsState()
  val shouldRefreshPlayer by backStackEntry.savedStateHandle
      .getStateFlow(Routes.ARG_REFRESH_PLAYER, false)
      .collectAsState()
  val dashboardContext = LocalContext.current
  LaunchedEffect(Unit) {
      TaskReminderScheduler.runNow(dashboardContext)
  }
  LaunchedEffect(shouldRefresh) {
      if (shouldRefresh) {
          vm.loadHabits()
          backStackEntry.savedStateHandle.remove<Boolean>(Routes.ARG_REFRESH_HABITS)
      }
  }
  LaunchedEffect(shouldRefreshPlayer) {
      if (shouldRefreshPlayer) {
          vm.refreshPlayerProgress()
          backStackEntry.savedStateHandle.remove<Boolean>(Routes.ARG_REFRESH_PLAYER)
      }
  }
            HomeScreen(
                viewModel = vm,
                profileCache = container.profileCache,
                onOpenProfile = {
                    navController.navigate(Routes.PROFILE) { launchSingleTop = true }
                },
                onOpenSettings = {
                    navController.navigate(Routes.SETTINGS) { launchSingleTop = true }
                },
                onOpenCategories = {
                    navController.navigate(Routes.CATEGORIES) { launchSingleTop = true }
                },
                onOpenPomodoro = {
                    navController.navigate(Routes.POMODORO) { launchSingleTop = true }
                },
                onHabitClick = { habitId ->
                    navController.navigate(Routes.habitDetail(habitId))
                },
                onCreateHabit = {
                    navController.navigate(Routes.CREATE_HABIT)
                },
                onOpenCoach = {
                    navController.navigate(Routes.COACH) { launchSingleTop = true }
                },
                onOpenStore = {
                    navController.navigate(Routes.STORE) { launchSingleTop = true }
                },
            )
        }
        composable(Routes.PROFILE) {
            val vm: ProfileViewModel = viewModel(
                factory = ProfileViewModel.Factory(
                    container.profileRepository,
                    container.profileCache,
                    container.tokenStore,
                ),
            )
            ProfileScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onLogout = {
                    scope.launch {
                        container.authRepository.logout()
                    }
                },
            )
        }
        composable(Routes.CREATE_HABIT) {
            val vm: CreateHabitViewModel = viewModel(
                factory = CreateHabitViewModel.Factory(
                    container.habitRepository as HabitRepository,
                    container.habitDisciplineRepository,
                    container.habitCategoryRepository,
                ),
            )
            CreateHabitScreen(
                viewModel = vm,
                onHabitCreated = {
                    runCatching {
                        navController.getBackStackEntry(Routes.DASHBOARD)
                            .savedStateHandle
                            .set(Routes.ARG_REFRESH_HABITS, true)
                    }
                    navController.popBackStack()
                },
            )
        }
        composable(Routes.CATEGORIES) {
            val vm: CategoriesViewModel = viewModel(
                factory = CategoriesViewModel.Factory(container.habitCategoryRepository),
            )
            LaunchedEffect(Unit) {
                vm.loadCategories()
            }
            CategoriesScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onCategoryClick = {
                    // Navegación al detalle de categoría: fuera de alcance de esta tarea.
                },
            )
        }
        composable(Routes.POMODORO) {
            val vm: PomodoroViewModel = viewModel(
                factory = PomodoroViewModel.Factory(container.habitRepository),
            )
            PomodoroScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.SETTINGS) {
            val vm: SettingsViewModel = viewModel(
                factory = SettingsViewModel.Factory(container.playerRepository),
            )
            SettingsScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = Routes.HABIT_DETAIL,
            arguments = listOf(navArgument("habitId") { type = NavType.IntType }),
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getInt("habitId") ?: return@composable
            val infoMessage by backStackEntry.savedStateHandle
                .getStateFlow<String?>(Routes.ARG_TASK_DEACTIVATED_MESSAGE, null)
                .collectAsState()
            val vm: HabitDetailViewModel = viewModel(
                factory = HabitDetailViewModel.Factory(
                    container.habitRepository,
                    container.habitDisciplineRepository,
                    habitId,
                ),
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
                infoMessage = infoMessage,
                onInfoMessageShown = {
                    backStackEntry.savedStateHandle.remove<String>(Routes.ARG_TASK_DEACTIVATED_MESSAGE)
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
                    container.habitDisciplineRepository,
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
            val initialTask = taskJson?.let {
                runCatching { json.decodeFromString(HabitTaskDto.serializer(), it) }.getOrNull()
            }?.takeIf { it.id == taskId }
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
                    profileCache = container.profileCache,
                    initialTask = initialTask,
                ),
            )
            HabitTaskDetailScreen(
                viewModel = vm,
                successMessageRes = successMessageRes,
                onBack = { navController.popBackStack() },
                onDone = { navController.popBackStack(Routes.DASHBOARD, inclusive = false) },
                onViewEvidences = {
                    val task = vm.state.value.task
                    navController.navigate(
                        Routes.taskEvidences(taskId, task?.isCompleted ?: false, task?.evidence ?: ""),
                    )
                },
                onEdit = { loadedTask ->
                    navController.navigate(Routes.editHabitTask(loadedTask.id))
                },
                onTaskDeactivated = { message ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(Routes.ARG_TASK_DEACTIVATED_MESSAGE, message)
                    navController.popBackStack()
                },
            )
        }
        composable(Routes.COACH) {
            val vm: CoachViewModel = viewModel(
                factory = CoachViewModel.Factory(container.coachRepository, container.tokenStore, container.chatStorage),
            )
            CoachScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.STORE) {
            val vm: StoreViewModel = viewModel(
                factory = StoreViewModel.Factory(container.rewardRepository),
            )
            StoreScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onOpenInventory = {
                    navController.navigate(Routes.INVENTORY) { launchSingleTop = true }
                },
                onItemClick = { item ->
                    val itemJson = json.encodeToString(RewardItemDto.serializer(), item)
                    navController.navigate(Routes.STORE_DETAIL)
                    navController.currentBackStackEntry?.savedStateHandle?.set(
                        Routes.ARG_STORE_ITEM_JSON,
                        itemJson,
                    )
                },
                onPurchaseSuccess = {
                    runCatching {
                        navController.getBackStackEntry(Routes.DASHBOARD)
                            .savedStateHandle
                            .set(Routes.ARG_REFRESH_PLAYER, true)
                    }
                },
            )
        }
        composable(Routes.STORE_DETAIL) { backStackEntry ->
            val itemJson = backStackEntry.savedStateHandle.get<String>(Routes.ARG_STORE_ITEM_JSON)
                ?: navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.get<String>(Routes.ARG_STORE_ITEM_JSON)
            val item = itemJson?.let {
                runCatching { json.decodeFromString(RewardItemDto.serializer(), it) }.getOrNull()
            } ?: return@composable
            val vm: StoreDetailViewModel = viewModel(
                factory = StoreDetailViewModel.Factory(container.rewardRepository),
            )
            StoreDetailScreen(
                item = item,
                viewModel = vm,
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.INVENTORY) {
            val vm: InventoryViewModel = viewModel(
                factory = InventoryViewModel.Factory(container.rewardRepository),
            )
            InventoryScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = Routes.TASK_EVIDENCES,
            arguments = listOf(
                navArgument("taskId") { type = NavType.IntType },
                navArgument("isCompleted") { type = NavType.BoolType; defaultValue = false },
                navArgument("evidence") { type = NavType.StringType; defaultValue = "" },
            ),
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getInt("taskId") ?: return@composable
            val isCompleted = backStackEntry.arguments?.getBoolean("isCompleted") ?: false
            val evidence = backStackEntry.arguments?.getString("evidence").orEmpty()
            val vm: EvidenceGalleryViewModel = viewModel(
                factory = EvidenceGalleryViewModel.Factory(
                    container.evidenceRepository,
                    taskId,
                    isCompleted,
                    container.healthConnectManager,
                    evidence,
                ),
            )
            EvidenceGalleryScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
            )
        }
    }
    }
}
