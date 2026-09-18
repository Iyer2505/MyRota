package com.myrota.app.calculation

import com.myrota.app.local.BreakEntity
import com.myrota.app.local.ShiftEntity
import com.myrota.app.local.ShiftStatus

data class ShiftCalculationResult(
    val scheduledMinutes: Long,
    val actualMinutes: Long?,
    val paidBreakMinutes: Long,
    val unpaidBreakMinutes: Long,
    val paidWorkedMinutes: Long?,
    val estimatedEarningsPence: Long?,
    val hasInvalidBreakData: Boolean
)

object ShiftCalculation {

    fun calculate(
        shift: ShiftEntity,
        breaks: List<BreakEntity>,
        hourlyRatePence: Long?
    ): ShiftCalculationResult {

        val scheduledMinutes =
            calculateDurationMinutes(
                startMillis =
                    shift.scheduledStartEpochMillis,

                endMillis =
                    shift.scheduledEndEpochMillis
            )

        val rawPaidBreakMinutes =
            breaks
                .filter { breakEntity ->
                    breakEntity.isPaid
                }
                .sumOf { breakEntity ->
                    breakEntity.durationMinutes.toLong()
                }

        val rawUnpaidBreakMinutes =
            breaks
                .filter { breakEntity ->
                    !breakEntity.isPaid
                }
                .sumOf { breakEntity ->
                    breakEntity.durationMinutes.toLong()
                }

        val rawTotalBreakMinutes =
            rawPaidBreakMinutes +
                    rawUnpaidBreakMinutes

        val actualMinutes =
            calculateActualMinutes(
                shift = shift
            )

        val hasInvalidBreakData =
            actualMinutes != null &&
                    rawTotalBreakMinutes >
                    actualMinutes

        val adjustedBreaks =
            adjustBreakMinutes(
                paidBreakMinutes =
                    rawPaidBreakMinutes,

                unpaidBreakMinutes =
                    rawUnpaidBreakMinutes,

                actualMinutes =
                    actualMinutes
            )

        val paidWorkedMinutes =
            actualMinutes?.let { minutes ->

                (
                        minutes -
                                adjustedBreaks
                                    .unpaidBreakMinutes
                        )
                    .coerceAtLeast(0L)
            }

        val estimatedEarningsPence =
            calculateEarningsPence(
                paidWorkedMinutes =
                    paidWorkedMinutes,

                hourlyRatePence =
                    hourlyRatePence
            )

        return ShiftCalculationResult(
            scheduledMinutes =
                scheduledMinutes,

            actualMinutes =
                actualMinutes,

            paidBreakMinutes =
                adjustedBreaks
                    .paidBreakMinutes,

            unpaidBreakMinutes =
                adjustedBreaks
                    .unpaidBreakMinutes,

            paidWorkedMinutes =
                paidWorkedMinutes,

            estimatedEarningsPence =
                estimatedEarningsPence,

            hasInvalidBreakData =
                hasInvalidBreakData
        )
    }

    private data class AdjustedBreaks(
        val paidBreakMinutes: Long,
        val unpaidBreakMinutes: Long
    )

    private fun adjustBreakMinutes(
        paidBreakMinutes: Long,
        unpaidBreakMinutes: Long,
        actualMinutes: Long?
    ): AdjustedBreaks {

        if (actualMinutes == null) {

            return AdjustedBreaks(
                paidBreakMinutes =
                    paidBreakMinutes,

                unpaidBreakMinutes =
                    unpaidBreakMinutes
            )
        }

        if (actualMinutes <= 0L) {

            return AdjustedBreaks(
                paidBreakMinutes = 0L,
                unpaidBreakMinutes = 0L
            )
        }

        val safeUnpaidBreakMinutes =
            unpaidBreakMinutes
                .coerceAtMost(
                    actualMinutes
                )

        val remainingMinutes =
            (
                    actualMinutes -
                            safeUnpaidBreakMinutes
                    )
                .coerceAtLeast(0L)

        val safePaidBreakMinutes =
            paidBreakMinutes
                .coerceAtMost(
                    remainingMinutes
                )

        return AdjustedBreaks(
            paidBreakMinutes =
                safePaidBreakMinutes,

            unpaidBreakMinutes =
                safeUnpaidBreakMinutes
        )
    }

    private fun calculateActualMinutes(
        shift: ShiftEntity
    ): Long? {

        if (
            shift.status !=
            ShiftStatus.COMPLETED
        ) {
            return null
        }

        val actualStart =
            shift.actualStartEpochMillis
                ?: return null

        val actualEnd =
            shift.actualEndEpochMillis
                ?: return null

        if (
            actualEnd <
            actualStart
        ) {
            return null
        }

        return calculateDurationMinutes(
            startMillis =
                actualStart,

            endMillis =
                actualEnd
        )
    }

    private fun calculateDurationMinutes(
        startMillis: Long,
        endMillis: Long
    ): Long {

        if (
            endMillis <=
            startMillis
        ) {
            return 0L
        }

        return (
                endMillis -
                        startMillis
                ) / 60_000L
    }

    private fun calculateEarningsPence(
        paidWorkedMinutes: Long?,
        hourlyRatePence: Long?
    ): Long? {

        if (
            paidWorkedMinutes == null ||
            hourlyRatePence == null
        ) {
            return null
        }

        if (
            paidWorkedMinutes <= 0L ||
            hourlyRatePence <= 0L
        ) {
            return 0L
        }

        val numerator =
            paidWorkedMinutes *
                    hourlyRatePence

        return (
                numerator + 30L
                ) / 60L
    }

    fun formatMinutes(
        totalMinutes: Long
    ): String {

        val safeMinutes =
            totalMinutes
                .coerceAtLeast(0L)

        val hours =
            safeMinutes / 60L

        val minutes =
            safeMinutes % 60L

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

    fun formatCurrency(
        amountPence: Long
    ): String {

        val safeAmount =
            amountPence
                .coerceAtLeast(0L)

        val pounds =
            safeAmount / 100L

        val pence =
            safeAmount % 100L

        return "£$pounds.${pence.toString().padStart(2, '0')}"
    }
}