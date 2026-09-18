package com.myrota.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.myrota.app.calculation.ShiftCalculation
import com.myrota.app.local.CompanyEntity
import com.myrota.app.local.ShiftEntity
import com.myrota.app.local.ShiftStatus
import com.myrota.app.ui.breaks.BreakViewModel
import com.myrota.app.ui.shifts.ShiftViewModel
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

@Composable
fun DashboardScreen(
    shiftViewModel: ShiftViewModel,
    breakViewModel: BreakViewModel,
    companies: List<CompanyEntity>,
    onCompaniesClick: () -> Unit,
    onAddShiftClick: () -> Unit,
    onViewShiftsClick: () -> Unit,
    onShiftClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {

    val shifts by
    shiftViewModel
        .allShifts
        .collectAsStateWithLifecycle()

    val zoneId =
        ZoneId.systemDefault()

    val today =
        LocalDate.now()

    val currentWeekStart =
        today.with(
            TemporalAdjusters.previousOrSame(
                DayOfWeek.MONDAY
            )
        )

    val currentWeekEnd =
        currentWeekStart.plusDays(6)

    val currentMonth =
        YearMonth.now()

    val now =
        System.currentTimeMillis()

    val activeShifts =
        shifts
            .filter { shift ->
                shift.status != ShiftStatus.CANCELLED
            }
            .sortedBy {
                it.scheduledStartEpochMillis
            }

    val todayShifts =
        activeShifts.filter { shift ->

            val shiftDate =
                Instant
                    .ofEpochMilli(
                        shift.scheduledStartEpochMillis
                    )
                    .atZone(zoneId)
                    .toLocalDate()

            shiftDate == today
        }

    val inProgressShift =
        activeShifts.firstOrNull { shift ->
            shift.status == ShiftStatus.IN_PROGRESS
        }

    val nextShift =
        activeShifts.firstOrNull { shift ->

            shift.status == ShiftStatus.SCHEDULED &&
                    shift.scheduledStartEpochMillis >= now
        }

    val todayScheduledMinutes =
        todayShifts.sumOf { shift ->

            (
                    shift.scheduledEndEpochMillis -
                            shift.scheduledStartEpochMillis
                    )
                .coerceAtLeast(0L) /
                    60_000L
        }

    var shiftToStart by remember {
        mutableStateOf<ShiftEntity?>(null)
    }

    var shiftToEnd by remember {
        mutableStateOf<ShiftEntity?>(null)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    val completedWeekShifts =
        shifts.filter { shift ->

            if (
                shift.status !=
                ShiftStatus.COMPLETED
            ) {
                false
            } else {

                val date =
                    Instant
                        .ofEpochMilli(
                            shift.scheduledStartEpochMillis
                        )
                        .atZone(zoneId)
                        .toLocalDate()

                !date.isBefore(currentWeekStart) &&
                        !date.isAfter(currentWeekEnd)
            }
        }

    val completedMonthShifts =
        shifts.filter { shift ->

            if (
                shift.status !=
                ShiftStatus.COMPLETED
            ) {
                false
            } else {

                val date =
                    Instant
                        .ofEpochMilli(
                            shift.scheduledStartEpochMillis
                        )
                        .atZone(zoneId)
                        .toLocalDate()

                YearMonth.from(date) ==
                        currentMonth
            }
        }

    /*
     * IMPORTANT:
     *
     * These are normal local variables.
     * They are recalculated from zero every recomposition.
     *
     * Do NOT use remember/mutableStateOf here.
     */
    var weeklyPaidMinutes = 0L

    var monthlyEarningsPence = 0L

    completedWeekShifts.forEach { shift ->

        val breaks by
        breakViewModel
            .observeBreaksForShift(
                shift.id
            )
            .collectAsStateWithLifecycle(
                initialValue = emptyList()
            )

        val company =
            companies.firstOrNull {
                it.id == shift.companyId
            }

        val calculation =
            ShiftCalculation.calculate(
                shift = shift,
                breaks = breaks,
                hourlyRatePence =
                    company?.hourlyRatePence
            )

        weeklyPaidMinutes +=
            calculation.paidWorkedMinutes
                ?: 0L
    }

    completedMonthShifts.forEach { shift ->

        val breaks by
        breakViewModel
            .observeBreaksForShift(
                shift.id
            )
            .collectAsStateWithLifecycle(
                initialValue = emptyList()
            )

        val company =
            companies.firstOrNull {
                it.id == shift.companyId
            }

        val calculation =
            ShiftCalculation.calculate(
                shift = shift,
                breaks = breaks,
                hourlyRatePence =
                    company?.hourlyRatePence
            )

        monthlyEarningsPence +=
            calculation.estimatedEarningsPence
                ?: 0L
    }

    LazyColumn(
        modifier =
            modifier
                .fillMaxSize()
                .padding(
                    horizontal = 16.dp
                ),

        verticalArrangement =
            Arrangement.spacedBy(
                12.dp
            )
    ) {

        item {

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            Text(
                text = "Today",

                style =
                    MaterialTheme
                        .typography
                        .headlineMedium
            )

            Text(
                text =
                    today.format(
                        DateTimeFormatter.ofPattern(
                            "EEEE, dd MMMM yyyy"
                        )
                    ),

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )

            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )

            DashboardSummaryCard(
                todayShiftCount =
                    todayShifts.size,

                scheduledMinutes =
                    todayScheduledMinutes
            )

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            HomeStatisticsCard(
                weeklyPaidMinutes =
                    weeklyPaidMinutes,

                monthlyEarningsPence =
                    monthlyEarningsPence
            )
        }

        if (
            inProgressShift != null
        ) {

            item {

                SectionTitle(
                    text = "Working Now"
                )

                DashboardShiftCard(
                    shift =
                        inProgressShift,

                    company =
                        companies
                            .firstOrNull {
                                it.id ==
                                        inProgressShift.companyId
                            },

                    actionText =
                        "End Shift",

                    onAction = {
                        shiftToEnd =
                            inProgressShift
                    },

                    onClick = {
                        onShiftClick(
                            inProgressShift.id
                        )
                    }
                )
            }

        } else if (
            nextShift != null
        ) {

            item {

                SectionTitle(
                    text = "Next Shift"
                )

                DashboardShiftCard(
                    shift =
                        nextShift,

                    company =
                        companies
                            .firstOrNull {
                                it.id ==
                                        nextShift.companyId
                            },

                    actionText =
                        "Start Shift",

                    onAction = {
                        shiftToStart =
                            nextShift
                    },

                    onClick = {
                        onShiftClick(
                            nextShift.id
                        )
                    }
                )
            }
        }

        item {

            SectionTitle(
                text = "Today's Shifts"
            )
        }

        if (
            todayShifts.isEmpty()
        ) {

            item {

                Card(
                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Column(
                        modifier =
                            Modifier.padding(
                                18.dp
                            )
                    ) {

                        Text(
                            text =
                                "No shifts today",

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
                                "You don't have any shifts scheduled for today.",

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant
                        )
                    }
                }
            }

        } else {

            items(
                items =
                    todayShifts,

                key = {
                    it.id
                }
            ) { shift ->

                val company =
                    companies
                        .firstOrNull {
                            it.id ==
                                    shift.companyId
                        }

                DashboardShiftCard(
                    shift =
                        shift,

                    company =
                        company,

                    actionText =
                        when (
                            shift.status
                        ) {

                            ShiftStatus.SCHEDULED ->
                                "Start Shift"

                            ShiftStatus.IN_PROGRESS ->
                                "End Shift"

                            else ->
                                null
                        },

                    onAction = {

                        when (
                            shift.status
                        ) {

                            ShiftStatus.SCHEDULED ->

                                shiftToStart =
                                    shift

                            ShiftStatus.IN_PROGRESS ->

                                shiftToEnd =
                                    shift

                            else ->

                                Unit
                        }
                    },

                    onClick = {

                        onShiftClick(
                            shift.id
                        )
                    }
                )
            }
        }

        item {

            Spacer(
                modifier =
                    Modifier.height(
                        8.dp
                    )
            )

            SectionTitle(
                text =
                    "Quick Actions"
            )

            Button(
                onClick =
                    onAddShiftClick,

                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Text(
                    "Add Shift"
                )
            }

            Spacer(
                modifier =
                    Modifier.height(
                        8.dp
                    )
            )

            OutlinedButton(
                onClick =
                    onViewShiftsClick,

                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Text(
                    "View All Shifts"
                )
            }

            Spacer(
                modifier =
                    Modifier.height(
                        8.dp
                    )
            )

            OutlinedButton(
                onClick =
                    onCompaniesClick,

                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Text(
                    "Manage Companies"
                )
            }

            Spacer(
                modifier =
                    Modifier.height(
                        24.dp
                    )
            )
        }
    }

    shiftToStart?.let { shift ->

        val companyName =
            companies
                .firstOrNull {
                    it.id ==
                            shift.companyId
                }
                ?.name
                ?: "this company"

        AlertDialog(
            onDismissRequest = {
                shiftToStart =
                    null
            },

            title = {
                Text(
                    "Start shift?"
                )
            },

            text = {
                Text(
                    "Clock in to your shift at $companyName now?"
                )
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        errorMessage =
                            null

                        shiftViewModel.startShift(
                            shiftId =
                                shift.id,

                            onSuccess = {
                                shiftToStart =
                                    null
                            },

                            onError = { message ->

                                shiftToStart =
                                    null

                                errorMessage =
                                    message
                            }
                        )
                    }
                ) {

                    Text(
                        "Start Shift"
                    )
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        shiftToStart =
                            null
                    }
                ) {

                    Text(
                        "Cancel"
                    )
                }
            }
        )
    }

    shiftToEnd?.let { shift ->

        val companyName =
            companies
                .firstOrNull {
                    it.id ==
                            shift.companyId
                }
                ?.name
                ?: "this company"

        AlertDialog(
            onDismissRequest = {
                shiftToEnd =
                    null
            },

            title = {
                Text(
                    "End shift?"
                )
            },

            text = {
                Text(
                    "Clock out from your shift at $companyName now?"
                )
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        errorMessage =
                            null

                        shiftViewModel.endShift(
                            shift =
                                shift,

                            onSuccess = {
                                shiftToEnd =
                                    null
                            },

                            onError = { message ->

                                shiftToEnd =
                                    null

                                errorMessage =
                                    message
                            }
                        )
                    }
                ) {

                    Text(
                        "End Shift"
                    )
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        shiftToEnd =
                            null
                    }
                ) {

                    Text(
                        "Keep Working"
                    )
                }
            }
        )
    }

    errorMessage?.let { message ->

        AlertDialog(
            onDismissRequest = {
                errorMessage =
                    null
            },

            title = {
                Text(
                    "Shift action failed"
                )
            },

            text = {
                Text(
                    message
                )
            },

            confirmButton = {

                TextButton(
                    onClick = {
                        errorMessage =
                            null
                    }
                ) {

                    Text(
                        "OK"
                    )
                }
            }
        )
    }
}

