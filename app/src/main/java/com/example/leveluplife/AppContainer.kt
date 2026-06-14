package com.example.leveluplife

import android.content.Context
import com.example.leveluplife.data.auth.AuthRepository
import com.example.leveluplife.data.auth.DefaultAuthRepository
import com.example.leveluplife.data.auth.EncryptedTokenStore
import com.example.leveluplife.data.auth.SessionEvents
import com.example.leveluplife.data.auth.TokenStore
import com.example.leveluplife.data.habits.DefaultHabitRepository
import com.example.leveluplife.data.habits.HabitRepository
import com.example.leveluplife.data.network.AuthApi
import com.example.leveluplife.data.network.HabitsApi
import com.example.leveluplife.data.network.HostProvider
import com.example.leveluplife.data.network.NetworkModule
import com.example.leveluplife.data.network.PlayerApi
import com.example.leveluplife.data.player.DefaultProfileCache
import com.example.leveluplife.data.player.DefaultProfileRepository
import com.example.leveluplife.data.player.LocalProfileAvatarStorage
import com.example.leveluplife.data.player.ProfileAvatarStorage
import com.example.leveluplife.data.player.ProfileCache
import com.example.leveluplife.data.player.ProfileRepository
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
    val sessionEvents: SessionEvents = SessionEvents()

    private val okHttp = NetworkModule.provideOkHttp(tokenStore, sessionEvents)
    private val retrofit by lazy {
        NetworkModule.provideRetrofit(okHttp, hostProvider.resolveBaseUrl())
    }
    private val authApi: AuthApi by lazy { NetworkModule.provideAuthApi(retrofit) }
    private val habitsApi: HabitsApi by lazy { NetworkModule.provideHabitsApi(retrofit) }
    private val playerApi: PlayerApi by lazy { NetworkModule.providePlayerApi(retrofit) }

    val profileAvatarStorage: ProfileAvatarStorage by lazy { LocalProfileAvatarStorage(appContext) }

    val profileCache: ProfileCache by lazy {
        DefaultProfileCache(appContext, profileAvatarStorage)
    }

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

    val profileRepository: ProfileRepository by lazy {
        DefaultProfileRepository(
            api = playerApi,
            profileCache = profileCache,
            json = NetworkModule.jsonParser(),
        )
    }
}
