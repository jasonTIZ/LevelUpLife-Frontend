package com.example.leveluplife

import android.content.Context
import com.example.leveluplife.data.auth.AuthRepository
import com.example.leveluplife.data.auth.DefaultAuthRepository
import com.example.leveluplife.data.auth.EncryptedTokenStore
import com.example.leveluplife.data.auth.TokenStore
import com.example.leveluplife.data.habits.DefaultHabitRepository
import com.example.leveluplife.data.habits.DefaultHabitTaskRepository
import com.example.leveluplife.data.habits.HabitRepository
import com.example.leveluplife.data.habits.HabitTaskRepository
import com.example.leveluplife.data.network.AuthApi
import com.example.leveluplife.data.network.HabitTasksApi
import com.example.leveluplife.data.network.HabitsApi
import com.example.leveluplife.data.network.HostProvider
import com.example.leveluplife.data.network.NetworkModule
import com.example.leveluplife.data.preferences.DebugApiPreferences
import com.example.leveluplife.data.preferences.ThemePreferences
import com.example.leveluplife.ui.theme.ThemeController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Dispatchers

class AppContainer(applicationContext: Context) {

    private val appContext = applicationContext.applicationContext
    private val appScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    val themePreferences: ThemePreferences = ThemePreferences(appContext)
    val themeController: ThemeController = ThemeController(themePreferences, appScope)

    val debugApiPreferences: DebugApiPreferences = DebugApiPreferences(appContext)
    val hostProvider: HostProvider = HostProvider(debugApiPreferences)

    val tokenStore: TokenStore = EncryptedTokenStore(appContext)

    private val okHttp = NetworkModule.provideOkHttp(tokenStore)
    private val retrofit by lazy {
        NetworkModule.provideRetrofit(okHttp, hostProvider.resolveBaseUrl())
    }
    private val authApi: AuthApi by lazy { NetworkModule.provideAuthApi(retrofit) }
    private val habitsApi: HabitsApi by lazy { NetworkModule.provideHabitsApi(retrofit) }
    private val habitTasksApi: HabitTasksApi by lazy { NetworkModule.provideHabitTasksApi(retrofit) }

    val authRepository: AuthRepository by lazy {
        DefaultAuthRepository(
            api = authApi,
            tokenStore = tokenStore,
            json = NetworkModule.jsonParser(),
        )
    }

    val habitRepository: HabitRepository by lazy {
        DefaultHabitRepository(api = habitsApi)
    }

    val habitTaskRepository: HabitTaskRepository by lazy {
        DefaultHabitTaskRepository(api = habitTasksApi)
    }
}
