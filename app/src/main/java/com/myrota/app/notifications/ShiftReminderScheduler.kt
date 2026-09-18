package com.myrota.app.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

class ShiftReminderScheduler(
    private val context: Context
) {

    fun scheduleReminder(
        shiftId: Long,
        companyName: String,
        scheduledStartEpochMillis: Long,
        notificationsEnabled: Boolean,
        reminderLeadMinutes: Int
    ) {

        cancelReminder(
            shiftId
        )

        if (!notificationsEnabled) {
            return
        }

        val validLeadMinutes =
            when (reminderLeadMinutes) {

                15,
                30,
                60 ->
                    reminderLeadMinutes

                else ->
                    30
            }

        val now =
            System.currentTimeMillis()

        if (
            scheduledStartEpochMillis <=
            now
        ) {
            return
        }

        val reminderMillis =
            validLeadMinutes *
                    60_000L

        val normalReminderTime =
            scheduledStartEpochMillis -
                    reminderMillis

        /*
         * If the shift is created inside the
         * reminder window, notify shortly after
         * saving it rather than skipping it.
         */
        val triggerAtMillis =
            if (
                normalReminderTime >
                now
            ) {

                normalReminderTime

            } else {

                now + 1_000L
            }

        val alarmManager =
            context.getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager

        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            createPendingIntent(
                shiftId =
                    shiftId,

                companyName =
                    companyName,

                scheduledStartEpochMillis =
                    scheduledStartEpochMillis
            )
        )
    }

    fun cancelReminder(
        shiftId: Long
    ) {

        val alarmManager =
            context.getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager

        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                requestCodeForShift(
                    shiftId
                ),
                Intent(
                    context,
                    ShiftReminderReceiver::class.java
                ).apply {

                    action =
                        ShiftReminderReceiver
                            .ACTION_SHIFT_REMINDER
                },
                PendingIntent.FLAG_NO_CREATE or
                        PendingIntent.FLAG_IMMUTABLE
            )

        if (
            pendingIntent != null
        ) {

            alarmManager.cancel(
                pendingIntent
            )

            pendingIntent.cancel()
        }
    }

    private fun createPendingIntent(
        shiftId: Long,
        companyName: String,
        scheduledStartEpochMillis: Long
    ): PendingIntent {

        val intent =
            Intent(
                context,
                ShiftReminderReceiver::class.java
            ).apply {

                action =
                    ShiftReminderReceiver
                        .ACTION_SHIFT_REMINDER

                putExtra(
                    ShiftReminderReceiver
                        .EXTRA_SHIFT_ID,
                    shiftId
                )

                putExtra(
                    ShiftReminderReceiver
                        .EXTRA_COMPANY_NAME,
                    companyName
                )

                putExtra(
                    ShiftReminderReceiver
                        .EXTRA_SHIFT_START,
                    scheduledStartEpochMillis
                )
            }

        return PendingIntent.getBroadcast(
            context,
            requestCodeForShift(
                shiftId
            ),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun requestCodeForShift(
        shiftId: Long
    ): Int {

        return (
                shiftId xor
                        (shiftId ushr 32)
                ).toInt()
    }
}