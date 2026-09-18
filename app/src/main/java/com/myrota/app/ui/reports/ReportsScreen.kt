package com.myrota.app.ui.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.myrota.app.calculation.ShiftCalculation
import com.myrota.app.local.CompanyEntity
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ReportViewModel,
    companies: List<CompanyEntity>,
    modifier: Modifier = Modifier
) {

    val reportMode by
    viewModel
        .reportMode
        .collectAsStateWithLifecycle()

    val selectedDate by
    viewModel
        .selectedDate
        .collectAsStateWithLifecycle()

    val selectedWeekStart by
    viewModel
        .selectedWeekStart
        .collectAsStateWithLifecycle()

    val selectedMonth by
    viewModel
        .selectedMonth
        .collectAsStateWithLifecycle()

    val dailyItems by
    viewModel
        .dailyReport
        .collectAsStateWithLifecycle()

    val weeklyItems by
    viewModel
        .weeklyReport
        .collectAsStateWithLifecycle()

    val monthlyItems by
    viewModel
        .monthlyReport
        .collectAsStateWithLifecycle()

    var showDatePicker by remember {
        mutableStateOf(false)
    }

    val dateFormatter =
        remember {
            DateTimeFormatter.ofPattern(
                "dd MMM yyyy"
            )
        }

    val shortDateFormatter =
        remember {
            DateTimeFormatter.ofPattern(
                "dd MMM"
            )
        }

    val monthFormatter =
        remember {
            DateTimeFormatter.ofPattern(
                "MMMM yyyy"
            )
        }

    val reportItems =
        when (reportMode) {

            ReportMode.DAILY ->
                dailyItems

            ReportMode.WEEKLY ->
                weeklyItems

            ReportMode.MONTHLY ->
                monthlyItems
        }

    val summary =
        calculateSummary(
            reportItems = reportItems,
            companies = companies
        )

    LazyColumn(
        modifier =
            modifier.fillMaxSize(),

        verticalArrangement =
            Arrangement.spacedBy(
                12.dp
            )
    ) {

        item {

            Column(
                modifier =
                    Modifier.padding(
                        horizontal = 16.dp
                    )
            ) {

                Spacer(
                    modifier =
                        Modifier.height(8.dp)
                )

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.spacedBy(
                            8.dp
                        )
                ) {

                    FilterChip(
                        selected =
                            reportMode ==
                                    ReportMode.DAILY,

                        onClick = {
                            viewModel
                                .setReportMode(
                                    ReportMode.DAILY
                                )
                        },

                        label = {
                            Text("Daily")
                        },

                        modifier =
                            Modifier.weight(1f)
                    )

                    FilterChip(
                        selected =
                            reportMode ==
                                    ReportMode.WEEKLY,

                        onClick = {
                            viewModel
                                .setReportMode(
                                    ReportMode.WEEKLY
                                )
                        },

                        label = {
                            Text("Weekly")
                        },

                        modifier =
                            Modifier.weight(1f)
                    )

                    FilterChip(
                        selected =
                            reportMode ==
                                    ReportMode.MONTHLY,

                        onClick = {
                            viewModel
                                .setReportMode(
                                    ReportMode.MONTHLY
                                )
                        },

                        label = {
                            Text("Monthly")
                        },

                        modifier =
                            Modifier.weight(1f)
                    )
                }

                Spacer(
                    modifier =
                        Modifier.height(16.dp)
                )

                when (reportMode) {

                    ReportMode.DAILY -> {

                        DailyDateControls(
                            selectedDate =
                                selectedDate,

                            dateFormatter =
                                dateFormatter,

                            onPrevious = {
                                viewModel.previousDay()
                            },

                            onNext = {
                                viewModel.nextDay()
                            },

                            onDateClick = {
                                showDatePicker = true
                            },

                            onToday = {
                                viewModel.today()
                            }
                        )
                    }

                    ReportMode.WEEKLY -> {

                        WeeklyControls(
                            weekStart =
                                selectedWeekStart,

                            weekEnd =
                                selectedWeekStart
                                    .plusDays(6),

                            formatter =
                                shortDateFormatter,

                            onPrevious = {
                                viewModel.previousWeek()
                            },

                            onNext = {
                                viewModel.nextWeek()
                            },

                            onCurrentWeek = {
                                viewModel.currentWeek()
                            }
                        )
                    }

                    ReportMode.MONTHLY -> {

                        MonthlyControls(
                            selectedMonth =
                                selectedMonth,

                            monthFormatter =
                                monthFormatter,

                            onPrevious = {
                                viewModel.previousMonth()
                            },

                            onNext = {
                                viewModel.nextMonth()
                            },

                            onCurrentMonth = {
                                viewModel.currentMonth()
                            }
                        )
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(4.dp)
                )
            }
        }

        if (
            reportItems.isEmpty()
        ) {

            item {

                EmptyReportCard(
                    reportMode =
                        reportMode,

                    selectedDate =
                        selectedDate,

                    selectedWeekStart =
                        selectedWeekStart,

                    selectedMonth =
                        selectedMonth
                )
            }

        } else {

            item {

                SummaryCard(
                    title =
                        when (reportMode) {

                            ReportMode.DAILY ->
                                "Daily Summary"

                            ReportMode.WEEKLY ->
                                "Weekly Summary"

                            ReportMode.MONTHLY ->
                                "Monthly Summary"
                        },

                    summary =
                        summary
                )
            }

            when (reportMode) {

                ReportMode.DAILY -> {

                    items(
                        items =
                            reportItems,

                        key = {
                            it.shift.id
                        }
                    ) { item ->

                        val company =
                            companies
                                .firstOrNull {
                                    it.id ==
                                            item.shift.companyId
                                }

                        val calculation =
                            ShiftCalculation
                                .calculate(
                                    shift =
                                        item.shift,

                                    breaks =
                                        item.breaks,

                                    hourlyRatePence =
                                        company
                                            ?.hourlyRatePence
                                )

                        ReportShiftCard(
                            item =
                                item,

                            companyName =
                                company?.name
                                    ?: "Unknown company",

                            hourlyRatePence =
                                company
                                    ?.hourlyRatePence,

                            scheduledMinutes =
                                calculation
                                    .scheduledMinutes,

                            actualMinutes =
                                calculation
                                    .actualMinutes,

                            paidBreakMinutes =
                                calculation
                                    .paidBreakMinutes,

                            unpaidBreakMinutes =
                                calculation
                                    .unpaidBreakMinutes,

                            paidWorkedMinutes =
                                calculation
                                    .paidWorkedMinutes,

                            earningsPence =
                                calculation
                                    .estimatedEarningsPence
                        )
                    }
                }

                ReportMode.WEEKLY -> {

                    item {

                        WeeklyBreakdownCard(
                            reportItems =
                                weeklyItems,

                            companies =
                                companies,

                            weekStart =
                                selectedWeekStart
                        )
                    }
                }

                ReportMode.MONTHLY -> {

                    item {

                        MonthlyCompanyBreakdownCard(
                            reportItems =
                                monthlyItems,

                            companies =
                                companies
                        )
                    }

                    item {

                        MonthlyWeekBreakdownCard(
                            reportItems =
                                monthlyItems,

                            companies =
                                companies,

                            month =
                                selectedMonth
                        )
                    }
                }
            }
        }

        item {

            Spacer(
                modifier =
                    Modifier.height(20.dp)
            )
        }
    }

    if (showDatePicker) {

        val pickerState =
            rememberDatePickerState(
                initialSelectedDateMillis =
                    selectedDate
                        .atStartOfDay(
                            ZoneId.systemDefault()
                        )
                        .toInstant()
                        .toEpochMilli()
            )

        DatePickerDialog(
            onDismissRequest = {
                showDatePicker =
                    false
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        pickerState
                            .selectedDateMillis
                            ?.let { millis ->

                                val date =
                                    Instant
                                        .ofEpochMilli(
                                            millis
                                        )
                                        .atZone(
                                            ZoneId.systemDefault()
                                        )
                                        .toLocalDate()

                                viewModel
                                    .setSelectedDate(
                                        date
                                    )
                            }

                        showDatePicker =
                            false
                    }
                ) {

                    Text("OK")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showDatePicker =
                            false
                    }
                ) {

                    Text("Cancel")
                }
            }
        ) {

            DatePicker(
                state =
                    pickerState
            )
        }
    }
}

