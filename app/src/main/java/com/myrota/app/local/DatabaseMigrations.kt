package com.myrota.app.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 =
    object : Migration(
        1,
        2
    ) {

        override fun migrate(
            db: SupportSQLiteDatabase
        ) {

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `shifts` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `companyId` INTEGER NOT NULL,
                    `scheduledStartEpochMillis` INTEGER NOT NULL,
                    `scheduledEndEpochMillis` INTEGER NOT NULL,
                    `actualStartEpochMillis` INTEGER,
                    `actualEndEpochMillis` INTEGER,
                    `notes` TEXT,
                    `status` TEXT NOT NULL,
                    `createdAtEpochMillis` INTEGER NOT NULL,
                    `updatedAtEpochMillis` INTEGER NOT NULL,
                    FOREIGN KEY(`companyId`)
                        REFERENCES `companies`(`id`)
                        ON UPDATE NO ACTION
                        ON DELETE RESTRICT
                )
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS
                `index_shifts_companyId`
                ON `shifts` (`companyId`)
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS
                `index_shifts_scheduledStartEpochMillis`
                ON `shifts`
                (`scheduledStartEpochMillis`)
                """.trimIndent()
            )
        }
    }

val MIGRATION_2_3 =
    object : Migration(
        2,
        3
    ) {

        override fun migrate(
            db: SupportSQLiteDatabase
        ) {

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `breaks` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `shiftId` INTEGER NOT NULL,
                    `durationMinutes` INTEGER NOT NULL,
                    `isPaid` INTEGER NOT NULL,
                    `createdAtEpochMillis` INTEGER NOT NULL,
                    `updatedAtEpochMillis` INTEGER NOT NULL,
                    FOREIGN KEY(`shiftId`)
                        REFERENCES `shifts`(`id`)
                        ON UPDATE NO ACTION
                        ON DELETE CASCADE
                )
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS
                `index_breaks_shiftId`
                ON `breaks` (`shiftId`)
                """.trimIndent()
            )
        }
    }