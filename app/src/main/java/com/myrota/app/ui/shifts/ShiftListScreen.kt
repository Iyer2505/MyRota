package com.myrota.app.ui.shifts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.myrota.app.calculation.ShiftCalculation
import com.myrota.app.local.BreakEntity
import com.myrota.app.local.CompanyEntity
import com.myrota.app.local.ShiftEntity
import com.myrota.app.local.ShiftStatus
import com.myrota.app.ui.breaks.BreakViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ShiftListScreen(
    viewModel: ShiftViewModel,
    breakViewModel: BreakViewModel,
    companies: List<CompanyEntity>,
    onShiftClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {

    val shifts by
    viewModel
        .allShifts
        .collectAsStateWithLifecycle()

    val activeShifts =
        shifts.filter { shift ->
            shift.status != ShiftStatus.CANCELLED
        }

    var shiftToStart by remember {
        mutableStateOf<ShiftEntity?>(null)
    }

    var shiftToEnd by remember {
        mutableStateOf<ShiftEntity?>(null)
    }

    var shiftForBreak by remember {
        mutableStateOf<ShiftEntity?>(null)
    }

    var breakToEdit by remember {
        mutableStateOf<BreakEntity?>(null)
    }

    var breakToDelete by remember {
        mutableStateOf<BreakEntity?>(null)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    if (activeShifts.isEmpty()) {

        EmptyShiftState(
            modifier = modifier
        )

    } else {

        ShiftList(
            shifts = activeShifts,
            companies = companies,
            breakViewModel = breakViewModel,
            onShiftClick = onShiftClick,

            onStartShift = { shift ->
                shiftToStart = shift
            },

            onEndShift = { shift ->
                shiftToEnd = shift
            },

            onAddBreak = { shift ->
                shiftForBreak = shift
            },

            onEditBreak = { breakEntity ->
                breakToEdit = breakEntity
            },

            modifier = modifier
        )
    }

    shiftToStart?.let { shift ->

        val companyName =
            companies
                .firstOrNull {
                    it.id == shift.companyId
                }
                ?.name
                ?: "this company"

        AlertDialog(
            onDismissRequest = {
                shiftToStart = null
            },

            title = {
                Text("Start shift?")
            },

            text = {
                Text(
                    "Clock in to your shift at $companyName now?"
                )
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        errorMessage = null

                        viewModel.startShift(
                            shiftId = shift.id,

                            onSuccess = {
                                shiftToStart = null
                            },

                            onError = { message ->
                                shiftToStart = null
                                errorMessage = message
                            }
                        )
                    }
                ) {
                    Text("Start Shift")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        shiftToStart = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    shiftToEnd?.let { shift ->

        val companyName =
            companies
                .firstOrNull {
                    it.id == shift.companyId
                }
                ?.name
                ?: "this company"

        AlertDialog(
            onDismissRequest = {
                shiftToEnd = null
            },

            title = {
                Text("End shift?")
            },

            text = {
                Text(
                    "Clock out from your shift at $companyName now?"
                )
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        errorMessage = null

                        viewModel.endShift(
                            shift = shift,

                            onSuccess = {
                                shiftToEnd = null
                            },

                            onError = { message ->
                                shiftToEnd = null
                                errorMessage = message
                            }
                        )
                    }
                ) {
                    Text("End Shift")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        shiftToEnd = null
                    }
                ) {
                    Text("Keep Working")
                }
            }
        )
    }

    shiftForBreak?.let { shift ->

        AddBreakDialog(
            shift = shift,
            breakViewModel = breakViewModel,

            onDismiss = {
                shiftForBreak = null
            },

            onError = { message ->
                shiftForBreak = null
                errorMessage = message
            }
        )
    }

    breakToEdit?.let { breakEntity ->

        EditBreakDialog(
            breakEntity = breakEntity,
            breakViewModel = breakViewModel,

            onDismiss = {
                breakToEdit = null
            },

            onDeleteRequest = {
                breakToEdit = null
                breakToDelete = breakEntity
            },

            onError = { message ->
                breakToEdit = null
                errorMessage = message
            }
        )
    }

    breakToDelete?.let { breakEntity ->

        AlertDialog(
            onDismissRequest = {
                breakToDelete = null
            },

            title = {
                Text("Delete break?")
            },

            text = {
                Text(
                    "This ${breakEntity.durationMinutes}-minute " +
                            "${if (breakEntity.isPaid) "paid" else "unpaid"} break will be removed."
                )
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        breakViewModel.deleteBreak(
                            breakEntity = breakEntity,

                            onSuccess = {
                                breakToDelete = null
                            },

                            onError = { message ->
                                breakToDelete = null
                                errorMessage = message
                            }
                        )
                    }
                ) {
                    Text(
                        text = "Delete",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        breakToDelete = null
                    }
                ) {
                    Text("Keep Break")
                }
            }
        )
    }

    errorMessage?.let { message ->

        AlertDialog(
            onDismissRequest = {
                errorMessage = null
            },

            title = {
                Text("Action failed")
            },

            text = {
                Text(message)
            },

            confirmButton = {

                TextButton(
                    onClick = {
                        errorMessage = null
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun EmptyShiftState(
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),

        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "No shifts yet",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = "Your saved shifts will appear here.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ShiftList(
    shifts: List<ShiftEntity>,
    companies: List<CompanyEntity>,
    breakViewModel: BreakViewModel,
    onShiftClick: (Long) -> Unit,
    onStartShift: (ShiftEntity) -> Unit,
    onEndShift: (ShiftEntity) -> Unit,
    onAddBreak: (ShiftEntity) -> Unit,
    onEditBreak: (BreakEntity) -> Unit,
    modifier: Modifier = Modifier
) {

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        items(
            items = shifts,
            key = { shift ->
                shift.id
            }
        ) { shift ->

            val company =
                companies.firstOrNull {
                    it.id == shift.companyId
                }

            ShiftCard(
                shift = shift,
                companyName =
                    company?.name ?: "Unknown company",

                breakViewModel = breakViewModel,

                hourlyRatePence =
                    company?.hourlyRatePence,

                onClick = {
                    onShiftClick(
                        shift.id
                    )
                },

                onStartShift = {
                    onStartShift(
                        shift
                    )
                },

                onEndShift = {
                    onEndShift(
                        shift
                    )
                },

                onAddBreak = {
                    onAddBreak(
                        shift
                    )
                },

                onEditBreak =
                    onEditBreak
            )
        }
    }
}

@Composable
private fun ShiftCard(
    shift: ShiftEntity,
    companyName: String,
    breakViewModel: BreakViewModel,
    hourlyRatePence: Long?,
    onClick: () -> Unit,
    onStartShift: () -> Unit,
    onEndShift: () -> Unit,
    onAddBreak: () -> Unit,
    onEditBreak: (BreakEntity) -> Unit
) {

    val zoneId =
        ZoneId.systemDefault()

    val startDateTime =
        Instant
            .ofEpochMilli(
                shift.scheduledStartEpochMillis
            )
            .atZone(zoneId)

    val endDateTime =
        Instant
            .ofEpochMilli(
                shift.scheduledEndEpochMillis
            )
            .atZone(zoneId)

    val dateFormatter =
        DateTimeFormatter.ofPattern(
            "dd MMM yyyy"
        )

    val timeFormatter =
        DateTimeFormatter.ofPattern(
            "h:mm a"
        )

    val startDate =
        startDateTime.format(
            dateFormatter
        )

    val scheduledStartTime =
        startDateTime.format(
            timeFormatter
        )

    val scheduledEndTime =
        endDateTime.format(
            timeFormatter
        )

    val isOvernight =
        startDateTime.toLocalDate() !=
                endDateTime.toLocalDate()

    val actualStartTime =
        shift.actualStartEpochMillis
            ?.let { millis ->

                Instant
                    .ofEpochMilli(millis)
                    .atZone(zoneId)
                    .format(timeFormatter)
            }

    val actualEndTime =
        shift.actualEndEpochMillis
            ?.let { millis ->

                Instant
                    .ofEpochMilli(millis)
                    .atZone(zoneId)
                    .format(timeFormatter)
            }

    val breaks by
    breakViewModel
        .observeBreaksForShift(
            shift.id
        )
        .collectAsStateWithLifecycle(
            initialValue = emptyList()
        )

    val calculation =
        ShiftCalculation.calculate(
            shift = shift,
            breaks = breaks,
            hourlyRatePence = hourlyRatePence
        )

    val rawPaidBreakMinutes =
        breaks
            .filter {
                it.isPaid
            }
            .sumOf {
                it.durationMinutes
            }

    val rawUnpaidBreakMinutes =
        breaks
            .filter {
                !it.isPaid
            }
            .sumOf {
                it.durationMinutes
            }

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
                Modifier.padding(16.dp)
        ) {

            Text(
                text = companyName,
                style =
                    MaterialTheme.typography.titleMedium
            )

            Spacer(
                modifier =
                    Modifier.height(6.dp)
            )

            Text(
                text = startDate,
                style =
                    MaterialTheme.typography.bodyLarge
            )

            Spacer(
                modifier =
                    Modifier.height(4.dp)
            )

            Text(
                text =
                    "Scheduled: $scheduledStartTime – $scheduledEndTime",

                style =
                    MaterialTheme.typography.bodyLarge
            )

            if (isOvernight) {

                Spacer(
                    modifier =
                        Modifier.height(4.dp)
                )

                Text(
                    text =
                        "Ends the following day",

                    style =
                        MaterialTheme.typography.bodyMedium,

                    color =
                        MaterialTheme.colorScheme.primary
                )
            }

            Spacer(
                modifier =
                    Modifier.height(10.dp)
            )

            Text(
                text =
                    when (shift.status) {

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
                    MaterialTheme.typography.labelMedium,

                color =
                    when (shift.status) {

                        ShiftStatus.IN_PROGRESS ->
                            MaterialTheme.colorScheme.primary

                        ShiftStatus.COMPLETED ->
                            MaterialTheme.colorScheme.tertiary

                        else ->
                            MaterialTheme.colorScheme.onSurfaceVariant
                    }
            )

            if (actualStartTime != null) {

                Spacer(
                    modifier =
                        Modifier.height(6.dp)
                )

                Text(
                    text =
                        "Clocked in: $actualStartTime",

                    style =
                        MaterialTheme.typography.bodyMedium
                )
            }

            if (actualEndTime != null) {

                Spacer(
                    modifier =
                        Modifier.height(4.dp)
                )

                Text(
                    text =
                        "Clocked out: $actualEndTime",

                    style =
                        MaterialTheme.typography.bodyMedium
                )
            }

            if (breaks.isNotEmpty()) {

                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )

                Text(
                    text = "Breaks",
                    style =
                        MaterialTheme.typography.titleSmall
                )

                Spacer(
                    modifier =
                        Modifier.height(6.dp)
                )

                breaks.forEach { breakEntity ->

                    OutlinedButton(
                        onClick = {
                            onEditBreak(
                                breakEntity
                            )
                        },

                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        Text(
                            text =
                                "${breakEntity.durationMinutes} min — " +
                                        if (
                                            breakEntity.isPaid
                                        ) {
                                            "Paid"
                                        } else {
                                            "Unpaid"
                                        }
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.height(6.dp)
                    )
                }

                if (
                    rawPaidBreakMinutes > 0
                ) {

                    Text(
                        text =
                            "Stored paid breaks: ${rawPaidBreakMinutes} min",

                        style =
                            MaterialTheme.typography.labelMedium
                    )
                }

                if (
                    rawUnpaidBreakMinutes > 0
                ) {

                    Text(
                        text =
                            "Stored unpaid breaks: ${rawUnpaidBreakMinutes} min",

                        style =
                            MaterialTheme.typography.labelMedium
                    )
                }

                if (
                    calculation.hasInvalidBreakData
                ) {

                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )

                    Text(
                        text =
                            "⚠ Historical break data exceeds the actual worked time. " +
                                    "Calculations have been adjusted safely.",

                        color =
                            MaterialTheme.colorScheme.error,

                        style =
                            MaterialTheme.typography.bodyMedium
                    )

                    Spacer(
                        modifier =
                            Modifier.height(6.dp)
                    )

                    Text(
                        text =
                            "Adjusted paid breaks: ${
                                ShiftCalculation.formatMinutes(
                                    calculation.paidBreakMinutes
                                )
                            }",

                        style =
                            MaterialTheme.typography.labelMedium
                    )

                    Text(
                        text =
                            "Adjusted unpaid breaks: ${
                                ShiftCalculation.formatMinutes(
                                    calculation.unpaidBreakMinutes
                                )
                            }",

                        style =
                            MaterialTheme.typography.labelMedium
                    )
                }
            }

            if (
                calculation.paidWorkedMinutes !=
                null
            ) {

                Spacer(
                    modifier =
                        Modifier.height(8.dp)
                )

                Text(
                    text =
                        "Paid work: ${
                            ShiftCalculation.formatMinutes(
                                calculation.paidWorkedMinutes
                            )
                        }",

                    style =
                        MaterialTheme.typography.bodyMedium,

                    color =
                        MaterialTheme.colorScheme.primary
                )
            }

            if (
                shift.status ==
                ShiftStatus.SCHEDULED
            ) {

                Spacer(
                    modifier =
                        Modifier.height(16.dp)
                )

                Button(
                    onClick = {
                        onStartShift()
                    },

                    modifier =
                        Modifier.fillMaxWidth()
                ) {
                    Text("Start Shift")
                }
            }

            if (
                shift.status ==
                ShiftStatus.IN_PROGRESS
            ) {

                Spacer(
                    modifier =
                        Modifier.height(16.dp)
                )

                OutlinedButton(
                    onClick = {
                        onAddBreak()
                    },

                    modifier =
                        Modifier.fillMaxWidth()
                ) {
                    Text("Add Break")
                }

                Spacer(
                    modifier =
                        Modifier.height(10.dp)
                )

                Button(
                    onClick = {
                        onEndShift()
                    },

                    modifier =
                        Modifier.fillMaxWidth()
                ) {
                    Text("End Shift")
                }
            }

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            Text(
                text =
                    "Tap card to edit shift",

                style =
                    MaterialTheme.typography.labelSmall,

                color =
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AddBreakDialog(
    shift: ShiftEntity,
    breakViewModel: BreakViewModel,
    onDismiss: () -> Unit,
    onError: (String) -> Unit
) {

    var selectedDuration by remember {
        mutableIntStateOf(30)
    }

    var isPaid by remember {
        mutableStateOf(false)
    }

    AlertDialog(
        onDismissRequest =
            onDismiss,

        title = {
            Text("Add Break")
        },

        text = {

            Column {

                Text(
                    text = "Break duration",
                    style =
                        MaterialTheme.typography.labelLarge
                )

                Spacer(
                    modifier =
                        Modifier.height(8.dp)
                )

                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {

                    listOf(
                        15,
                        30,
                        60
                    ).forEach { duration ->

                        FilterChip(
                            selected =
                                selectedDuration ==
                                        duration,

                            onClick = {
                                selectedDuration =
                                    duration
                            },

                            label = {
                                Text(
                                    "$duration min"
                                )
                            }
                        )
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(20.dp)
                )

                Text(
                    text = "Break type",
                    style =
                        MaterialTheme.typography.labelLarge
                )

                Spacer(
                    modifier =
                        Modifier.height(8.dp)
                )

                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {

                    FilterChip(
                        selected =
                            !isPaid,

                        onClick = {
                            isPaid = false
                        },

                        label = {
                            Text("Unpaid")
                        }
                    )

                    FilterChip(
                        selected =
                            isPaid,

                        onClick = {
                            isPaid = true
                        },

                        label = {
                            Text("Paid")
                        }
                    )
                }
            }
        },

        confirmButton = {

            TextButton(
                onClick = {

                    breakViewModel.addBreak(
                        shiftId =
                            shift.id,

                        durationMinutes =
                            selectedDuration,

                        isPaid =
                            isPaid,

                        onSuccess = {
                            onDismiss()
                        },

                        onError = { message ->
                            onError(
                                message
                            )
                        }
                    )
                }
            ) {
                Text("Add Break")
            }
        },

        dismissButton = {

            TextButton(
                onClick =
                    onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun EditBreakDialog(
    breakEntity: BreakEntity,
    breakViewModel: BreakViewModel,
    onDismiss: () -> Unit,
    onDeleteRequest: () -> Unit,
    onError: (String) -> Unit
) {

    var selectedDuration by remember(
        breakEntity.id
    ) {
        mutableIntStateOf(
            breakEntity.durationMinutes
        )
    }

    var isPaid by remember(
        breakEntity.id
    ) {
        mutableStateOf(
            breakEntity.isPaid
        )
    }

    AlertDialog(
        onDismissRequest =
            onDismiss,

        title = {
            Text("Edit Break")
        },

        text = {

            Column {

                Text(
                    text = "Break duration",
                    style =
                        MaterialTheme.typography.labelLarge
                )

                Spacer(
                    modifier =
                        Modifier.height(8.dp)
                )

                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {

                    listOf(
                        15,
                        30,
                        60
                    ).forEach { duration ->

                        FilterChip(
                            selected =
                                selectedDuration ==
                                        duration,

                            onClick = {
                                selectedDuration =
                                    duration
                            },

                            label = {
                                Text(
                                    "$duration min"
                                )
                            }
                        )
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(20.dp)
                )

                Text(
                    text = "Break type",
                    style =
                        MaterialTheme.typography.labelLarge
                )

                Spacer(
                    modifier =
                        Modifier.height(8.dp)
                )

                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {

                    FilterChip(
                        selected =
                            !isPaid,

                        onClick = {
                            isPaid = false
                        },

                        label = {
                            Text("Unpaid")
                        }
                    )

                    FilterChip(
                        selected =
                            isPaid,

                        onClick = {
                            isPaid = true
                        },

                        label = {
                            Text("Paid")
                        }
                    )
                }

                Spacer(
                    modifier =
                        Modifier.height(20.dp)
                )

                OutlinedButton(
                    onClick = {
                        onDeleteRequest()
                    },

                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Text(
                        text = "Delete Break",
                        color =
                            MaterialTheme.colorScheme.error
                    )
                }
            }
        },

        confirmButton = {

            TextButton(
                onClick = {

                    breakViewModel.updateBreak(
                        existingBreak =
                            breakEntity,

                        durationMinutes =
                            selectedDuration,

                        isPaid =
                            isPaid,

                        onSuccess = {
                            onDismiss()
                        },

                        onError = { message ->
                            onError(
                                message
                            )
                        }
                    )
                }
            ) {
                Text("Save")
            }
        },

        dismissButton = {

            TextButton(
                onClick =
                    onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}