package com.myrota.app.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.myrota.app.MainActivity
import com.myrota.app.R
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ShiftReminderReceiver :
    BroadcastReceiver() {

    companion object {

        const val ACTION_SHIFT_REMINDER =
            "com.myrota.app.action.SHIFT_REMINDER"

        const val EXTRA_SHIFT_ID =
            "shift_id"

        const val EXTRA_COMPANY_NAME =
            "company_name"

        const val EXTRA_SHIFT_START =
            "shift_start"

        private const val NOTIFICATION_ID_BASE =
            10_000
    }

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {

        if (
            intent.action !=
            ACTION_SHIFT_REMINDER
        ) {
            return
        }

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU
        ) {

            val permission =
                ContextCompat
                    .checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    )

            if (
                permission !=
                PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val shiftId =
            intent.getLongExtra(
                EXTRA_SHIFT_ID,
                -1L
            )

        if (shiftId <= 0L) {
            return
        }

        val companyName =
            intent.getStringExtra(
                EXTRA_COMPANY_NAME
            )
                ?: "your company"

        val shiftStart =
            intent.getLongExtra(
                EXTRA_SHIFT_START,
                0L
            )

        val timeText =
            if (shiftStart > 0L) {

                Instant
                    .ofEpochMilli(
                        shiftStart
                    )
                    .atZone(
                        ZoneId.systemDefault()
                    )
                    .format(
                        DateTimeFormatter.ofPattern(
                            "h:mm a"
                        )
                    )

            } else {

                null
            }

        val openAppIntent =
            Intent(
                context,
                MainActivity::class.java
            ).apply {

                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP

                putExtra(
                    EXTRA_SHIFT_ID,
                    shiftId
                )
            }

        val openAppPendingIntent =
            PendingIntent.getActivity(
                context,
                requestCodeForShift(
                    shiftId
                ),
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        val contentText =
            if (timeText != null) {

                "Your shift at $companyName starts at $timeText."

            } else {

                "Your shift at $companyName starts soon."
            }

        val notification =
            NotificationCompat
                .Builder(
                    context,
                    NotificationHelper
                        .SHIFT_REMINDER_CHANNEL_ID
                )
                .setSmallIcon(
                    R.drawable.ic_shift_notification
                )
                .setContentTitle(
                    "Shift starts soon"
                )
                .setContentText(
                    contentText
                )
                .setStyle(
                    NotificationCompat
                        .BigTextStyle()
                        .bigText(
                            contentText
                        )
                )
                .setPriority(
                    NotificationCompat
                        .PRIORITY_DEFAULT
                )
                .setAutoCancel(
                    true
                )
                .setContentIntent(
                    openAppPendingIntent
                )
                .build()

        NotificationManagerCompat
            .from(context)
            .notify(
                NOTIFICATION_ID_BASE +
                        requestCodeForShift(
                            shiftId
                        ),
                notification
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