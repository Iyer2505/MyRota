package com.myrota.app.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.myrota.app.local.CompanyEntity
import com.myrota.app.local.ShiftEntity
import com.myrota.app.local.ShiftStatus
import com.myrota.app.ui.shifts.ShiftViewModel
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun CalendarScreen(
    shiftViewModel: ShiftViewModel,
    companies: List<CompanyEntity>,
    onShiftClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {

    val shifts by
    shiftViewModel
        .allShifts
        .collectAsStateWithLifecycle()

    var selectedMonth by remember {
        mutableStateOf(
            YearMonth.now()
        )
    }

    var selectedDate by remember {
        mutableStateOf(
            LocalDate.now()
        )
    }

    val zoneId =
        ZoneId.systemDefault()

    val monthFormatter =
        remember {
            DateTimeFormatter.ofPattern(
                "MMMM yyyy"
            )
        }

    val selectedDateFormatter =
        remember {
            DateTimeFormatter.ofPattern(
                "EEE, dd MMM yyyy"
            )
        }

    val activeShifts =
        shifts.filter { shift ->
            shift.status !=
                    ShiftStatus.CANCELLED
        }

    val shiftsByDate =
        activeShifts.groupBy { shift ->

            Instant
                .ofEpochMilli(
                    shift.scheduledStartEpochMillis
                )
                .atZone(zoneId)
                .toLocalDate()
        }

    val selectedDayShifts =
        shiftsByDate[selectedDate]
            ?.sortedBy {
                it.scheduledStartEpochMillis
            }
            ?: emptyList()

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
                    Modifier.height(
                        8.dp
                    )
            )

            Text(
                text =
                    selectedMonth.format(
                        monthFormatter
                    ),

                style =
                    MaterialTheme
                        .typography
                        .headlineSmall
            )

            Spacer(
                modifier =
                    Modifier.height(
                        12.dp
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

                OutlinedButton(
                    onClick = {

                        selectedMonth =
                            selectedMonth
                                .minusMonths(1)

                        selectedDate =
                            selectedMonth
                                .atDay(1)
                    },

                    modifier =
                        Modifier.weight(1f)
                ) {

                    Text("Previous")
                }

                OutlinedButton(
                    onClick = {

                        selectedMonth =
                            selectedMonth
                                .plusMonths(1)

                        selectedDate =
                            selectedMonth
                                .atDay(1)
                    },

                    modifier =
                        Modifier.weight(1f)
                ) {

                    Text("Next")
                }
            }

            Spacer(
                modifier =
                    Modifier.height(
                        8.dp
                    )
            )

            Button(
                onClick = {

                    selectedMonth =
                        YearMonth.now()

                    selectedDate =
                        LocalDate.now()
                },

                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Text(
                    "Current Month"
                )
            }

            Spacer(
                modifier =
                    Modifier.height(
                        16.dp
                    )
            )

            CalendarGrid(
                selectedMonth =
                    selectedMonth,

                selectedDate =
                    selectedDate,

                shiftsByDate =
                    shiftsByDate,

                companies =
                    companies,

                onDateSelected = { date ->

                    selectedDate =
                        date

                    selectedMonth =
                        YearMonth.from(
                            date
                        )
                }
            )

            Spacer(
                modifier =
                    Modifier.height(
                        20.dp
                    )
            )

            Text(
                text =
                    selectedDate.format(
                        selectedDateFormatter
                    ),

                style =
                    MaterialTheme
                        .typography
                        .titleLarge
            )
        }

        if (
            selectedDayShifts.isEmpty()
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
                                "No shifts",

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
                                "There are no shifts scheduled for this day.",

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
                    selectedDayShifts,

                key = {
                    it.id
                }
            ) { shift ->

                val company =
                    companies.firstOrNull {
                        it.id ==
                                shift.companyId
                    }

                CalendarShiftCard(
                    shift =
                        shift,

                    companyName =
                        company?.name
                            ?: "Unknown company",

                    companyColor =
                        company?.let {
                            Color(
                                it.colorArgb
                            )
                        }
                            ?: MaterialTheme
                                .colorScheme
                                .primary,

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
                        16.dp
                    )
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    selectedMonth: YearMonth,
    selectedDate: LocalDate,
    shiftsByDate: Map<LocalDate, List<ShiftEntity>>,
    companies: List<CompanyEntity>,
    onDateSelected: (LocalDate) -> Unit
) {

    val dayHeaders =
        listOf(
            "Mon",
            "Tue",
            "Wed",
            "Thu",
            "Fri",
            "Sat",
            "Sun"
        )

    Row(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        dayHeaders.forEach { day ->

            Text(
                text =
                    day,

                modifier =
                    Modifier.weight(
                        1f
                    ),

                textAlign =
                    TextAlign.Center,

                style =
                    MaterialTheme
                        .typography
                        .labelMedium,

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
                8.dp
            )
    )

    val firstDay =
        selectedMonth
            .atDay(1)

    val lastDay =
        selectedMonth
            .atEndOfMonth()

    val firstCalendarDate =
        firstDay.minusDays(
            daysFromMonday(
                firstDay.dayOfWeek
            ).toLong()
        )

    val lastCalendarDate =
        lastDay.plusDays(
            (
                    6 -
                            daysFromMonday(
                                lastDay.dayOfWeek
                            )
                    ).toLong()
        )

    val totalDays =
        ChronoUnit.DAYS
            .between(
                firstCalendarDate,
                lastCalendarDate
            )
            .toInt() + 1

    val dates =
        (0 until totalDays)
            .map { offset ->

                firstCalendarDate
                    .plusDays(
                        offset.toLong()
                    )
            }

    dates
        .chunked(7)
        .forEach { week ->

            Row(
                modifier =
                    Modifier.fillMaxWidth()
            ) {

                week.forEach { date ->

                    val dayShifts =
                        shiftsByDate[date]
                            ?: emptyList()

                    val companyColors =
                        dayShifts
                            .mapNotNull { shift ->

                                companies
                                    .firstOrNull {
                                        it.id ==
                                                shift.companyId
                                    }
                                    ?.let { company ->

                                        Color(
                                            company.colorArgb
                                        )
                                    }
                            }
                            .distinct()

                    CalendarDayCell(
                        date =
                            date,

                        isCurrentMonth =
                            YearMonth.from(
                                date
                            ) ==
                                    selectedMonth,

                        isSelected =
                            date ==
                                    selectedDate,

                        companyColors =
                            companyColors,

                        shiftCount =
                            dayShifts.size,

                        onClick = {

                            onDateSelected(
                                date
                            )
                        },

                        modifier =
                            Modifier.weight(
                                1f
                            )
                    )
                }
            }
        }
}

@Composable
private fun CalendarDayCell(
    date: LocalDate,
    isCurrentMonth: Boolean,
    isSelected: Boolean,
    companyColors: List<Color>,
    shiftCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val background =
        if (isSelected) {

            MaterialTheme
                .colorScheme
                .primaryContainer

        } else {

            MaterialTheme
                .colorScheme
                .surface
        }

    val textColor =
        when {

            isSelected ->

                MaterialTheme
                    .colorScheme
                    .onPrimaryContainer

            isCurrentMonth ->

                MaterialTheme
                    .colorScheme
                    .onSurface

            else ->

                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
                    .copy(
                        alpha =
                            0.45f
                    )
        }

    Column(
        modifier =
            modifier
                .aspectRatio(1f)
                .padding(
                    2.dp
                )
                .clip(
                    RoundedCornerShape(
                        12.dp
                    )
                )
                .background(
                    background
                )
                .clickable {
                    onClick()
                }
                .padding(
                    4.dp
                ),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Center
    ) {

        Text(
            text =
                date.dayOfMonth
                    .toString(),

            color =
                textColor,

            style =
                MaterialTheme
                    .typography
                    .bodyMedium
        )

        if (
            companyColors.isNotEmpty()
        ) {

            Spacer(
                modifier =
                    Modifier.height(
                        4.dp
                    )
            )

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(
                        3.dp
                    ),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                companyColors
                    .take(3)
                    .forEach { color ->

                        Spacer(
                            modifier =
                                Modifier
                                    .size(
                                        7.dp
                                    )
                                    .clip(
                                        CircleShape
                                    )
                                    .background(
                                        color
                                    )
                        )
                    }

                if (
                    companyColors.size >
                    3
                ) {

                    Text(
                        text =
                            "+${companyColors.size - 3}",

                        style =
                            MaterialTheme
                                .typography
                                .labelSmall,

                        color =
                            textColor
                    )
                }
            }
        }

        if (
            shiftCount > 1
        ) {

            Text(
                text =
                    "$shiftCount shifts",

                style =
                    MaterialTheme
                        .typography
                        .labelSmall,

                color =
                    textColor
            )
        }
    }
}

@Composable
private fun CalendarShiftCard(
    shift: ShiftEntity,
    companyName: String,
    companyColor: Color,
    onClick: () -> Unit
) {

    val zoneId =
        ZoneId.systemDefault()

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

    val timeFormatter =
        DateTimeFormatter.ofPattern(
            "h:mm a"
        )

    val startTime =
        startDateTime
            .format(
                timeFormatter
            )

    val endTime =
        endDateTime
            .format(
                timeFormatter
            )

    val isOvernight =
        startDateTime
            .toLocalDate() !=
                endDateTime
                    .toLocalDate()

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
                        companyName,

                    style =
                        MaterialTheme
                            .typography
                            .titleMedium
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
                    "$startTime – $endTime",

                style =
                    MaterialTheme
                        .typography
                        .bodyLarge
            )

            if (isOvernight) {

                Spacer(
                    modifier =
                        Modifier.height(
                            4.dp
                        )
                )

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
                        8.dp
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

            Spacer(
                modifier =
                    Modifier.height(
                        6.dp
                    )
            )

            Text(
                text =
                    "Tap to edit shift",

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

private fun daysFromMonday(
    dayOfWeek: DayOfWeek
): Int {

    return when (
        dayOfWeek
    ) {

        DayOfWeek.MONDAY ->
            0

        DayOfWeek.TUESDAY ->
            1

        DayOfWeek.WEDNESDAY ->
            2

        DayOfWeek.THURSDAY ->
            3

        DayOfWeek.FRIDAY ->
            4

        DayOfWeek.SATURDAY ->
            5

        DayOfWeek.SUNDAY ->
            6
    }
}