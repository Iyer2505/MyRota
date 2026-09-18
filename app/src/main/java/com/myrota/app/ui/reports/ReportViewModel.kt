package com.myrota.app.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.myrota.app.data.repository.BreakRepository
import com.myrota.app.data.repository.ShiftRepository
import com.myrota.app.local.BreakEntity
import com.myrota.app.local.ShiftEntity
import com.myrota.app.local.ShiftStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

data class DailyReportShift(
    val shift: ShiftEntity,
    val breaks: List<BreakEntity>
)

enum class ReportMode {
    DAILY,
    WEEKLY,
    MONTHLY
}

@OptIn(ExperimentalCoroutinesApi::class)
class ReportViewModel(
    private val shiftRepository: ShiftRepository,
    private val breakRepository: BreakRepository
) : ViewModel() {

    private val zoneId =
        ZoneId.systemDefault()

    private val _reportMode =
        MutableStateFlow(
            ReportMode.DAILY
        )

    val reportMode: StateFlow<ReportMode> =
        _reportMode

    private val _selectedDate =
        MutableStateFlow(
            LocalDate.now()
        )

    val selectedDate: StateFlow<LocalDate> =
        _selectedDate

    private val _selectedWeekStart =
        MutableStateFlow(
            getWeekStart(
                LocalDate.now()
            )
        )

    val selectedWeekStart:
            StateFlow<LocalDate> =
        _selectedWeekStart

    private val _selectedMonth =
        MutableStateFlow(
            YearMonth.now()
        )

    val selectedMonth:
            StateFlow<YearMonth> =
        _selectedMonth

    val dailyReport:
            StateFlow<List<DailyReportShift>> =
        combine(
            shiftRepository.observeAllShifts(),
            selectedDate
        ) { shifts, date ->

            shifts
                .filter { shift ->

                    if (
                        shift.status !=
                        ShiftStatus.COMPLETED
                    ) {
                        return@filter false
                    }

                    val shiftDate =
                        getShiftDate(
                            shift
                        )

                    shiftDate == date
                }
                .sortedBy {
                    it.scheduledStartEpochMillis
                }
        }
            .flatMapLatest { shifts ->

                buildReportFlow(
                    shifts
                )
            }
            .stateIn(
                scope = viewModelScope,

                started =
                    SharingStarted
                        .WhileSubscribed(
                            5_000
                        ),

                initialValue =
                    emptyList()
            )

    val weeklyReport:
            StateFlow<List<DailyReportShift>> =
        combine(
            shiftRepository.observeAllShifts(),
            selectedWeekStart
        ) { shifts, weekStart ->

            val weekEnd =
                weekStart.plusDays(6)

            shifts
                .filter { shift ->

                    if (
                        shift.status !=
                        ShiftStatus.COMPLETED
                    ) {
                        return@filter false
                    }

                    val shiftDate =
                        getShiftDate(
                            shift
                        )

                    !shiftDate.isBefore(
                        weekStart
                    ) &&
                            !shiftDate.isAfter(
                                weekEnd
                            )
                }
                .sortedBy {
                    it.scheduledStartEpochMillis
                }
        }
            .flatMapLatest { shifts ->

                buildReportFlow(
                    shifts
                )
            }
            .stateIn(
                scope = viewModelScope,

                started =
                    SharingStarted
                        .WhileSubscribed(
                            5_000
                        ),

                initialValue =
                    emptyList()
            )

    val monthlyReport:
            StateFlow<List<DailyReportShift>> =
        combine(
            shiftRepository.observeAllShifts(),
            selectedMonth
        ) { shifts, month ->

            shifts
                .filter { shift ->

                    if (
                        shift.status !=
                        ShiftStatus.COMPLETED
                    ) {
                        return@filter false
                    }

                    val shiftDate =
                        getShiftDate(
                            shift
                        )

                    YearMonth.from(
                        shiftDate
                    ) == month
                }
                .sortedBy {
                    it.scheduledStartEpochMillis
                }
        }
            .flatMapLatest { shifts ->

                buildReportFlow(
                    shifts
                )
            }
            .stateIn(
                scope = viewModelScope,

                started =
                    SharingStarted
                        .WhileSubscribed(
                            5_000
                        ),

                initialValue =
                    emptyList()
            )

    fun setReportMode(
        mode: ReportMode
    ) {

        _reportMode.value =
            mode
    }

    fun setSelectedDate(
        date: LocalDate
    ) {

        _selectedDate.value =
            date
    }

    fun previousDay() {

        _selectedDate.value =
            _selectedDate.value
                .minusDays(1)
    }

    fun nextDay() {

        _selectedDate.value =
            _selectedDate.value
                .plusDays(1)
    }

    fun today() {

        _selectedDate.value =
            LocalDate.now()
    }

    fun previousWeek() {

        _selectedWeekStart.value =
            _selectedWeekStart.value
                .minusWeeks(1)
    }

    fun nextWeek() {

        _selectedWeekStart.value =
            _selectedWeekStart.value
                .plusWeeks(1)
    }

    fun currentWeek() {

        _selectedWeekStart.value =
            getWeekStart(
                LocalDate.now()
            )
    }

    fun previousMonth() {

        _selectedMonth.value =
            _selectedMonth.value
                .minusMonths(1)
    }

    fun nextMonth() {

        _selectedMonth.value =
            _selectedMonth.value
                .plusMonths(1)
    }

    fun currentMonth() {

        _selectedMonth.value =
            YearMonth.now()
    }

    private fun getShiftDate(
        shift: ShiftEntity
    ): LocalDate {

        return Instant
            .ofEpochMilli(
                shift.scheduledStartEpochMillis
            )
            .atZone(
                zoneId
            )
            .toLocalDate()
    }

    private fun buildReportFlow(
        shifts: List<ShiftEntity>
    ): Flow<List<DailyReportShift>> {

        if (shifts.isEmpty()) {

            return flowOf(
                emptyList()
            )
        }

        val reportFlows:
                List<Flow<DailyReportShift>> =
            shifts.map { shift ->

                breakRepository
                    .observeBreaksForShift(
                        shift.id
                    )
                    .map { breaks ->

                        DailyReportShift(
                            shift = shift,
                            breaks = breaks
                        )
                    }
            }

        return combine(
            reportFlows
        ) { reports ->

            reports.toList()
        }
    }

    private fun getWeekStart(
        date: LocalDate
    ): LocalDate {

        return date.with(
            TemporalAdjusters
                .previousOrSame(
                    DayOfWeek.MONDAY
                )
        )
    }
}

class ReportViewModelFactory(
    private val shiftRepository:
    ShiftRepository,

    private val breakRepository:
    BreakRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (
            modelClass.isAssignableFrom(
                ReportViewModel::class.java
            )
        ) {

            return ReportViewModel(
                shiftRepository =
                    shiftRepository,

                breakRepository =
                    breakRepository
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}