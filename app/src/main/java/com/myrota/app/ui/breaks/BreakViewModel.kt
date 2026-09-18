package com.myrota.app.ui.breaks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.myrota.app.data.repository.BreakRepository
import com.myrota.app.data.repository.ShiftRepository
import com.myrota.app.local.BreakEntity
import com.myrota.app.local.ShiftStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BreakViewModel(
    private val repository: BreakRepository,
    private val shiftRepository: ShiftRepository
) : ViewModel() {

    fun observeBreaksForShift(
        shiftId: Long
    ): Flow<List<BreakEntity>> {

        return repository.observeBreaksForShift(
            shiftId
        )
    }

    fun addBreak(
        shiftId: Long,
        durationMinutes: Int,
        isPaid: Boolean,
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

            if (
                durationMinutes != 15 &&
                durationMinutes != 30 &&
                durationMinutes != 60
            ) {

                onError(
                    "Break duration must be 15, 30, or 60 minutes."
                )

                return@launch
            }

            try {

                val validationError =
                    validateBreakDuration(
                        shiftId = shiftId,
                        proposedBreakMinutes =
                            durationMinutes,
                        breakIdBeingEdited =
                            null
                    )

                if (
                    validationError != null
                ) {

                    onError(
                        validationError
                    )

                    return@launch
                }

                val now =
                    System.currentTimeMillis()

                repository.insertBreak(
                    BreakEntity(
                        shiftId =
                            shiftId,

                        durationMinutes =
                            durationMinutes,

                        isPaid =
                            isPaid,

                        createdAtEpochMillis =
                            now,

                        updatedAtEpochMillis =
                            now
                    )
                )

                onSuccess()

            } catch (
                exception: Exception
            ) {

                onError(
                    exception.message
                        ?: "Unable to add break."
                )
            }
        }
    }

    fun updateBreak(
        existingBreak: BreakEntity,
        durationMinutes: Int,
        isPaid: Boolean,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {

        viewModelScope.launch {

            if (
                existingBreak.id <= 0
            ) {

                onError(
                    "Invalid break."
                )

                return@launch
            }

            if (
                durationMinutes != 15 &&
                durationMinutes != 30 &&
                durationMinutes != 60
            ) {

                onError(
                    "Break duration must be 15, 30, or 60 minutes."
                )

                return@launch
            }

            try {

                val validationError =
                    validateBreakDuration(
                        shiftId =
                            existingBreak.shiftId,

                        proposedBreakMinutes =
                            durationMinutes,

                        breakIdBeingEdited =
                            existingBreak.id
                    )

                if (
                    validationError != null
                ) {

                    onError(
                        validationError
                    )

                    return@launch
                }

                repository.updateBreak(
                    existingBreak.copy(
                        durationMinutes =
                            durationMinutes,

                        isPaid =
                            isPaid,

                        updatedAtEpochMillis =
                            System.currentTimeMillis()
                    )
                )

                onSuccess()

            } catch (
                exception: Exception
            ) {

                onError(
                    exception.message
                        ?: "Unable to update break."
                )
            }
        }
    }

    fun deleteBreak(
        breakEntity: BreakEntity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {

        viewModelScope.launch {

            if (
                breakEntity.id <= 0
            ) {

                onError(
                    "Invalid break."
                )

                return@launch
            }

            try {

                /*
                 * Deleting a break is always safe
                 * because it reduces total break time.
                 */
                repository.deleteBreak(
                    breakEntity
                )

                onSuccess()

            } catch (
                exception: Exception
            ) {

                onError(
                    exception.message
                        ?: "Unable to delete break."
                )
            }
        }
    }

    fun deleteBreakById(
        breakId: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {

        viewModelScope.launch {

            if (
                breakId <= 0
            ) {

                onError(
                    "Invalid break."
                )

                return@launch
            }

            try {

                val rowsDeleted =
                    repository.deleteBreakById(
                        breakId
                    )

                if (
                    rowsDeleted == 1
                ) {

                    onSuccess()

                } else {

                    onError(
                        "Break could not be deleted."
                    )
                }

            } catch (
                exception: Exception
            ) {

                onError(
                    exception.message
                        ?: "Unable to delete break."
                )
            }
        }
    }

    fun getBreakTotals(
        shiftId: Long,
        onSuccess: (
            paidMinutes: Int,
            unpaidMinutes: Int
        ) -> Unit,
        onError: (String) -> Unit
    ) {

        viewModelScope.launch {

            if (
                shiftId <= 0
            ) {

                onError(
                    "Invalid shift."
                )

                return@launch
            }

            try {

                val paidMinutes =
                    repository
                        .getTotalPaidBreakMinutes(
                            shiftId
                        )

                val unpaidMinutes =
                    repository
                        .getTotalUnpaidBreakMinutes(
                            shiftId
                        )

                onSuccess(
                    paidMinutes,
                    unpaidMinutes
                )

            } catch (
                exception: Exception
            ) {

                onError(
                    exception.message
                        ?: "Unable to calculate break totals."
                )
            }
        }
    }

    private suspend fun validateBreakDuration(
        shiftId: Long,
        proposedBreakMinutes: Int,
        breakIdBeingEdited: Long?
    ): String? {

        val shift =
            shiftRepository
                .getShiftByIdOnce(
                    shiftId
                )
                ?: return "Shift could not be found."

        /*
         * For an IN_PROGRESS shift we do not yet
         * know the final worked duration, so fixed
         * breaks are allowed for now.
         */
        if (
            shift.status !=
            ShiftStatus.COMPLETED
        ) {

            return null
        }

        val actualStart =
            shift.actualStartEpochMillis
                ?: return "Completed shift does not have a clock-in time."

        val actualEnd =
            shift.actualEndEpochMillis
                ?: return "Completed shift does not have a clock-out time."

        if (
            actualEnd <=
            actualStart
        ) {

            return "Completed shift has an invalid worked duration."
        }

        val actualWorkedMinutes =
            (
                    actualEnd -
                            actualStart
                    ) / 60_000L

        val existingBreaks =
            repository
                .observeBreaksForShift(
                    shiftId
                )
                .first()

        val otherBreakMinutes =
            existingBreaks
                .filter { breakEntity ->

                    breakEntity.id !=
                            breakIdBeingEdited
                }
                .sumOf { breakEntity ->

                    breakEntity
                        .durationMinutes
                        .toLong()
                }

        val proposedTotalBreakMinutes =
            otherBreakMinutes +
                    proposedBreakMinutes

        if (
            proposedTotalBreakMinutes >
            actualWorkedMinutes
        ) {

            return buildString {

                append(
                    "Break time cannot exceed actual worked time. "
                )

                append(
                    "This shift was worked for "
                )

                append(
                    formatMinutes(
                        actualWorkedMinutes
                    )
                )

                append(
                    ", but the total breaks would be "
                )

                append(
                    formatMinutes(
                        proposedTotalBreakMinutes
                    )
                )

                append(".")
            }
        }

        return null
    }

    private fun formatMinutes(
        totalMinutes: Long
    ): String {

        val hours =
            totalMinutes / 60L

        val minutes =
            totalMinutes % 60L

        return when {

            hours > 0L &&
                    minutes > 0L ->

                "${hours}h ${minutes}m"

            hours > 0L ->

                "${hours}h"

            else ->

                "${minutes}m"
        }
    }
}

class BreakViewModelFactory(
    private val repository: BreakRepository,
    private val shiftRepository: ShiftRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (
            modelClass.isAssignableFrom(
                BreakViewModel::class.java
            )
        ) {

            return BreakViewModel(
                repository =
                    repository,

                shiftRepository =
                    shiftRepository
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}