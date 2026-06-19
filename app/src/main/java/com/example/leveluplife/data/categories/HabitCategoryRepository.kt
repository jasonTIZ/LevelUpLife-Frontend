package com.example.leveluplife.data.categories

import com.example.leveluplife.data.network.HabitCategoriesApi
import com.example.leveluplife.data.network.dto.HabitCategoriesPageResponse
import com.example.leveluplife.data.network.dto.PaginationDto

interface HabitCategoryRepository {
    suspend fun getActiveCategories(
        page: Int,
        pageSize: Int = 10,
        search: String? = null,
    ): Result<HabitCategoriesPageResponse>
}

class DefaultHabitCategoryRepository(
    private val api: HabitCategoriesApi,
) : HabitCategoryRepository {

    override suspend fun getActiveCategories(
        page: Int,
        pageSize: Int,
        search: String?,
    ): Result<HabitCategoriesPageResponse> = try {
        val response = api.getActiveCategories(page, pageSize, search?.takeIf { it.isNotBlank() })
        when {
            response.isSuccessful -> Result.success(requireNotNull(response.body()))
            response.code() == 400 -> Result.success(
                HabitCategoriesPageResponse(
                    success = true,
                    categories = emptyList(),
                    pagination = PaginationDto(
                        currentPage = page,
                        pageSize = pageSize,
                        totalPages = 1,
                        totalRecords = 0,
                    ),
                )
            )
            else -> Result.failure(Exception("HTTP ${response.code()}"))
        }
    } catch (t: Throwable) {
        Result.failure(t)
    }
}
