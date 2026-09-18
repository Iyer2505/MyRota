package com.myrota.app.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "shifts",

    foreignKeys = [
        ForeignKey(
            entity = CompanyEntity::class,
            parentColumns = ["id"],
            childColumns = ["companyId"],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.NO_ACTION
        )
    ],

    indices = [
        Index(
            value = ["companyId"]
        ),
        Index(
            value = ["scheduledStartEpochMillis"]
        )
    ]
)
data class ShiftEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val companyId: Long,

    val scheduledStartEpochMillis: Long,

    val scheduledEndEpochMillis: Long,

    val actualStartEpochMillis: Long? = null,

    val actualEndEpochMillis: Long? = null,

    val notes: String? = null,

    val status: ShiftStatus = ShiftStatus.SCHEDULED,

    val createdAtEpochMillis: Long,

    val updatedAtEpochMillis: Long
)