@Composable
private fun DashboardSummaryCard(
    todayShiftCount: Int,
    scheduledMinutes: Long
) {

    Card(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        18.dp
                    ),

            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {

            Column {

                Text(
                    text =
                        "Today's shifts",

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            4.dp
                        )
                )

                Text(
                    text =
                        todayShiftCount
                            .toString(),

                    style =
                        MaterialTheme
                            .typography
                            .headlineSmall
                )
            }

            Column(
                horizontalAlignment =
                    Alignment.End
            ) {

                Text(
                    text =
                        "Scheduled",

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            4.dp
                        )
                )

                Text(
                    text =
                        ShiftCalculation
                            .formatMinutes(
                                scheduledMinutes
                            ),

                    style =
                        MaterialTheme
                            .typography
                            .headlineSmall
                )
            }
        }
    }
}

@Composable
private fun HomeStatisticsCard(
    weeklyPaidMinutes: Long,
    monthlyEarningsPence: Long
) {

    Card(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        18.dp
                    ),

            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {

            Column {

                Text(
                    text =
                        "This Week",

                    style =
                        MaterialTheme
                            .typography
                            .labelLarge,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            6.dp
                        )
                )

                Text(
                    text =
                        "Paid work",

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )

                Text(
                    text =
                        ShiftCalculation
                            .formatMinutes(
                                weeklyPaidMinutes
                            ),

                    style =
                        MaterialTheme
                            .typography
                            .headlineSmall
                )
            }

            Column(
                horizontalAlignment =
                    Alignment.End
            ) {

                Text(
                    text =
                        "This Month",

                    style =
                        MaterialTheme
                            .typography
                            .labelLarge,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            6.dp
                        )
                )

                Text(
                    text =
                        "Earnings",

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )

                Text(
                    text =
                        ShiftCalculation
                            .formatCurrency(
                                monthlyEarningsPence
                            ),

                    style =
                        MaterialTheme
                            .typography
                            .headlineSmall,

                    color =
                        MaterialTheme
                            .colorScheme
                            .primary
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(
    text: String
) {

    Text(
        text =
            text,

        style =
            MaterialTheme
                .typography
                .titleLarge
    )
}

@Composable
private fun DashboardShiftCard(
    shift: ShiftEntity,
    company: CompanyEntity?,
    actionText: String?,
    onAction: () -> Unit,
    onClick: () -> Unit
) {

    val zoneId =
        ZoneId.systemDefault()

    val timeFormatter =
        DateTimeFormatter.ofPattern(
            "h:mm a"
        )

    val startDateTime =
        Instant
            .ofEpochMilli(
                shift.scheduledStartEpochMillis
            )
            .atZone(
                zoneId
            )

    val endDateTime =
        Instant
            .ofEpochMilli(
                shift.scheduledEndEpochMillis
            )
            .atZone(
                zoneId
            )

    val startTime =
        startDateTime.format(
            timeFormatter
        )

    val endTime =
        endDateTime.format(
            timeFormatter
        )

    val isOvernight =
        startDateTime
            .toLocalDate() !=
                endDateTime
                    .toLocalDate()

    val companyColor =
        company?.let {

            Color(
                it.colorArgb
            )

        } ?: MaterialTheme
            .colorScheme
            .primary

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable {
                    onClick()
                }
    ) {

        Column(
            modifier =
                Modifier.padding(
                    16.dp
                )
        ) {

            Row(
                verticalAlignment =
                    Alignment.CenterVertically,

                horizontalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    )
            ) {

                Spacer(
                    modifier =
                        Modifier
                            .size(
                                10.dp
                            )
                            .clip(
                                CircleShape
                            )
                            .background(
                                companyColor
                            )
                )

                Text(
                    text =
                        company?.name
                            ?: "Unknown company",

                    style =
                        MaterialTheme
                            .typography
                            .titleMedium
                )
            }

            Spacer(
                modifier =
                    Modifier.height(
                        8.dp
                    )
            )

            Text(
                text =
                    "$startTime – $endTime",

                style =
                    MaterialTheme
                        .typography
                        .bodyLarge
            )

            if (isOvernight) {

                Text(
                    text =
                        "Ends the following day",

                    color =
                        MaterialTheme
                            .colorScheme
                            .primary,

                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium
                )
            }

            Spacer(
                modifier =
                    Modifier.height(
                        6.dp
                    )
            )

            Text(
                text =
                    when (
                        shift.status
                    ) {

                        ShiftStatus.SCHEDULED ->
                            "SCHEDULED"

                        ShiftStatus.IN_PROGRESS ->
                            "IN PROGRESS"

                        ShiftStatus.COMPLETED ->
                            "COMPLETED"

                        ShiftStatus.CANCELLED ->
                            "CANCELLED"
                    },

                style =
                    MaterialTheme
                        .typography
                        .labelMedium,

                color =
                    when (
                        shift.status
                    ) {

                        ShiftStatus.IN_PROGRESS ->
                            MaterialTheme
                                .colorScheme
                                .primary

                        ShiftStatus.COMPLETED ->
                            MaterialTheme
                                .colorScheme
                                .tertiary

                        else ->
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    }
            )

            if (
                actionText != null
            ) {

                Spacer(
                    modifier =
                        Modifier.height(
                            12.dp
                        )
                )

                Button(
                    onClick =
                        onAction,

                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Text(
                        actionText
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(
                        6.dp
                    )
            )

            Text(
                text =
                    "Tap card to edit",

                style =
                    MaterialTheme
                        .typography
                        .labelSmall,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }
    }
}