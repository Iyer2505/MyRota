package com.myrota.app

import android.app.Application
import com.myrota.app.data.local.MyRotaDatabaseProvider
import com.myrota.app.data.repository.CompanyRepository

class MyRotaApplication : Application() {

    val database by lazy {
        MyRotaDatabaseProvider.getDatabase(this)
    }

    val companyRepository by lazy {
        CompanyRepository(
            companyDao = database.companyDao()
        )
    }
}