package com.myrota.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.myrota.app.data.settings.AppSettings
import com.myrota.app.data.settings.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: SettingsRepository
) : ViewModel() {

    val settings: StateFlow<AppSettings> =
        repository
            .settings
            .stateIn(
                scope = viewModelScope,
                started =
                    SharingStarted.WhileSubscribed(
                        5_000
                    ),
                initialValue =
                    AppSettings()
            )

    fun setReminderLeadMinutes(
        minutes: Int
    ) {

        viewModelScope.launch {

            repository
                .setReminderLeadMinutes(
                    minutes
                )
        }
    }
}

class SettingsViewModelFactory(
    private val repository:
    SettingsRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (
            modelClass.isAssignableFrom(
                SettingsViewModel::class.java
            )
        ) {

            return SettingsViewModel(
                repository =
                    repository
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}