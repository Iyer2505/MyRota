package com.myrota.app.data.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val SETTINGS_DATASTORE_NAME =
    "myrota_settings"

private val Context.settingsDataStore by preferencesDataStore(
    name = SETTINGS_DATASTORE_NAME
)

class SettingsRepository(
    private val context: Context
) {

    companion object {

        private val REMINDER_LEAD_MINUTES =
            intPreferencesKey(
                "reminder_lead_minutes"
            )

        const val DEFAULT_REMINDER_LEAD_MINUTES =
            30
    }

    val settings: Flow<AppSettings> =
        context
            .settingsDataStore
            .data
            .map { preferences ->

                val storedReminderMinutes =
                    preferences[
                        REMINDER_LEAD_MINUTES
                    ]
                        ?: DEFAULT_REMINDER_LEAD_MINUTES

                AppSettings(
                    reminderLeadMinutes =
                        validateReminderMinutes(
                            storedReminderMinutes
                        )
                )
            }

    suspend fun setReminderLeadMinutes(
        minutes: Int
    ) {

        val validatedMinutes =
            validateReminderMinutes(
                minutes
            )

        context
            .settingsDataStore
            .edit { preferences ->

                preferences[
                    REMINDER_LEAD_MINUTES
                ] =
                    validatedMinutes
            }
    }

    private fun validateReminderMinutes(
        minutes: Int
    ): Int {

        return when (minutes) {

            15,
            30,
            60 ->
                minutes

            else ->
                DEFAULT_REMINDER_LEAD_MINUTES
        }
    }
}