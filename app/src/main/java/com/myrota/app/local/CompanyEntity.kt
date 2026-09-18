package com.myrota.app.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "companies")
data class CompanyEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    val description: String? = null,

    val colorArgb: Long,

    val hourlyRatePence: Long? = null,

    val notificationsEnabled: Boolean = true,

    val isActive: Boolean = true,

    val createdAtEpochMillis: Long,

    val updatedAtEpochMillis: Long
)