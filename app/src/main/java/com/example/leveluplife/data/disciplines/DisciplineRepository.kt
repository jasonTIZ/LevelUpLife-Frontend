package com.example.leveluplife.data.disciplines

import com.example.leveluplife.data.auth.TokenStore
import com.example.leveluplife.data.network.DisciplineApi
import com.example.leveluplife.data.network.dto.DisciplineData
import com.example.leveluplife.data.network.dto.HabitData
import java.io.IOException

data class PagedHabits(
    val habits: List<HabitData>,
    val currentPage: Int,
    val totalPages: Int,
    val totalItems: Int,
)

interface DisciplineRepository {
    suspend fun getDiscipline(id: String): Result<DisciplineData>
    suspend fun getHabits(disciplineId: String, page: Int, size: Int): Result<PagedHabits>
}

class DefaultDisciplineRepository(
    private val api: DisciplineApi,
    private val tokenStore: TokenStore,
) : DisciplineRepository {

    private fun bearerToken(): String = "Bearer ${tokenStore.accessToken().orEmpty()}"

    override suspend fun getDiscipline(id: String): Result<DisciplineData> = try {
        val response = api.getDiscipline(id = id, token = bearerToken())
        when {
            response.isSuccessful -> {
                val data = response.body()?.data
                if (data != null) Result.success(data)
                else Result.failure(DisciplineErrorException(DisciplineError.Unknown("empty_body")))
            }
            response.code() == 404 -> Result.failure(DisciplineErrorException(DisciplineError.NotFound))
            response.code() in 500..599 -> Result.failure(DisciplineErrorException(DisciplineError.Server()))
            else -> Result.failure(DisciplineErrorException(DisciplineError.Unknown("http_${response.code()}")))
        }
    } catch (e: IOException) {
        Result.failure(DisciplineErrorException(DisciplineError.Network(e.message)))
    } catch (t: Throwable) {
        Result.failure(DisciplineErrorException(DisciplineError.Unknown(t.message)))
    }

    override suspend fun getHabits(disciplineId: String, page: Int, size: Int): Result<PagedHabits> = try {
        val response = api.getHabits(
            disciplineId = disciplineId,
            page = page,
            size = size,
            token = bearerToken(),
        )
        when {
            response.isSuccessful -> {
                val body = response.body()
                val habits = body?.data ?: emptyList()
                val totalPages = body?.totalPages ?: body?.total?.let {
                    val t = body.totalItems ?: it
                    if (size > 0) ((t + size - 1) / size) else 1
                } ?: 1
                val totalItems = body?.totalItems ?: body?.total ?: habits.size
                val currentPage = body?.currentPage ?: body?.page ?: page
                Result.success(PagedHabits(habits, currentPage, totalPages, totalItems))
            }
            response.code() == 404 -> Result.success(PagedHabits(emptyList(), 1, 1, 0))
            response.code() in 500..599 -> Result.failure(DisciplineErrorException(DisciplineError.Server()))
            else -> Result.failure(DisciplineErrorException(DisciplineError.Unknown("http_${response.code()}")))
        }
    } catch (e: IOException) {
        Result.failure(DisciplineErrorException(DisciplineError.Network(e.message)))
    } catch (t: Throwable) {
        Result.failure(DisciplineErrorException(DisciplineError.Unknown(t.message)))
    }
}
