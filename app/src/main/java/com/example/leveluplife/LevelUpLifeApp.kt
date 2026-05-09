package com.example.leveluplife

import android.app.Application

class LevelUpLifeApp : Application() {

    lateinit var container: AppContainer
        private set

    
    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
