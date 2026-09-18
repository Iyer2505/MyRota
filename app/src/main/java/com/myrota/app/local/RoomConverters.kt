package com.myrota.app.local

import androidx.room.TypeConverter

class RoomConverters {

    @TypeConverter
    fun fromShiftStatus(
        status: ShiftStatus
    ): String {
        return status.name
    }

    @TypeConverter
    fun toShiftStatus(
        value: String
    ): ShiftStatus {
        return ShiftStatus.valueOf(value)
    }
}