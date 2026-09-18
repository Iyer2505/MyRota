package com.myrota.app.local

import android.content.Context
import androidx.room.Room

object MyRotaDatabaseProvider {

    @Volatile
    private var INSTANCE:
            MyRotaDatabase? = null

    fun getDatabase(
        context: Context
    ): MyRotaDatabase {

        return INSTANCE
            ?: synchronized(this) {

                val instance =
                    Room.databaseBuilder(
                        context.applicationContext,
                        MyRotaDatabase::class.java,
                        "myrota_database"
                    )
                        .addMigrations(
                            MIGRATION_1_2,
                            MIGRATION_2_3
                        )
                        .build()

                INSTANCE =
                    instance

                instance
            }
    }
}