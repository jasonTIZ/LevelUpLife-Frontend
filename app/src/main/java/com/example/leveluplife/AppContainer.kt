package com.example.leveluplife

import android.content.Context
import android.util.Log
import com.example.leveluplife.BuildConfig
import com.example.leveluplife.data.auth.AuthRepository
import com.example.leveluplife.data.auth.DefaultAuthRepository
import com.example.leveluplife.data.auth.EncryptedTokenStore
import com.example.leveluplife.data.auth.SessionEvents
import com.example.leveluplife.data.auth.TokenStore
import com.example.leveluplife.data.categories.DefaultHabitCategoryRepository
import com.example.leveluplife.data.categories.HabitCategoryRepository
import com.example.leveluplife.data.habits.DefaultHabitDisciplineRepository
import com.example.leveluplife.data.habits.DefaultHabitRepository
import com.example.leveluplife.data.habits.DefaultHabitTaskRepository
import com.example.leveluplife.data.habits.DefaultEvidenceRepository
import com.example.leveluplife.data.habits.EvidenceRepository
import com.example.leveluplife.data.habits.HabitDisciplineRepository
import com.example.leveluplife.data.habits.HabitRepository
import com.example.leveluplife.data.habits.HabitTaskRepository
import com.example.leveluplife.data.network.AuthApi
import com.example.leveluplife.data.network.HabitCategoriesApi
import com.example.leveluplife.data.network.HabitDisciplinesApi
import com.example.leveluplife.data.network.HabitTasksApi
import com.example.leveluplife.data.network.HabitsApi
import com.example.leveluplife.data.network.HostProvider
import com.example.leveluplife.data.network.NetworkModule
import com.example.leveluplife.data.network.PlayerApi
import com.example.leveluplife.data.player.DefaultPlayerRepository
import com.example.leveluplife.data.player.DefaultProfileCache
import com.example.leveluplife.data.player.DefaultProfileRepository
import com.example.leveluplife.data.player.LocalProfileAvatarStorage
import com.example.leveluplife.data.player.ProfileAvatarUploader
import com.example.leveluplife.data.player.PlayerRepository
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

    companion object {
        private const val TAG = "LevelUpLife"
    }
    private val appScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    val themePreferences: ThemePreferences = ThemePreferences(appContext)
    val themeController: ThemeController = ThemeController(themePreferences, appScope)

    val debugApiPreferences: DebugApiPreferences = DebugApiPreferences(appContext)
    val hostProvider: HostProvider = HostProvider(debugApiPreferences)

    val tokenStore: TokenStore = EncryptedTokenStore(appContext)
    val sessionEvents: SessionEvents = SessionEvents()

    val profileAvatarStorage: ProfileAvatarStorage by lazy {
        LocalProfileAvatarStorage(appContext, tokenStore)
    }

    val profileCache: ProfileCache by lazy {
        DefaultProfileCache(appContext, profileAvatarStorage, tokenStore)
    }

    private val okHttp by lazy {
        NetworkModule.provideOkHttp(tokenStore, sessionEvents, profileCache)
    }
    private val retrofit by lazy {
        val baseUrl = hostProvider.resolveBaseUrl()
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "API base URL: $baseUrl (BuildConfig host=${BuildConfig.API_HOST})")
        }
        NetworkModule.provideRetrofit(okHttp, baseUrl)
    }
    private val authApi: AuthApi by lazy { NetworkModule.provideAuthApi(retrofit) }
    private val habitsApi: HabitsApi by lazy { NetworkModule.provideHabitsApi(retrofit) }
    private val habitCategoriesApi: HabitCategoriesApi by lazy {
        NetworkModule.provideHabitCategoriesApi(retrofit)
    }
    private val habitTasksApi: HabitTasksApi by lazy { NetworkModule.provideHabitTasksApi(retrofit) }
    private val habitDisciplinesApi: HabitDisciplinesApi by lazy { NetworkModule.provideHabitDisciplinesApi(retrofit) }
    private val playerApi: PlayerApi by lazy { NetworkModule.providePlayerApi(retrofit) }

    val authRepository: AuthRepository by lazy {
        DefaultAuthRepository(
            api = authApi,
            tokenStore = tokenStore,
            sessionEvents = sessionEvents,
            profileCache = profileCache,
            json = NetworkModule.jsonParser(),
        )
    }

    val habitRepository: HabitRepository by lazy {
        DefaultHabitRepository(api = habitsApi)
    }

    val habitCategoryRepository: HabitCategoryRepository by lazy {
        DefaultHabitCategoryRepository(api = habitCategoriesApi)
    }

    val habitTaskRepository: HabitTaskRepository by lazy {
        DefaultHabitTaskRepository(api = habitTasksApi)
    }

    val habitDisciplineRepository: HabitDisciplineRepository by lazy {
        DefaultHabitDisciplineRepository(api = habitDisciplinesApi)
    }

    val evidenceRepository: EvidenceRepository by lazy {
        DefaultEvidenceRepository(api = habitTasksApi)
    }

    val playerRepository: PlayerRepository by lazy {
        DefaultPlayerRepository(api = playerApi, authRepository = authRepository)
    }

    val profileAvatarUploader: ProfileAvatarUploader by lazy {
        ProfileAvatarUploader(appContext)
    }

    val profileRepository: ProfileRepository by lazy {
        DefaultProfileRepository(
            api = playerApi,
            profileCache = profileCache,
            avatarUploader = profileAvatarUploader,
            apiBaseUrl = hostProvider.resolveBaseUrl().trimEnd('/'),
            json = NetworkModule.jsonParser(),
        )
    }
}
