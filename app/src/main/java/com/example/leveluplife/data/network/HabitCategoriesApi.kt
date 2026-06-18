package com.example.leveluplife.data.network

import com.example.leveluplife.data.network.dto.HabitCategoriesPageResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface HabitCategoriesApi {
    @GET("api/HabitCategory/list")
    suspend fun getActiveCategories(
        @Query("pageNumber") pageNumber: Int,
        @Query("pageSize") pageSize: Int,
        @Query("search") search: String? = null,
    ): Response<HabitCategoriesPageResponse>
}
