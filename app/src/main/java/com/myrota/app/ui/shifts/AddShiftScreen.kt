package com.myrota.app.ui.shifts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.myrota.app.local.CompanyEntity
import com.myrota.app.local.ShiftEntity
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddShiftScreen(
    companies: List<CompanyEntity>,
    viewModel: ShiftViewModel,
    onShiftSaved: () -> Unit,
    modifier: Modifier = Modifier
) {

    var selectedCompanyId by remember {
        mutableLongStateOf(0L)
    }

    var selectedDate by remember {
        mutableStateOf(LocalDate.now())
    }

    var startTime by remember {
        mutableStateOf(LocalTime.of(9, 0))
    }

    var endTime by remember {
        mutableStateOf(LocalTime.of(17, 0))
    }

    var companyMenuExpanded by remember {
        mutableStateOf(false)
    }

    var showDatePicker by remember {
        mutableStateOf(false)
    }

    var showStartTimePicker by remember {
        mutableStateOf(false)
    }

    var showEndTimePicker by remember {
        mutableStateOf(false)
    }

    var overlapShifts by remember {
        mutableStateOf<List<ShiftEntity>>(emptyList())
    }

    var showOverlapDialog by remember {
        mutableStateOf(false)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    val selectedCompany =
        companies.firstOrNull {
            it.id == selectedCompanyId
        }

    val dateFormatter =
        remember {
            DateTimeFormatter.ofPattern(
                "dd MMM yyyy"
            )
        }

    val timeFormatter =
        remember {
            DateTimeFormatter.ofPattern(
                "h:mm a"
            )
        }

    fun saveShift(
        allowOverlap: Boolean
    ) {

        errorMessage = null

        viewModel.createShift(
            companyId =
                selectedCompanyId,

            companyName =
                selectedCompany?.name
                    ?: "Company",

            notificationsEnabled =
                selectedCompany
                    ?.notificationsEnabled
                    ?: false,

            date =
                selectedDate,

            startTime =
                startTime,

            endTime =
                endTime,

            allowOverlap =
                allowOverlap,

            onOverlapFound = { overlaps ->

                overlapShifts = overlaps
                showOverlapDialog = true
            },

            onSuccess = {

                onShiftSaved()
            },

            onError = { message ->

                errorMessage = message
            }
        )
    }

    Column(
        modifier = modifier
            .padding(24.dp)
    ) {

        Text(
            text = "Shift details",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        // COMPANY
        ExposedDropdownMenuBox(
            expanded = companyMenuExpanded,

            onExpandedChange = {
                companyMenuExpanded = it
            }
        ) {

            OutlinedTextField(
                value =
                    selectedCompany?.name
                        ?: "Select company",

                onValueChange = {},

                readOnly = true,

                label = {
                    Text("Company")
                },

                trailingIcon = {

                    ExposedDropdownMenuDefaults
                        .TrailingIcon(
                            expanded =
                                companyMenuExpanded
                        )
                },

                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = companyMenuExpanded,

                onDismissRequest = {
                    companyMenuExpanded = false
                }
            ) {

                companies.forEach { company ->

                    DropdownMenuItem(
                        text = {
                            Text(company.name)
                        },

                        onClick = {

                            selectedCompanyId =
                                company.id

                            companyMenuExpanded =
                                false

                            errorMessage = null
                        }
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        // DATE
        Text(
            text = "Shift date",
            style = MaterialTheme.typography.labelLarge
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        OutlinedButton(
            onClick = {
                showDatePicker = true
            },

            modifier =
                Modifier.fillMaxWidth()
        ) {

            Text(
                text =
                    selectedDate.format(
                        dateFormatter
                    )
            )
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        // START / END TIMES
        Row(
            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            Column(
                modifier =
                    Modifier.weight(1f)
            ) {

                Text(
                    text = "Start time",
                    style =
                        MaterialTheme.typography.labelLarge
                )

                Spacer(
                    modifier =
                        Modifier.height(8.dp)
                )

                OutlinedButton(
                    onClick = {
                        showStartTimePicker = true
                    },

                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Text(
                        text =
                            startTime.format(
                                timeFormatter
                            )
                    )
                }
            }

            Column(
                modifier =
                    Modifier.weight(1f)
            ) {

                Text(
                    text = "End time",
                    style =
                        MaterialTheme.typography.labelLarge
                )

                Spacer(
                    modifier =
                        Modifier.height(8.dp)
                )

                OutlinedButton(
                    onClick = {
                        showEndTimePicker = true
                    },

                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Text(
                        text =
                            endTime.format(
                                timeFormatter
                            )
                    )
                }
            }
        }

        // OVERNIGHT MESSAGE
        if (endTime <= startTime) {

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            Text(
                text =
                    "Overnight shift: this shift will end the following day.",

                style =
                    MaterialTheme.typography.bodyMedium,

                color =
                    MaterialTheme.colorScheme.primary
            )
        }

        errorMessage?.let { message ->

            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )

            Text(
                text = message,
                color =
                    MaterialTheme.colorScheme.error
            )
        }

        Spacer(
            modifier = Modifier.height(32.dp)
        )

        Button(
            onClick = {
                saveShift(
                    allowOverlap = false
                )
            },

            modifier =
                Modifier.fillMaxWidth(),

            enabled =
                companies.isNotEmpty()
        ) {

            Text("Save shift")
        }

        if (companies.isEmpty()) {

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            Text(
                text =
                    "Add an active company before creating a shift.",

                color =
                    MaterialTheme.colorScheme.error,

                style =
                    MaterialTheme.typography.bodyMedium
            )
        }
    }

    // DATE PICKER
    if (showDatePicker) {

        val datePickerState =
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
                showDatePicker = false
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        datePickerState
                            .selectedDateMillis
                            ?.let { millis ->

                                selectedDate =
                                    Instant
                                        .ofEpochMilli(
                                            millis
                                        )
                                        .atZone(
                                            ZoneId.systemDefault()
                                        )
                                        .toLocalDate()
                            }

                        showDatePicker = false
                    }
                ) {

                    Text("OK")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showDatePicker = false
                    }
                ) {

                    Text("Cancel")
                }
            }
        ) {

            DatePicker(
                state = datePickerState
            )
        }
    }

    // START TIME PICKER
    if (showStartTimePicker) {

        ShiftTimePickerDialog(
            title = "Start time",

            initialTime =
                startTime,

            onDismiss = {
                showStartTimePicker = false
            },

            onConfirm = { time ->

                startTime = time

                showStartTimePicker =
                    false
            }
        )
    }

    // END TIME PICKER
    if (showEndTimePicker) {

        ShiftTimePickerDialog(
            title = "End time",

            initialTime =
                endTime,

            onDismiss = {
                showEndTimePicker = false
            },

            onConfirm = { time ->

                endTime = time

                showEndTimePicker =
                    false
            }
        )
    }

    // OVERLAP WARNING
    if (showOverlapDialog) {

        AlertDialog(
            onDismissRequest = {
                showOverlapDialog = false
            },

            title = {

                Text(
                    "Shift overlap detected"
                )
            },

            text = {

                Column {

                    Text(
                        text =
                            "This shift overlaps ${overlapShifts.size} existing shift(s)."
                    )

                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )

                    Text(
                        text =
                            "You can change the shift time or add it anyway."
                    )
                }
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        showOverlapDialog =
                            false

                        saveShift(
                            allowOverlap = true
                        )
                    }
                ) {

                    Text("Add anyway")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showOverlapDialog =
                            false
                    }
                ) {

                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShiftTimePickerDialog(
    title: String,
    initialTime: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit
) {

    val state =
        rememberTimePickerState(
            initialHour =
                initialTime.hour,

            initialMinute =
                initialTime.minute,

            is24Hour = false
        )

    AlertDialog(
        onDismissRequest =
            onDismiss,

        title = {
            Text(title)
        },

        text = {

            TimePicker(
                state = state
            )
        },

        confirmButton = {

            TextButton(
                onClick = {

                    onConfirm(
                        LocalTime.of(
                            state.hour,
                            state.minute
                        )
                    )
                }
            ) {

                Text("OK")
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