package com.myrota.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.myrota.app.MyRotaApplication
import com.myrota.app.notifications.ShiftReminderScheduler
import com.myrota.app.ui.breaks.BreakViewModel
import com.myrota.app.ui.breaks.BreakViewModelFactory
import com.myrota.app.ui.calendar.CalendarScreen
import com.myrota.app.ui.companies.AddCompanyScreen
import com.myrota.app.ui.companies.CompanyListScreen
import com.myrota.app.ui.companies.CompanyViewModel
import com.myrota.app.ui.companies.CompanyViewModelFactory
import com.myrota.app.ui.companies.EditCompanyScreen
import com.myrota.app.ui.dashboard.DashboardScreen
import com.myrota.app.ui.reports.ReportViewModel
import com.myrota.app.ui.reports.ReportViewModelFactory
import com.myrota.app.ui.reports.ReportsScreen
import com.myrota.app.ui.settings.SettingsScreen
import com.myrota.app.ui.settings.SettingsViewModel
import com.myrota.app.ui.settings.SettingsViewModelFactory
import com.myrota.app.ui.shifts.AddShiftScreen
import com.myrota.app.ui.shifts.EditShiftScreen
import com.myrota.app.ui.shifts.ShiftListScreen
import com.myrota.app.ui.shifts.ShiftViewModel
import com.myrota.app.ui.shifts.ShiftViewModelFactory

private const val COMPANIES_ROUTE =
    "companies"

private const val ADD_COMPANY_ROUTE =
    "add_company"

private const val EDIT_COMPANY_ROUTE =
    "edit_company/{companyId}"

private const val SHIFTS_ROUTE =
    "shifts"

private const val ADD_SHIFT_ROUTE =
    "add_shift"

private const val EDIT_SHIFT_ROUTE =
    "edit_shift/{shiftId}"

private fun editCompanyRoute(
    companyId: Long
): String {

    return "edit_company/$companyId"
}

