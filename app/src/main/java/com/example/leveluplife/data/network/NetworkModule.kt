package com.example.leveluplife.data.network

import com.example.leveluplife.BuildConfig
import com.example.leveluplife.data.auth.TokenStore
import com.example.leveluplife.data.network.interceptor.AuthInterceptor
import com.example.leveluplife.data.network.interceptor.RetryInterceptor
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

object NetworkModule {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = false
    }

    // Interceptor order (outermost → innermost):
    //   RetryInterceptor  – retries transient 5xx / IOException
    //   AuthInterceptor   – re-reads token on every attempt (future-proof for refresh)
    //   HttpLoggingInterceptor – logs the final request including auth header
    fun provideOkHttp(tokenStore: TokenStore): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        }
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(RetryInterceptor())
            .addInterceptor(AuthInterceptor(tokenStore))
            .addInterceptor(logging)
            .build()
    }

    fun provideRetrofit(client: OkHttpClient, baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    fun provideHabitsApi(retrofit: Retrofit): HabitsApi = retrofit.create(HabitsApi::class.java)

    fun provideHabitTasksApi(retrofit: Retrofit): HabitTasksApi =
        retrofit.create(HabitTasksApi::class.java)

    fun jsonParser(): Json = json
}
