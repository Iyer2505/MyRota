package com.myrota.app.ui.companies

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AddCompanyScreen(
    viewModel: CompanyViewModel,
    onCompanySaved: () -> Unit,
    modifier: Modifier = Modifier
) {

    var name by remember {
        mutableStateOf("")
    }

    var description by remember {
        mutableStateOf("")
    }

    var hourlyRate by remember {
        mutableStateOf("")
    }

    var notificationsEnabled by remember {
        mutableStateOf(true)
    }

    var selectedColorArgb by remember {
        mutableLongStateOf(
            AndroidColor.rgb(
                33,
                150,
                243
            ).toLong()
        )
    }

    var showColorPicker by remember {
        mutableStateOf(false)
    }

    var showDuplicateWarning by remember {
        mutableStateOf(false)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    fun saveCompany(
        allowDuplicate: Boolean
    ) {

        viewModel.addCompany(
            name = name,
            description = description,
            colorArgb = selectedColorArgb,
            hourlyRateText = hourlyRate,
            notificationsEnabled =
                notificationsEnabled,
            allowDuplicate = allowDuplicate,

            onDuplicateFound = {
                showDuplicateWarning = true
            },

            onSuccess = {
                onCompanySaved()
            },

            onError = { message ->
                errorMessage = message
            }
        )
    }

    Column(
        modifier = modifier
            .verticalScroll(
                rememberScrollState()
            )
            .padding(24.dp)
    ) {

        Text(
            text = "Company details",
            style =
                MaterialTheme.typography.headlineSmall
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                errorMessage = null
            },
            label = {
                Text("Company name *")
            },
            singleLine = true,
            modifier =
                Modifier.fillMaxWidth()
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        OutlinedTextField(
            value = description,
            onValueChange = {
                description = it
            },
            label = {
                Text("Description")
            },
            minLines = 3,
            modifier =
                Modifier.fillMaxWidth()
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Text(
            text = "Company colour",
            style =
                MaterialTheme.typography.titleMedium
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Row(
            verticalAlignment =
                Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.spacedBy(16.dp)
        ) {

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = Color(
                            selectedColorArgb
                        ),
                        shape = CircleShape
                    )
                    .clickable {
                        showColorPicker = true
                    }
            )

            TextButton(
                onClick = {
                    showColorPicker = true
                }
            ) {
                Text("Choose colour")
            }
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        OutlinedTextField(
            value = hourlyRate,
            onValueChange = {
                hourlyRate = it
                errorMessage = null
            },
            label = {
                Text("Hourly rate")
            },
            prefix = {
                Text("£")
            },
            supportingText = {
                Text("Optional")
            },
            singleLine = true,
            modifier =
                Modifier.fillMaxWidth()
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        HorizontalDivider()

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Row(
            modifier =
                Modifier.fillMaxWidth(),
            verticalAlignment =
                Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {

            Column(
                modifier =
                    Modifier.weight(1f)
            ) {

                Text(
                    text =
                        "Shift notifications",
                    style =
                        MaterialTheme.typography.titleMedium
                )

                Text(
                    text =
                        "Allow reminders for this company.",
                    style =
                        MaterialTheme.typography.bodyMedium,
                    color =
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Switch(
                checked =
                    notificationsEnabled,
                onCheckedChange = {
                    notificationsEnabled = it
                }
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
                    MaterialTheme.colorScheme.error,
                style =
                    MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(
            modifier = Modifier.height(32.dp)
        )

        Button(
            onClick = {
                saveCompany(
                    allowDuplicate = false
                )
            },
            modifier =
                Modifier.fillMaxWidth()
        ) {

            Text("Save company")
        }
    }

    if (showColorPicker) {

        CompanyColorPickerDialog(
            initialArgb =
                selectedColorArgb,

            onDismiss = {
                showColorPicker = false
            },

            onConfirm = { color ->

                selectedColorArgb =
                    color

                showColorPicker = false
            }
        )
    }

    if (showDuplicateWarning) {

        AlertDialog(
            onDismissRequest = {
                showDuplicateWarning = false
            },

            title = {
                Text("Company already exists")
            },

            text = {
                Text(
                    "A company named \"${name.trim()}\" already exists. Do you want to add another company with the same name?"
                )
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        showDuplicateWarning =
                            false

                        saveCompany(
                            allowDuplicate = true
                        )
                    }
                ) {

                    Text("Add anyway")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showDuplicateWarning =
                            false
                    }
                ) {

                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun CompanyColorPickerDialog(
    initialArgb: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {

    val initialHsv =
        remember(initialArgb) {

            FloatArray(3).also {

                AndroidColor.colorToHSV(
                    initialArgb.toInt(),
                    it
                )
            }
        }

    var hue by remember {
        mutableFloatStateOf(
            initialHsv[0]
        )
    }

    var saturation by remember {
        mutableFloatStateOf(
            initialHsv[1]
        )
    }

    var value by remember {
        mutableFloatStateOf(
            initialHsv[2]
        )
    }

    val previewArgb =
        AndroidColor.HSVToColor(
            floatArrayOf(
                hue,
                saturation,
                value
            )
        ).toLong()

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text("Choose company colour")
        },

        text = {

            Column {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .background(
                            Color(previewArgb)
                        )
                )

                Spacer(
                    modifier =
                        Modifier.height(20.dp)
                )

                Text("Hue")

                Slider(
                    value = hue,
                    onValueChange = {
                        hue = it
                    },
                    valueRange =
                        0f..360f
                )

                Text("Saturation")

                Slider(
                    value = saturation,
                    onValueChange = {
                        saturation = it
                    },
                    valueRange =
                        0f..1f
                )

                Text("Brightness")

                Slider(
                    value = value,
                    onValueChange = {
                        value = it
                    },
                    valueRange =
                        0f..1f
                )
            }
        },

        confirmButton = {

            TextButton(
                onClick = {
                    onConfirm(
                        previewArgb
                    )
                }
            ) {

                Text("Use colour")
            }
        },

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {

                Text("Cancel")
            }
        }
    )
}