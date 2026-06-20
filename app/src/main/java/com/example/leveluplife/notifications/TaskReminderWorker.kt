package com.example.leveluplife.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.leveluplife.LevelUpLifeApp
import com.example.leveluplife.data.network.dto.HabitDto
import java.time.LocalDate

class TaskReminderWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? LevelUpLifeApp ?: return Result.success()
        val container = app.container
        if (!container.authRepository.isLoggedIn()) return Result.success()

        return try {
            val habits = loadActiveHabits()
            val today = LocalDate.now()

            for (habit in habits) {
                val detail = container.habitRepository.getHabitById(habit.id).getOrNull() ?: continue
                for (task in detail.tasks) {
                    if (!task.isActive || task.isCompleted) continue
                    val dueDate = parseDate(task.startDate) ?: continue
                    when {
                        dueDate.isEqual(today) ->
                            TaskNotifier.notifyPendingToday(applicationContext, task.id, task.title)
                        dueDate.isBefore(today) ->
                            TaskNotifier.notifyExpired(applicationContext, task.id, task.title)
                    }
                }
            }
            Result.success()
        } catch (t: Throwable) {
            Result.success()
        }
    }

    private suspend fun loadActiveHabits(): List<HabitDto> {
        val repository = (applicationContext as LevelUpLifeApp).container.habitRepository
        val habits = mutableListOf<HabitDto>()
        var page = 1
        while (true) {
            val response = repository.getActiveHabits(page = page, pageSize = PAGE_SIZE).getOrNull() ?: break
            val pageHabits = response.habits ?: emptyList()
            habits += pageHabits
            val pagination = response.pagination
            val currentPage = pagination?.currentPage ?: page
            val totalPages = pagination?.totalPages ?: currentPage
            if (pageHabits.isEmpty() || currentPage >= totalPages) break
            page = currentPage + 1
        }
        return habits
    }

    private fun parseDate(value: String): LocalDate? =
        runCatching { LocalDate.parse(value.trim().take(10)) }.getOrNull()

    private companion object {
        const val PAGE_SIZE = 50
    }
}
