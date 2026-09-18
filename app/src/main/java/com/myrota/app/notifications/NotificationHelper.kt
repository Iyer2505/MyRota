package com.myrota.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationHelper {

    const val SHIFT_REMINDER_CHANNEL_ID =
        "shift_reminders"

    private const val SHIFT_REMINDER_CHANNEL_NAME =
        "Shift Reminders"

    private const val SHIFT_REMINDER_CHANNEL_DESCRIPTION =
        "Notifications for upcoming work shifts"

    fun createNotificationChannels(
        context: Context
    ) {

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {

            val notificationManager =
                context.getSystemService(
                    Context.NOTIFICATION_SERVICE
                ) as NotificationManager

            val shiftReminderChannel =
                NotificationChannel(
                    SHIFT_REMINDER_CHANNEL_ID,
                    SHIFT_REMINDER_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {

                    description =
                        SHIFT_REMINDER_CHANNEL_DESCRIPTION

                    enableVibration(true)
                }

            notificationManager
                .createNotificationChannel(
                    shiftReminderChannel
                )
        }
    }
}