package com.myrota.app.ui.shifts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.myrota.app.data.repository.ShiftRepository
import com.myrota.app.data.settings.SettingsRepository
import com.myrota.app.local.CompanyEntity
import com.myrota.app.local.ShiftEntity
import com.myrota.app.local.ShiftStatus
import com.myrota.app.notifications.ShiftReminderScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class ShiftViewModel(
    private val repository: ShiftRepository,
    private val reminderScheduler: ShiftReminderScheduler,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val allShifts: StateFlow<List<ShiftEntity>> =
        repository
            .observeAllShifts()
            .stateIn(
                scope = viewModelScope,
                started =
                    SharingStarted.WhileSubscribed(
                        5_000
                    ),
                initialValue =
                    emptyList()
            )

    fun observeShiftById(
        shiftId: Long
    ): Flow<ShiftEntity?> {

        return repository.observeShiftById(
            shiftId
        )
    }

    fun createShift(
        companyId: Long,
        companyName: String,
        notificationsEnabled: Boolean,
        date: LocalDate,
        startTime: LocalTime,
        endTime: LocalTime,
        allowOverlap: Boolean,
        onOverlapFound: (List<ShiftEntity>) -> Unit,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {

        viewModelScope.launch {

            if (companyId <= 0) {

                onError(
                    "Select a company."
                )

                return@launch
            }

            try {

                val zoneId =
                    ZoneId.systemDefault()

                val startDateTime =
                    LocalDateTime.of(
                        date,
                        startTime
                    )

                val endDate =
                    if (
                        endTime <=
                        startTime
                    ) {

                        date.plusDays(1)

                    } else {

                        date
                    }

                val endDateTime =
                    LocalDateTime.of(
                        endDate,
                        endTime
                    )

                val startMillis =
                    startDateTime
                        .atZone(zoneId)
                        .toInstant()
                        .toEpochMilli()

                val endMillis =
                    endDateTime
                        .atZone(zoneId)
                        .toInstant()
                        .toEpochMilli()

                if (
                    endMillis <=
                    startMillis
                ) {

                    onError(
                        "Shift end must be after shift start."
                    )

                    return@launch
                }

                if (!allowOverlap) {

                    val overlaps =
                        repository
                            .findOverlappingShifts(
                                startMillis =
                                    startMillis,

                                endMillis =
                                    endMillis
                            )

                    if (
                        overlaps.isNotEmpty()
                    ) {

                        onOverlapFound(
                            overlaps
                        )

                        return@launch
                    }
                }

                val now =
                    System.currentTimeMillis()

                val shiftId =
                    repository.insertShift(
                        ShiftEntity(
                            companyId =
                                companyId,

                            scheduledStartEpochMillis =
                                startMillis,

                            scheduledEndEpochMillis =
                                endMillis,

                            actualStartEpochMillis =
                                null,

                            actualEndEpochMillis =
                                null,

                            notes =
                                null,

                            status =
                                ShiftStatus.SCHEDULED,

                            createdAtEpochMillis =
                                now,

                            updatedAtEpochMillis =
                                now
                        )
                    )

                val settings =
                    settingsRepository
                        .settings
                        .first()

                reminderScheduler
                    .scheduleReminder(
                        shiftId =
                            shiftId,

                        companyName =
                            companyName,

                        scheduledStartEpochMillis =
                            startMillis,

                        notificationsEnabled =
                            notificationsEnabled,

                        reminderLeadMinutes =
                            settings.reminderLeadMinutes
                    )

                onSuccess()

            } catch (
                exception: Exception
            ) {

                onError(
                    exception.message
                        ?: "Unable to create shift."
                )
            }
        }
    }

    fun updateShift(
        existingShift: ShiftEntity,
        companyId: Long,
        companyName: String,
        notificationsEnabled: Boolean,
        date: LocalDate,
        startTime: LocalTime,
        endTime: LocalTime,
        allowOverlap: Boolean,
        onOverlapFound: (List<ShiftEntity>) -> Unit,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {

        viewModelScope.launch {

            if (companyId <= 0) {

                onError(
                    "Select a company."
                )

                return@launch
            }

            try {

                val zoneId =
                    ZoneId.systemDefault()

                val startDateTime =
                    LocalDateTime.of(
                        date,
                        startTime
                    )

                val endDate =
                    if (
                        endTime <=
                        startTime
                    ) {

                        date.plusDays(1)

                    } else {

                        date
                    }

                val endDateTime =
                    LocalDateTime.of(
                        endDate,
                        endTime
                    )

                val startMillis =
                    startDateTime
                        .atZone(zoneId)
                        .toInstant()
                        .toEpochMilli()

                val endMillis =
                    endDateTime
                        .atZone(zoneId)
                        .toInstant()
                        .toEpochMilli()

                if (
                    endMillis <=
                    startMillis
                ) {

                    onError(
                        "Shift end must be after shift start."
                    )

                    return@launch
                }

                if (!allowOverlap) {

                    val overlaps =
                        repository
                            .findOverlappingShiftsExcluding(
                                shiftId =
                                    existingShift.id,

                                startMillis =
                                    startMillis,

                                endMillis =
                                    endMillis
                            )

                    if (
                        overlaps.isNotEmpty()
                    ) {

                        onOverlapFound(
                            overlaps
                        )

                        return@launch
                    }
                }

                val updatedShift =
                    existingShift.copy(
                        companyId =
                            companyId,

                        scheduledStartEpochMillis =
                            startMillis,

                        scheduledEndEpochMillis =
                            endMillis,

                        updatedAtEpochMillis =
                            System.currentTimeMillis()
                    )

                repository.updateShift(
                    updatedShift
                )

                reminderScheduler.cancelReminder(
                    existingShift.id
                )

                if (
                    updatedShift.status ==
                    ShiftStatus.SCHEDULED
                ) {

                    val settings =
                        settingsRepository
                            .settings
                            .first()

                    reminderScheduler
                        .scheduleReminder(
                            shiftId =
                                updatedShift.id,

                            companyName =
                                companyName,

                            scheduledStartEpochMillis =
                                updatedShift
                                    .scheduledStartEpochMillis,

                            notificationsEnabled =
                                notificationsEnabled,

                            reminderLeadMinutes =
                                settings.reminderLeadMinutes
                        )
                }

                onSuccess()

            } catch (
                exception: Exception
            ) {

                onError(
                    exception.message
                        ?: "Unable to update shift."
                )
            }
        }
    }

    fun cancelShift(
        shiftId: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {

        viewModelScope.launch {

            if (shiftId <= 0) {

                onError(
                    "Invalid shift."
                )

                return@launch
            }

            try {

                repository.cancelShift(
                    shiftId =
                        shiftId,

                    updatedAt =
                        System.currentTimeMillis()
                )

                reminderScheduler.cancelReminder(
                    shiftId
                )

                onSuccess()

            } catch (
                exception: Exception
            ) {

                onError(
                    exception.message
                        ?: "Unable to cancel shift."
                )
            }
        }
    }

    fun startShift(
        shiftId: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {

        viewModelScope.launch {

            if (shiftId <= 0) {

                onError(
                    "Invalid shift."
                )

                return@launch
            }

            try {

                val now =
                    System.currentTimeMillis()

                val rowsUpdated =
                    repository.startShift(
                        shiftId =
                            shiftId,

                        actualStart =
                            now,

                        updatedAt =
                            now
                    )

                if (
                    rowsUpdated == 1
                ) {

                    reminderScheduler
                        .cancelReminder(
                            shiftId
                        )

                    onSuccess()

                } else {

                    onError(
                        "This shift cannot be started."
                    )
                }

            } catch (
                exception: Exception
            ) {

                onError(
                    exception.message
                        ?: "Unable to start shift."
                )
            }
        }
    }

    fun endShift(
        shift: ShiftEntity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {

        viewModelScope.launch {

            if (shift.id <= 0) {

                onError(
                    "Invalid shift."
                )

                return@launch
            }

            if (
                shift.status !=
                ShiftStatus.IN_PROGRESS
            ) {

                onError(
                    "Only an in-progress shift can be ended."
                )

                return@launch
            }

            val actualStart =
                shift.actualStartEpochMillis

            if (
                actualStart == null
            ) {

                onError(
                    "This shift does not have a clock-in time."
                )

                return@launch
            }

            try {

                val now =
                    System.currentTimeMillis()

                if (
                    now <=
                    actualStart
                ) {

                    onError(
                        "Clock-out time must be after clock-in time."
                    )

                    return@launch
                }

                val rowsUpdated =
                    repository.endShift(
                        shiftId =
                            shift.id,

                        actualEnd =
                            now,

                        updatedAt =
                            now
                    )

                if (
                    rowsUpdated == 1
                ) {

                    reminderScheduler
                        .cancelReminder(
                            shift.id
                        )

                    onSuccess()

                } else {

                    onError(
                        "This shift cannot be ended."
                    )
                }

            } catch (
                exception: Exception
            ) {

                onError(
                    exception.message
                        ?: "Unable to end shift."
                )
            }
        }
    }

    /*
     * Called when the user changes the global
     * reminder lead time in Settings.
     *
     * All future SCHEDULED shift alarms are
     * cancelled and recreated using the new value.
     */
    fun rescheduleFutureReminders(
        companies: List<CompanyEntity>,
        reminderLeadMinutes: Int
    ) {

        viewModelScope.launch {

            try {

                val now =
                    System.currentTimeMillis()

                val shifts =
                    repository
                        .observeAllShifts()
                        .first()

                val futureScheduledShifts =
                    shifts.filter { shift ->

                        shift.status ==
                                ShiftStatus.SCHEDULED &&
                                shift.scheduledStartEpochMillis >
                                now
                    }

                futureScheduledShifts
                    .forEach { shift ->

                        val company =
                            companies
                                .firstOrNull {
                                    it.id ==
                                            shift.companyId
                                }

                        /*
                         * Always remove the previous
                         * alarm first.
                         */
                        reminderScheduler
                            .cancelReminder(
                                shift.id
                            )

                        /*
                         * If the company still exists
                         * in the active company list,
                         * schedule according to its
                         * notification setting.
                         */
                        if (
                            company != null
                        ) {

                            reminderScheduler
                                .scheduleReminder(
                                    shiftId =
                                        shift.id,

                                    companyName =
                                        company.name,

                                    scheduledStartEpochMillis =
                                        shift
                                            .scheduledStartEpochMillis,

                                    notificationsEnabled =
                                        company
                                            .notificationsEnabled,

                                    reminderLeadMinutes =
                                        reminderLeadMinutes
                                )
                        }
                    }

            } catch (
                exception: Exception
            ) {

                /*
                 * Do not crash Settings if Android
                 * cannot recreate an alarm.
                 *
                 * Newly created/edited shifts will
                 * still use the latest saved setting.
                 */
            }
        }
    }
}

class ShiftViewModelFactory(
    private val repository: ShiftRepository,
    private val reminderScheduler:
    ShiftReminderScheduler,
    private val settingsRepository:
    SettingsRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (
            modelClass.isAssignableFrom(
                ShiftViewModel::class.java
            )
        ) {

            return ShiftViewModel(
                repository =
                    repository,

                reminderScheduler =
                    reminderScheduler,

                settingsRepository =
                    settingsRepository
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}