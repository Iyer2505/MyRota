package com.myrota.app.data.repository

import com.myrota.app.local.CompanyDao
import com.myrota.app.local.CompanyEntity
import kotlinx.coroutines.flow.Flow

class CompanyRepository(
    private val companyDao: CompanyDao
) {

    fun observeActiveCompanies(): Flow<List<CompanyEntity>> {
        return companyDao.observeActiveCompanies()
    }

    fun observeAllCompanies(): Flow<List<CompanyEntity>> {
        return companyDao.observeAllCompanies()
    }

    fun observeCompanyById(
        companyId: Long
    ): Flow<CompanyEntity?> {
        return companyDao.observeCompanyById(companyId)
    }

    suspend fun insertCompany(
        company: CompanyEntity
    ): Long {
        return companyDao.insertCompany(company)
    }

    suspend fun updateCompany(
        company: CompanyEntity
    ) {
        companyDao.updateCompany(company)
    }

    suspend fun archiveCompany(
        companyId: Long,
        updatedAt: Long
    ) {
        companyDao.archiveCompany(
            companyId = companyId,
            updatedAt = updatedAt
        )
    }

    suspend fun hasActiveCompanyWithName(
        name: String
    ): Boolean {
        return companyDao.countActiveCompaniesWithName(
            name = name.trim()
        ) > 0
    }
}