package com.myrota.app.ui.companies

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.myrota.app.local.CompanyEntity

@Composable
fun CompanyListScreen(
    viewModel: CompanyViewModel,
    onAddCompanyClick: () -> Unit,
    onCompanyClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {

    val companies by
    viewModel.companies.collectAsStateWithLifecycle()

    if (companies.isEmpty()) {

        EmptyCompanyState(
            onAddCompanyClick = onAddCompanyClick,
            modifier = modifier
        )

    } else {

        CompanyList(
            companies = companies,
            onAddCompanyClick = onAddCompanyClick,
            onCompanyClick = onCompanyClick,
            modifier = modifier
        )
    }
}

@Composable
private fun EmptyCompanyState(
    onAddCompanyClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement =
            Arrangement.Center
    ) {

        Text(
            text = "No companies yet",
            style =
                MaterialTheme.typography.headlineSmall
        )

        Text(
            text =
                "Add a company to start organising your shifts.",
            style =
                MaterialTheme.typography.bodyLarge,
            color =
                MaterialTheme.colorScheme.onSurfaceVariant,
            modifier =
                Modifier.padding(top = 8.dp)
        )

        Button(
            onClick = onAddCompanyClick,
            modifier =
                Modifier.padding(top = 24.dp)
        ) {
            Text("Add your first company")
        }
    }
}

@Composable
private fun CompanyList(
    companies: List<CompanyEntity>,
    onAddCompanyClick: () -> Unit,
    onCompanyClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding =
            PaddingValues(16.dp),
        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {

        item {
            Button(
                onClick = onAddCompanyClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add company")
            }
        }

        items(
            items = companies,
            key = { company ->
                company.id
            }
        ) { company ->

            CompanyCard(
                company = company,
                onClick = {
                    onCompanyClick(company.id)
                }
            )
        }
    }
}

@Composable
private fun CompanyCard(
    company: CompanyEntity,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick
            )
    ) {

        Column(
            modifier =
                Modifier.padding(16.dp)
        ) {

            Text(
                text = company.name,
                style =
                    MaterialTheme.typography.titleMedium
            )

            company.description
                ?.takeIf {
                    it.isNotBlank()
                }
                ?.let { description ->

                    Text(
                        text = description,
                        style =
                            MaterialTheme.typography.bodyMedium,
                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier =
                            Modifier.padding(top = 4.dp)
                    )
                }

            company.hourlyRatePence
                ?.let { rate ->

                    val pounds =
                        rate / 100.0

                    Text(
                        text =
                            "Hourly rate: £%.2f".format(
                                pounds
                            ),
                        style =
                            MaterialTheme.typography.bodyMedium,
                        modifier =
                            Modifier.padding(top = 8.dp)
                    )
                }
        }
    }
}