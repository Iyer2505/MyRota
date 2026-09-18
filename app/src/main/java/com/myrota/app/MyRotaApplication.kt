package com.myrota.app

import android.app.Application
import com.myrota.app.data.repository.BreakRepository
import com.myrota.app.data.repository.CompanyRepository
import com.myrota.app.data.repository.ShiftRepository
import com.myrota.app.data.settings.SettingsRepository
import com.myrota.app.local.MyRotaDatabaseProvider
import com.myrota.app.notifications.NotificationHelper

class MyRotaApplication : Application() {

    val database by lazy {

        MyRotaDatabaseProvider
            .getDatabase(this)
    }

    val companyRepository by lazy {

        CompanyRepository(
            companyDao =
                database.companyDao()
        )
    }

    val shiftRepository by lazy {

        ShiftRepository(
            shiftDao =
                database.shiftDao()
        )
    }

    val breakRepository by lazy {

        BreakRepository(
            breakDao =
                database.breakDao()
        )
    }

    val settingsRepository by lazy {

        SettingsRepository(
            context =
                applicationContext
        )
    }

    override fun onCreate() {

        super.onCreate()

        NotificationHelper
            .createNotificationChannels(
                this
            )
    }
}