@Composable
private fun EmptyReportCard(
    reportMode: ReportMode,
    selectedDate: LocalDate,
    selectedWeekStart: LocalDate,
    selectedMonth: YearMonth
) {

    val dateFormatter =
        DateTimeFormatter.ofPattern(
            "dd MMM yyyy"
        )

    val shortDateFormatter =
        DateTimeFormatter.ofPattern(
            "dd MMM"
        )

    val monthFormatter =
        DateTimeFormatter.ofPattern(
            "MMMM yyyy"
        )

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal =
                        16.dp
                )
    ) {

        Column(
            modifier =
                Modifier.padding(
                    20.dp
                )
        ) {

            Text(
                text =
                    when (reportMode) {

                        ReportMode.DAILY ->
                            "No completed shifts"

                        ReportMode.WEEKLY ->
                            "No completed shifts this week"

                        ReportMode.MONTHLY ->
                            "No completed shifts this month"
                    },

                style =
                    MaterialTheme
                        .typography
                        .titleMedium
            )

            Spacer(
                modifier =
                    Modifier.height(6.dp)
            )

            Text(
                text =
                    when (reportMode) {

                        ReportMode.DAILY ->

                            "There are no completed shifts for ${
                                selectedDate.format(
                                    dateFormatter
                                )
                            }."

                        ReportMode.WEEKLY -> {

                            val weekEnd =
                                selectedWeekStart
                                    .plusDays(6)

                            "There are no completed shifts from ${
                                selectedWeekStart.format(
                                    shortDateFormatter
                                )
                            } to ${
                                weekEnd.format(
                                    dateFormatter
                                )
                            }."
                        }

                        ReportMode.MONTHLY ->

                            "There are no completed shifts for ${
                                selectedMonth.format(
                                    monthFormatter
                                )
                            }."
                    },

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WeeklyBreakdownCard(
    reportItems: List<DailyReportShift>,
    companies: List<CompanyEntity>,
    weekStart: LocalDate
) {

    val zoneId =
        ZoneId.systemDefault()

    val dayFormatter =
        DateTimeFormatter.ofPattern(
            "EEE, dd MMM"
        )

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp
                )
    ) {

        Column(
            modifier =
                Modifier.padding(16.dp)
        ) {

            Text(
                text =
                    "Breakdown by Day",

                style =
                    MaterialTheme
                        .typography
                        .titleLarge
            )

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            (0L..6L).forEach { offset ->

                val date =
                    weekStart
                        .plusDays(offset)

                val itemsForDay =
                    reportItems.filter { item ->

                        Instant
                            .ofEpochMilli(
                                item.shift
                                    .scheduledStartEpochMillis
                            )
                            .atZone(zoneId)
                            .toLocalDate() ==
                                date
                    }

                val daySummary =
                    calculateSummary(
                        reportItems =
                            itemsForDay,

                        companies =
                            companies
                    )

                ReportBreakdownRow(
                    title =
                        date.format(
                            dayFormatter
                        ),

                    subtitle =
                        "${daySummary.shiftCount} shift(s)",

                    paidWorkMinutes =
                        daySummary
                            .paidWorkedMinutes,

                    earningsPence =
                        daySummary
                            .earningsPence
                )

                if (offset < 6L) {

                    Spacer(
                        modifier =
                            Modifier.height(10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthlyCompanyBreakdownCard(
    reportItems: List<DailyReportShift>,
    companies: List<CompanyEntity>
) {

    val companyGroups =
        reportItems
            .groupBy {
                it.shift.companyId
            }

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp
                )
    ) {

        Column(
            modifier =
                Modifier.padding(16.dp)
        ) {

            Text(
                text =
                    "Breakdown by Company",

                style =
                    MaterialTheme
                        .typography
                        .titleLarge
            )

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            companyGroups
                .entries
                .sortedBy { entry ->

                    companies
                        .firstOrNull {
                            it.id ==
                                    entry.key
                        }
                        ?.name
                        ?: "Unknown company"
                }
                .forEachIndexed {
                        index,
                        entry ->

                    val company =
                        companies
                            .firstOrNull {
                                it.id ==
                                        entry.key
                            }

                    val summary =
                        calculateSummary(
                            reportItems =
                                entry.value,

                            companies =
                                companies
                        )

                    ReportBreakdownRow(
                        title =
                            company?.name
                                ?: "Unknown company",

                        subtitle =
                            "${summary.shiftCount} shift(s)",

                        paidWorkMinutes =
                            summary
                                .paidWorkedMinutes,

                        earningsPence =
                            summary
                                .earningsPence
                    )

                    if (
                        index <
                        companyGroups.size - 1
                    ) {

                        Spacer(
                            modifier =
                                Modifier.height(
                                    10.dp
                                )
                        )
                    }
                }
        }
    }
}

@Composable
private fun MonthlyWeekBreakdownCard(
    reportItems: List<DailyReportShift>,
    companies: List<CompanyEntity>,
    month: YearMonth
) {

    val zoneId =
        ZoneId.systemDefault()

    val firstDay =
        month.atDay(1)

    val lastDay =
        month.atEndOfMonth()

    var weekStart =
        firstDay.with(
            TemporalAdjusters
                .previousOrSame(
                    DayOfWeek.MONDAY
                )
        )

    val finalWeekStart =
        lastDay.with(
            TemporalAdjusters
                .previousOrSame(
                    DayOfWeek.MONDAY
                )
        )

    val weekStarts =
        mutableListOf<LocalDate>()

    while (
        !weekStart.isAfter(
            finalWeekStart
        )
    ) {

        weekStarts.add(
            weekStart
        )

        weekStart =
            weekStart.plusWeeks(1)
    }

    val formatter =
        DateTimeFormatter.ofPattern(
            "dd MMM"
        )

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp
                )
    ) {

        Column(
            modifier =
                Modifier.padding(16.dp)
        ) {

            Text(
                text =
                    "Breakdown by Week",

                style =
                    MaterialTheme
                        .typography
                        .titleLarge
            )

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            weekStarts.forEachIndexed {
                    index,
                    start ->

                val end =
                    start.plusDays(6)

                val weekItems =
                    reportItems.filter { item ->

                        val date =
                            Instant
                                .ofEpochMilli(
                                    item.shift
                                        .scheduledStartEpochMillis
                                )
                                .atZone(
                                    zoneId
                                )
                                .toLocalDate()

                        !date.isBefore(start) &&
                                !date.isAfter(end)
                    }

                val summary =
                    calculateSummary(
                        reportItems =
                            weekItems,

                        companies =
                            companies
                    )

                ReportBreakdownRow(
                    title =
                        "${start.format(formatter)} – ${
                            end.format(
                                formatter
                            )
                        }",

                    subtitle =
                        "${summary.shiftCount} shift(s)",

                    paidWorkMinutes =
                        summary
                            .paidWorkedMinutes,

                    earningsPence =
                        summary
                            .earningsPence
                )

                if (
                    index <
                    weekStarts.size - 1
                ) {

                    Spacer(
                        modifier =
                            Modifier.height(10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ReportBreakdownRow(
    title: String,
    subtitle: String,
    paidWorkMinutes: Long,
    earningsPence: Long
) {

    Row(
        modifier =
            Modifier.fillMaxWidth(),

        horizontalArrangement =
            Arrangement.SpaceBetween
    ) {

        Column(
            modifier =
                Modifier.weight(1f)
        ) {

            Text(
                text =
                    title,

                style =
                    MaterialTheme
                        .typography
                        .titleSmall
            )

            Text(
                text =
                    subtitle,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }

        Column {

            Text(
                text =
                    ShiftCalculation
                        .formatMinutes(
                            paidWorkMinutes
                        ),

                style =
                    MaterialTheme
                        .typography
                        .labelLarge
            )

            Text(
                text =
                    ShiftCalculation
                        .formatCurrency(
                            earningsPence
                        ),

                color =
                    MaterialTheme
                        .colorScheme
                        .primary
            )
        }
    }
}

@Composable
private fun DailyDateControls(
    selectedDate: LocalDate,
    dateFormatter: DateTimeFormatter,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onDateClick: () -> Unit,
    onToday: () -> Unit
) {

    Row(
        modifier =
            Modifier.fillMaxWidth(),

        horizontalArrangement =
            Arrangement.spacedBy(
                8.dp
            )
    ) {

        OutlinedButton(
            onClick =
                onPrevious,

            modifier =
                Modifier.weight(1f)
        ) {

            Text("Previous")
        }

        OutlinedButton(
            onClick =
                onDateClick,

            modifier =
                Modifier.weight(1.4f)
        ) {

            Text(
                selectedDate.format(
                    dateFormatter
                )
            )
        }

        OutlinedButton(
            onClick =
                onNext,

            modifier =
                Modifier.weight(1f)
        ) {

            Text("Next")
        }
    }

    Spacer(
        modifier =
            Modifier.height(8.dp)
    )

    Button(
        onClick =
            onToday,

        modifier =
            Modifier.fillMaxWidth()
    ) {

        Text("Today")
    }
}

@Composable
private fun WeeklyControls(
    weekStart: LocalDate,
    weekEnd: LocalDate,
    formatter: DateTimeFormatter,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onCurrentWeek: () -> Unit
) {

    Text(
        text =
            "Week: ${
                weekStart.format(
                    formatter
                )
            } – ${
                weekEnd.format(
                    DateTimeFormatter.ofPattern(
                        "dd MMM yyyy"
                    )
                )
            }",

        style =
            MaterialTheme
                .typography
                .titleMedium
    )

    Spacer(
        modifier =
            Modifier.height(10.dp)
    )

    Row(
        modifier =
            Modifier.fillMaxWidth(),

        horizontalArrangement =
            Arrangement.spacedBy(
                8.dp
            )
    ) {

        OutlinedButton(
            onClick =
                onPrevious,

            modifier =
                Modifier.weight(1f)
        ) {

            Text("Previous")
        }

        OutlinedButton(
            onClick =
                onNext,

            modifier =
                Modifier.weight(1f)
        ) {

            Text("Next")
        }
    }

    Spacer(
        modifier =
            Modifier.height(8.dp)
    )

    Button(
        onClick =
            onCurrentWeek,

        modifier =
            Modifier.fillMaxWidth()
    ) {

        Text("Current Week")
    }
}

@Composable
private fun MonthlyControls(
    selectedMonth: YearMonth,
    monthFormatter: DateTimeFormatter,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onCurrentMonth: () -> Unit
) {

    Text(
        text =
            selectedMonth.format(
                monthFormatter
            ),

        style =
            MaterialTheme
                .typography
                .titleLarge
    )

    Spacer(
        modifier =
            Modifier.height(10.dp)
    )

    Row(
        modifier =
            Modifier.fillMaxWidth(),

        horizontalArrangement =
            Arrangement.spacedBy(
                8.dp
            )
    ) {

        OutlinedButton(
            onClick =
                onPrevious,

            modifier =
                Modifier.weight(1f)
        ) {

            Text("Previous")
        }

        OutlinedButton(
            onClick =
                onNext,

            modifier =
                Modifier.weight(1f)
        ) {

            Text("Next")
        }
    }

    Spacer(
        modifier =
            Modifier.height(8.dp)
    )

    Button(
        onClick =
            onCurrentMonth,

        modifier =
            Modifier.fillMaxWidth()
    ) {

        Text("Current Month")
    }
}

private data class ReportSummary(
    val shiftCount: Int,
    val scheduledMinutes: Long,
    val actualMinutes: Long,
    val paidWorkedMinutes: Long,
    val paidBreakMinutes: Long,
    val unpaidBreakMinutes: Long,
    val earningsPence: Long
)

private fun calculateSummary(
    reportItems: List<DailyReportShift>,
    companies: List<CompanyEntity>
): ReportSummary {

    var scheduledMinutes =
        0L

    var actualMinutes =
        0L

    var paidWorkedMinutes =
        0L

    var paidBreakMinutes =
        0L

    var unpaidBreakMinutes =
        0L

    var earningsPence =
        0L

    reportItems.forEach { item ->

        val company =
            companies.firstOrNull {
                it.id ==
                        item.shift.companyId
            }

        val calculation =
            ShiftCalculation
                .calculate(
                    shift =
                        item.shift,

                    breaks =
                        item.breaks,

                    hourlyRatePence =
                        company
                            ?.hourlyRatePence
                )

        scheduledMinutes +=
            calculation
                .scheduledMinutes

        actualMinutes +=
            calculation
                .actualMinutes
                ?: 0L

        paidWorkedMinutes +=
            calculation
                .paidWorkedMinutes
                ?: 0L

        paidBreakMinutes +=
            calculation
                .paidBreakMinutes

        unpaidBreakMinutes +=
            calculation
                .unpaidBreakMinutes

        earningsPence +=
            calculation
                .estimatedEarningsPence
                ?: 0L
    }

    return ReportSummary(
        shiftCount =
            reportItems.size,

        scheduledMinutes =
            scheduledMinutes,

        actualMinutes =
            actualMinutes,

        paidWorkedMinutes =
            paidWorkedMinutes,

        paidBreakMinutes =
            paidBreakMinutes,

        unpaidBreakMinutes =
            unpaidBreakMinutes,

        earningsPence =
            earningsPence
    )
}

@Composable
private fun SummaryCard(
    title: String,
    summary: ReportSummary
) {

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp
                )
    ) {

        Column(
            modifier =
                Modifier.padding(16.dp)
        ) {

            Text(
                text =
                    title,

                style =
                    MaterialTheme
                        .typography
                        .titleLarge
            )

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            ReportLine(
                label =
                    "Completed shifts",

                value =
                    summary
                        .shiftCount
                        .toString()
            )

            ReportLine(
                label =
                    "Scheduled",

                value =
                    ShiftCalculation
                        .formatMinutes(
                            summary
                                .scheduledMinutes
                        )
            )

            ReportLine(
                label =
                    "Actual",

                value =
                    ShiftCalculation
                        .formatMinutes(
                            summary
                                .actualMinutes
                        )
            )

            ReportLine(
                label =
                    "Paid breaks",

                value =
                    ShiftCalculation
                        .formatMinutes(
                            summary
                                .paidBreakMinutes
                        )
            )

            ReportLine(
                label =
                    "Unpaid breaks",

                value =
                    ShiftCalculation
                        .formatMinutes(
                            summary
                                .unpaidBreakMinutes
                        )
            )

            ReportLine(
                label =
                    "Paid work",

                value =
                    ShiftCalculation
                        .formatMinutes(
                            summary
                                .paidWorkedMinutes
                        )
            )

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            Text(
                text =
                    "Estimated earnings: ${
                        ShiftCalculation
                            .formatCurrency(
                                summary
                                    .earningsPence
                            )
                    }",

                style =
                    MaterialTheme
                        .typography
                        .titleMedium,

                color =
                    MaterialTheme
                        .colorScheme
                        .primary
            )
        }
    }
}

@Composable
private fun ReportShiftCard(
    item: DailyReportShift,
    companyName: String,
    hourlyRatePence: Long?,
    scheduledMinutes: Long,
    actualMinutes: Long?,
    paidBreakMinutes: Long,
    unpaidBreakMinutes: Long,
    paidWorkedMinutes: Long?,
    earningsPence: Long?
) {

    val zoneId =
        ZoneId.systemDefault()

    val timeFormatter =
        DateTimeFormatter.ofPattern(
            "h:mm a"
        )

    val scheduledStartDateTime =
        Instant
            .ofEpochMilli(
                item.shift
                    .scheduledStartEpochMillis
            )
            .atZone(
                zoneId
            )

    val scheduledEndDateTime =
        Instant
            .ofEpochMilli(
                item.shift
                    .scheduledEndEpochMillis
            )
            .atZone(
                zoneId
            )

    val scheduledStart =
        scheduledStartDateTime
            .format(
                timeFormatter
            )

    val scheduledEnd =
        scheduledEndDateTime
            .format(
                timeFormatter
            )

    val actualStart =
        item.shift
            .actualStartEpochMillis
            ?.let { millis ->

                Instant
                    .ofEpochMilli(
                        millis
                    )
                    .atZone(zoneId)
                    .format(
                        timeFormatter
                    )
            }

    val actualEnd =
        item.shift
            .actualEndEpochMillis
            ?.let { millis ->

                Instant
                    .ofEpochMilli(
                        millis
                    )
                    .atZone(zoneId)
                    .format(
                        timeFormatter
                    )
            }

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp
                )
    ) {

        Column(
            modifier =
                Modifier.padding(
                    16.dp
                )
        ) {

            Text(
                text =
                    companyName,

                style =
                    MaterialTheme
                        .typography
                        .titleMedium
            )

            Spacer(
                modifier =
                    Modifier.height(
                        10.dp
                    )
            )

            Text(
                text =
                    "Scheduled: $scheduledStart – $scheduledEnd"
            )

            Text(
                text =
                    "Scheduled duration: ${
                        ShiftCalculation
                            .formatMinutes(
                                scheduledMinutes
                            )
                    }"
            )

            Spacer(
                modifier =
                    Modifier.height(
                        8.dp
                    )
            )

            if (
                actualStart != null &&
                actualEnd != null
            ) {

                Text(
                    text =
                        "Actual: $actualStart – $actualEnd"
                )
            }

            if (
                actualMinutes != null
            ) {

                Text(
                    text =
                        "Actual duration: ${
                            ShiftCalculation
                                .formatMinutes(
                                    actualMinutes
                                )
                        }"
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
                    "Paid breaks: ${
                        ShiftCalculation
                            .formatMinutes(
                                paidBreakMinutes
                            )
                    }"
            )

            Text(
                text =
                    "Unpaid breaks: ${
                        ShiftCalculation
                            .formatMinutes(
                                unpaidBreakMinutes
                            )
                    }"
            )

            if (
                paidWorkedMinutes != null
            ) {

                Text(
                    text =
                        "Paid work: ${
                            ShiftCalculation
                                .formatMinutes(
                                    paidWorkedMinutes
                                )
                        }"
                )
            }

            Spacer(
                modifier =
                    Modifier.height(
                        8.dp
                    )
            )

            if (
                hourlyRatePence != null
            ) {

                Text(
                    text =
                        "Hourly rate: ${
                            ShiftCalculation
                                .formatCurrency(
                                    hourlyRatePence
                                )
                        }"
                )

            } else {

                Text(
                    text =
                        "Hourly rate: Not set",

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )
            }

            if (
                earningsPence != null
            ) {

                Text(
                    text =
                        "Estimated earnings: ${
                            ShiftCalculation
                                .formatCurrency(
                                    earningsPence
                                )
                        }",

                    style =
                        MaterialTheme
                            .typography
                            .titleSmall,

                    color =
                        MaterialTheme
                            .colorScheme
                            .primary
                )

            } else {

                Text(
                    text =
                        "Estimated earnings unavailable",

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ReportLine(
    label: String,
    value: String
) {

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 3.dp
                ),

        horizontalArrangement =
            Arrangement.SpaceBetween
    ) {

        Text(
            text =
                label,

            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )

        Text(
            text =
                value,

            style =
                MaterialTheme
                    .typography
                    .labelLarge
        )
    }
}