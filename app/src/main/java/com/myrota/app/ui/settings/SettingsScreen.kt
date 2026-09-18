package com.myrota.app.ui.settings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.myrota.app.local.CompanyEntity
import com.myrota.app.ui.shifts.ShiftViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    shiftViewModel: ShiftViewModel,
    companies: List<CompanyEntity>,
    modifier: Modifier = Modifier
) {

    val settings by
    viewModel
        .settings
        .collectAsStateWithLifecycle()

    val context =
        LocalContext.current

    val notificationsAllowed =
        areNotificationsAllowed(
            context
        )

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(
                    16.dp
                )
    ) {

        Text(
            text =
                "Shift Reminders",

            style =
                MaterialTheme
                    .typography
                    .headlineSmall
        )

        Spacer(
            modifier =
                Modifier.height(
                    16.dp
                )
        )

        Card(
            modifier =
                Modifier.fillMaxWidth()
        ) {

            Column(
                modifier =
                    Modifier.padding(
                        16.dp
                    )
            ) {

                Text(
                    text =
                        "Reminder lead time",

                    style =
                        MaterialTheme
                            .typography
                            .titleMedium
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            6.dp
                        )
                )

                Text(
                    text =
                        "Choose how long before a shift MyRota should remind you.",

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            16.dp
                        )
                )

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.spacedBy(
                            8.dp
                        )
                ) {

                    listOf(
                        15,
                        30,
                        60
                    ).forEach { minutes ->

                        FilterChip(
                            selected =
                                settings
                                    .reminderLeadMinutes ==
                                        minutes,

                            onClick = {

                                if (
                                    settings
                                        .reminderLeadMinutes !=
                                    minutes
                                ) {

                                    /*
                                     * Persist the setting.
                                     */
                                    viewModel
                                        .setReminderLeadMinutes(
                                            minutes
                                        )

                                    /*
                                     * Immediately recreate
                                     * future shift alarms.
                                     */
                                    shiftViewModel
                                        .rescheduleFutureReminders(
                                            companies =
                                                companies,

                                            reminderLeadMinutes =
                                                minutes
                                        )
                                }
                            },

                            label = {

                                Text(
                                    "$minutes min"
                                )
                            },

                            modifier =
                                Modifier.weight(
                                    1f
                                )
                        )
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(
                            12.dp
                        )
                )

                Text(
                    text =
                        "Current setting: ${settings.reminderLeadMinutes} minutes before shift",

                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,

                    color =
                        MaterialTheme
                            .colorScheme
                            .primary
                )
            }
        }

        Spacer(
            modifier =
                Modifier.height(
                    16.dp
                )
        )

        Card(
            modifier =
                Modifier.fillMaxWidth()
        ) {

            Column(
                modifier =
                    Modifier.padding(
                        16.dp
                    )
            ) {

                Text(
                    text =
                        "Notification status",

                    style =
                        MaterialTheme
                            .typography
                            .titleMedium
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            8.dp
                        )
                )

                Text(
                    text =
                        if (
                            notificationsAllowed
                        ) {

                            "Notifications allowed"

                        } else {

                            "Notifications are not allowed"
                        },

                    color =
                        if (
                            notificationsAllowed
                        ) {

                            MaterialTheme
                                .colorScheme
                                .primary

                        } else {

                            MaterialTheme
                                .colorScheme
                                .error
                        }
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            6.dp
                        )
                )

                Text(
                    text =
                        if (
                            notificationsAllowed
                        ) {

                            "MyRota can show shift reminders."

                        } else {

                            "Enable notifications for MyRota in Android Settings to receive shift reminders."
                        },

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )
            }
        }

        Spacer(
            modifier =
                Modifier.height(
                    16.dp
                )
        )

        Card(
            modifier =
                Modifier.fillMaxWidth()
        ) {

            Column(
                modifier =
                    Modifier.padding(
                        16.dp
                    )
            ) {

                Text(
                    text =
                        "About",

                    style =
                        MaterialTheme
                            .typography
                            .titleMedium
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            8.dp
                        )
                )

                Text(
                    text =
                        "MyRota",

                    style =
                        MaterialTheme
                            .typography
                            .bodyLarge
                )

                Text(
                    text =
                        "Offline shift, break, rota and earnings tracker.",

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )
            }
        }
    }
}

private fun areNotificationsAllowed(
    context: Context
): Boolean {

    return if (
        Build.VERSION.SDK_INT >=
        Build.VERSION_CODES.TIRAMISU
    ) {

        ContextCompat
            .checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) ==
                PackageManager.PERMISSION_GRANTED

    } else {

        true
    }
}