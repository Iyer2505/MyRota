package com.myrota.app.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "breaks",
    foreignKeys = [
        ForeignKey(
            entity = ShiftEntity::class,
            parentColumns = ["id"],
            childColumns = ["shiftId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.NO_ACTION
        )
    ],
    indices = [
        Index(value = ["shiftId"])
    ]
)
data class BreakEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val shiftId: Long,

    val durationMinutes: Int,

    val isPaid: Boolean,

    val createdAtEpochMillis: Long,

    val updatedAtEpochMillis: Long
)