private fun editShiftRoute(
    shiftId: Long
): String {

    return "edit_shift/$shiftId"
}

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun MyRotaApp() {

    val navController =
        rememberNavController()

    val currentBackStackEntry by
    navController
        .currentBackStackEntryAsState()

    val currentRoute =
        currentBackStackEntry
            ?.destination
            ?.route

    val currentDestination =
        MyRotaDestination
            .entries
            .firstOrNull {
                it.route ==
                        currentRoute
            }
            ?: MyRotaDestination.HOME

    val application =
        LocalContext
            .current
            .applicationContext as
                MyRotaApplication

    val companyViewModel:
            CompanyViewModel =
        viewModel(
            factory =
                CompanyViewModelFactory(
                    repository =
                        application
                            .companyRepository
                )
        )

    val shiftViewModel:
            ShiftViewModel =
        viewModel(
            factory =
                ShiftViewModelFactory(
                    repository =
                        application
                            .shiftRepository,

                    reminderScheduler =
                        ShiftReminderScheduler(
                            application
                        ),

                    settingsRepository =
                        application
                            .settingsRepository
                )
        )

    val breakViewModel:
            BreakViewModel =
        viewModel(
            factory =
                BreakViewModelFactory(
                    repository =
                        application
                            .breakRepository,

                    shiftRepository =
                        application
                            .shiftRepository
                )
        )

    val reportViewModel:
            ReportViewModel =
        viewModel(
            factory =
                ReportViewModelFactory(
                    shiftRepository =
                        application
                            .shiftRepository,

                    breakRepository =
                        application
                            .breakRepository
                )
        )

    val settingsViewModel:
            SettingsViewModel =
        viewModel(
            factory =
                SettingsViewModelFactory(
                    repository =
                        application
                            .settingsRepository
                )
        )

    /*
     * ACTIVE companies only.
     *
     * Use this list when the user is choosing
     * a company for a new shift.
     */
    val companies by
    companyViewModel
        .companies
        .collectAsStateWithLifecycle()

    /*
     * ACTIVE + ARCHIVED companies.
     *
     * Historical screens use this list so old
     * shifts still know their company name,
     * colour and hourly rate.
     */
    val allCompanies by
    companyViewModel
        .allCompanies
        .collectAsStateWithLifecycle()

    Scaffold(
        topBar = {

            TopAppBar(
                title = {

                    Text(
                        text =
                            when {

                                currentRoute ==
                                        COMPANIES_ROUTE ->
                                    "Companies"

                                currentRoute ==
                                        ADD_COMPANY_ROUTE ->
                                    "Add Company"

                                currentRoute
                                    ?.startsWith(
                                        "edit_company/"
                                    ) == true ->
                                    "Edit Company"

                                currentRoute ==
                                        SHIFTS_ROUTE ->
                                    "Shifts"

                                currentRoute ==
                                        ADD_SHIFT_ROUTE ->
                                    "Add Shift"

                                currentRoute
                                    ?.startsWith(
                                        "edit_shift/"
                                    ) == true ->
                                    "Edit Shift"

                                currentDestination ==
                                        MyRotaDestination.HOME ->
                                    "MyRota"

                                else ->
                                    currentDestination.label
                            }
                    )
                }
            )
        },

        bottomBar = {

            NavigationBar {

                MyRotaDestination
                    .entries
                    .forEach { destination ->

                        NavigationBarItem(
                            selected =
                                currentRoute ==
                                        destination.route,

                            onClick = {

                                if (
                                    currentRoute !=
                                    destination.route
                                ) {

                                    navController
                                        .navigate(
                                            destination.route
                                        ) {

                                            popUpTo(
                                                MyRotaDestination
                                                    .HOME
                                                    .route
                                            ) {

                                                saveState =
                                                    true
                                            }

                                            launchSingleTop =
                                                true

                                            restoreState =
                                                true
                                        }
                                }
                            },

                            icon = {

                                Icon(
                                    imageVector =
                                        destination.icon,

                                    contentDescription =
                                        destination.label
                                )
                            },

                            label = {

                                Text(
                                    destination.label
                                )
                            }
                        )
                    }
            }
        }
    ) { innerPadding ->

        NavHost(
            navController =
                navController,

            startDestination =
                MyRotaDestination
                    .HOME
                    .route,

            modifier =
                Modifier.padding(
                    innerPadding
                )
        ) {

            /*
             * HOME
             *
             * Use allCompanies because today's
             * existing shifts might belong to a
             * company that was later archived.
             */
            composable(
                route =
                    MyRotaDestination
                        .HOME
                        .route
            ) {

                DashboardScreen(
                    shiftViewModel =
                        shiftViewModel,

                    breakViewModel =
                        breakViewModel,

                    companies =
                        allCompanies,

                    onCompaniesClick = {

                        navController
                            .navigate(
                                COMPANIES_ROUTE
                            )
                    },

                    onAddShiftClick = {

                        navController
                            .navigate(
                                ADD_SHIFT_ROUTE
                            )
                    },

                    onViewShiftsClick = {

                        navController
                            .navigate(
                                SHIFTS_ROUTE
                            )
                    },

                    onShiftClick = { shiftId ->

                        navController
                            .navigate(
                                editShiftRoute(
                                    shiftId
                                )
                            )
                    }
                )
            }

            /*
             * CALENDAR
             *
             * Historical calendar entries should
             * continue showing archived companies.
             */
            composable(
                route =
                    MyRotaDestination
                        .CALENDAR
                        .route
            ) {

                CalendarScreen(
                    shiftViewModel =
                        shiftViewModel,

                    companies =
                        allCompanies,

                    onShiftClick = { shiftId ->

                        navController
                            .navigate(
                                editShiftRoute(
                                    shiftId
                                )
                            )
                    }
                )
            }

            /*
             * REPORTS
             *
             * allCompanies preserves:
             * - historical names
             * - hourly rates
             * - company grouping
             */
            composable(
                route =
                    MyRotaDestination
                        .REPORTS
                        .route
            ) {

                ReportsScreen(
                    viewModel =
                        reportViewModel,

                    companies =
                        allCompanies
                )
            }

            /*
             * SETTINGS
             *
             * Use allCompanies so future alarms
             * can still resolve an existing shift's
             * company information.
             */
            composable(
                route =
                    MyRotaDestination
                        .SETTINGS
                        .route
            ) {

                SettingsScreen(
                    viewModel =
                        settingsViewModel,

                    shiftViewModel =
                        shiftViewModel,

                    companies =
                        allCompanies
                )
            }

            /*
             * COMPANY MANAGEMENT
             *
             * Keep active list behaviour unchanged.
             */
            composable(
                route =
                    COMPANIES_ROUTE
            ) {

                CompanyListScreen(
                    viewModel =
                        companyViewModel,

                    onAddCompanyClick = {

                        navController
                            .navigate(
                                ADD_COMPANY_ROUTE
                            )
                    },

                    onCompanyClick = { companyId ->

                        navController
                            .navigate(
                                editCompanyRoute(
                                    companyId
                                )
                            )
                    }
                )
            }

            composable(
                route =
                    ADD_COMPANY_ROUTE
            ) {

                AddCompanyScreen(
                    viewModel =
                        companyViewModel,

                    onCompanySaved = {

                        navController
                            .popBackStack()
                    }
                )
            }

            composable(
                route =
                    EDIT_COMPANY_ROUTE
            ) { backStackEntry ->

                val companyId =
                    backStackEntry
                        .arguments
                        ?.getString(
                            "companyId"
                        )
                        ?.toLongOrNull()
                        ?: return@composable

                EditCompanyScreen(
                    companyId =
                        companyId,

                    viewModel =
                        companyViewModel,

                    onCompanySaved = {

                        navController
                            .popBackStack()
                    }
                )
            }

            /*
             * SHIFT LIST
             *
             * Historical shifts need allCompanies.
             */
            composable(
                route =
                    SHIFTS_ROUTE
            ) {

                ShiftListScreen(
                    viewModel =
                        shiftViewModel,

                    breakViewModel =
                        breakViewModel,

                    companies =
                        allCompanies,

                    onShiftClick = { shiftId ->

                        navController
                            .navigate(
                                editShiftRoute(
                                    shiftId
                                )
                            )
                    }
                )
            }

            /*
             * ADD SHIFT
             *
             * Only ACTIVE companies can be chosen
             * for new shifts.
             */
            composable(
                route =
                    ADD_SHIFT_ROUTE
            ) {

                AddShiftScreen(
                    companies =
                        companies,

                    viewModel =
                        shiftViewModel,

                    onShiftSaved = {

                        navController
                            .popBackStack()
                    }
                )
            }

            /*
             * EDIT SHIFT
             *
             * allCompanies is used so an existing
             * shift assigned to an archived company
             * still resolves its current company.
             *
             * Add Shift still prevents creating new
             * shifts for archived companies.
             */
            composable(
                route =
                    EDIT_SHIFT_ROUTE
            ) { backStackEntry ->

                val shiftId =
                    backStackEntry
                        .arguments
                        ?.getString(
                            "shiftId"
                        )
                        ?.toLongOrNull()
                        ?: return@composable

                EditShiftScreen(
                    shiftId =
                        shiftId,

                    companies =
                        allCompanies,

                    viewModel =
                        shiftViewModel,

                    onShiftSaved = {

                        navController
                            .popBackStack()
                    }
                )
            }
        }
    }
}