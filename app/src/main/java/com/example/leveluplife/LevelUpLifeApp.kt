package com.example.leveluplife

import android.app.Application
import com.example.leveluplife.notifications.TaskReminderScheduler

class LevelUpLifeApp : Application() {

    lateinit var container: AppContainer
        private set


    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        TaskReminderScheduler.schedule(this)
    }
}
