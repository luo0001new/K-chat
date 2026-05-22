package com.example.kchat

import android.app.Application
import android.content.Context
import com.example.kchat.data.MomentSchedulerService
import com.example.kchat.data.ProactiveChatSchedulerService
import com.example.kchat.data.SettingsRepository

class KChatApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        startBackgroundServices()
    }

    private fun startBackgroundServices() {
        SettingsRepository.getInstance(this)
        MomentSchedulerService.start(this)
        ProactiveChatSchedulerService.start(this)
    }

    companion object {
        private lateinit var instance: KChatApplication

        fun getInstance(): KChatApplication = instance

        fun getContext(): Context = instance.applicationContext
    }
}
