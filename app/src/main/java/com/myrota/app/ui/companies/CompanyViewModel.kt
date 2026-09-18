package com.myrota.app.ui.companies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.myrota.app.data.repository.CompanyRepository
import com.myrota.app.local.CompanyEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

class CompanyViewModel(
    private val repository: CompanyRepository
) : ViewModel() {

    /*
     * Active companies only.
     *
     * Used for:
     * - Add Shift
     * - normal company selection
     * - Company Management
     */
    val companies: StateFlow<List<CompanyEntity>> =
        repository
            .observeActiveCompanies()
            .stateIn(
                scope = viewModelScope,
                started =
                    SharingStarted.WhileSubscribed(
                        5_000
                    ),
                initialValue =
                    emptyList()
            )

    /*
     * Includes archived companies.
     *
     * Used for:
     * - historical shifts
     * - reports
     * - calendar
     * - dashboard
     *
     * This prevents an archived company from
     * appearing as "Unknown company".
     */
    val allCompanies: StateFlow<List<CompanyEntity>> =
        repository
            .observeAllCompanies()
            .stateIn(
                scope = viewModelScope,
                started =
                    SharingStarted.WhileSubscribed(
                        5_000
                    ),
                initialValue =
                    emptyList()
            )

    fun observeCompany(
        companyId: Long
    ): Flow<CompanyEntity?> {

        return repository
            .observeCompanyById(
                companyId
            )
    }

    fun addCompany(
        name: String,
        description: String,
        colorArgb: Long,
        hourlyRateText: String,
        notificationsEnabled: Boolean,
        allowDuplicate: Boolean,
        onDuplicateFound: () -> Unit,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {

        viewModelScope.launch {

            val cleanedName =
                name.trim()

            if (
                cleanedName.isBlank()
            ) {

                onError(
                    "Company name is required."
                )

                return@launch
            }

            val hourlyRatePence =
                try {

                    parseHourlyRatePence(
                        hourlyRateText
                    )

                } catch (
                    exception: IllegalArgumentException
                ) {

                    onError(
                        exception.message
                            ?: "Invalid hourly rate."
                    )

                    return@launch
                }

            try {

                if (!allowDuplicate) {

                    val duplicateExists =
                        repository
                            .hasActiveCompanyWithName(
                                cleanedName
                            )

                    if (
                        duplicateExists
                    ) {

                        onDuplicateFound()

                        return@launch
                    }
                }

                val now =
                    System.currentTimeMillis()

                repository.insertCompany(
                    CompanyEntity(
                        name =
                            cleanedName,

                        description =
                            description
                                .trim()
                                .takeIf {
                                    it.isNotBlank()
                                },

                        colorArgb =
                            colorArgb,

                        hourlyRatePence =
                            hourlyRatePence,

                        notificationsEnabled =
                            notificationsEnabled,

                        isActive =
                            true,

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
                        ?: "Unable to add company."
                )
            }
        }
    }

    fun updateCompany(
        existingCompany: CompanyEntity,
        name: String,
        description: String,
        colorArgb: Long,
        hourlyRateText: String,
        notificationsEnabled: Boolean,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {

        viewModelScope.launch {

            val cleanedName =
                name.trim()

            if (
                cleanedName.isBlank()
            ) {

                onError(
                    "Company name is required."
                )

                return@launch
            }

            val hourlyRatePence =
                try {

                    parseHourlyRatePence(
                        hourlyRateText
                    )

                } catch (
                    exception: IllegalArgumentException
                ) {

                    onError(
                        exception.message
                            ?: "Invalid hourly rate."
                    )

                    return@launch
                }

            try {

                repository.updateCompany(
                    existingCompany.copy(
                        name =
                            cleanedName,

                        description =
                            description
                                .trim()
                                .takeIf {
                                    it.isNotBlank()
                                },

                        colorArgb =
                            colorArgb,

                        hourlyRatePence =
                            hourlyRatePence,

                        notificationsEnabled =
                            notificationsEnabled,

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
                        ?: "Unable to update company."
                )
            }
        }
    }

    fun archiveCompany(
        companyId: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {

        viewModelScope.launch {

            if (
                companyId <= 0
            ) {

                onError(
                    "Invalid company."
                )

                return@launch
            }

            try {

                repository.archiveCompany(
                    companyId =
                        companyId,

                    updatedAt =
                        System.currentTimeMillis()
                )

                onSuccess()

            } catch (
                exception: Exception
            ) {

                onError(
                    exception.message
                        ?: "Unable to archive company."
                )
            }
        }
    }

    private fun parseHourlyRatePence(
        value: String
    ): Long? {

        val cleaned =
            value
                .trim()
                .replace(
                    "£",
                    ""
                )
                .replace(
                    ",",
                    ""
                )
                .trim()

        if (
            cleaned.isBlank()
        ) {
            return null
        }

        val amount =
            try {

                BigDecimal(
                    cleaned
                )

            } catch (
                exception: NumberFormatException
            ) {

                throw IllegalArgumentException(
                    "Enter a valid hourly rate."
                )
            }

        if (
            amount <
            BigDecimal.ZERO
        ) {

            throw IllegalArgumentException(
                "Hourly rate cannot be negative."
            )
        }

        return try {

            amount
                .movePointRight(2)
                .setScale(
                    0,
                    RoundingMode.HALF_UP
                )
                .longValueExact()

        } catch (
            exception: ArithmeticException
        ) {

            throw IllegalArgumentException(
                "Hourly rate is too large."
            )
        }
    }
}

class CompanyViewModelFactory(
    private val repository:
    CompanyRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (
            modelClass.isAssignableFrom(
                CompanyViewModel::class.java
            )
        ) {

            return CompanyViewModel(
                repository =
                    repository
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}