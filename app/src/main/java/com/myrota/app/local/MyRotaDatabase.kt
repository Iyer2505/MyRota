package com.myrota.app.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        CompanyEntity::class,
        ShiftEntity::class,
        BreakEntity::class
    ],
    version = 3,
    exportSchema = true
)
@TypeConverters(
    RoomConverters::class
)
abstract class MyRotaDatabase :
    RoomDatabase() {

    abstract fun companyDao():
            CompanyDao

    abstract fun shiftDao():
            ShiftDao

    abstract fun breakDao():
            BreakDao
}