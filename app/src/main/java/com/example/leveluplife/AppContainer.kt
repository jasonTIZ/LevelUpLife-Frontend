package com.example.leveluplife

import android.content.Context
import com.example.leveluplife.data.auth.AuthRepository
import com.example.leveluplife.data.auth.DefaultAuthRepository
import com.example.leveluplife.data.auth.EncryptedTokenStore
import com.example.leveluplife.data.auth.TokenStore
import com.example.leveluplife.data.disciplines.DefaultDisciplineRepository
import com.example.leveluplife.data.disciplines.DisciplineRepository
import com.example.leveluplife.data.network.AuthApi
import com.example.leveluplife.data.network.DisciplineApi
import com.example.leveluplife.data.habits.DefaultHabitRepository
import com.example.leveluplife.data.habits.HabitRepository
import com.example.leveluplife.data.network.AuthApi
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
    private val disciplineApi: DisciplineApi by lazy { NetworkModule.provideDisciplineApi(retrofit) }
    private val habitsApi: HabitsApi by lazy { NetworkModule.provideHabitsApi(retrofit) }

    val authRepository: AuthRepository by lazy {
        DefaultAuthRepository(
            api = authApi,
            tokenStore = tokenStore,
            json = NetworkModule.jsonParser(),
        )
    }
    val disciplineRepository: DisciplineRepository by lazy {
        DefaultDisciplineRepository(api = disciplineApi, tokenStore = tokenStore)
    }
    val habitRepository: HabitRepository by lazy {
        DefaultHabitRepository(api = habitsApi)
    }
}
