package com.example.kchat.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.kchat.data.ApiConfig
import com.example.kchat.data.SettingsRepository

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository.getInstance(application)

    val apiConfig = repository.apiConfig

    fun saveApiConfig(config: ApiConfig) {
        repository.saveApiConfig(config)
    }
}
