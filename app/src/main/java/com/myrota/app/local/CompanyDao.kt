package com.myrota.app.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CompanyDao {

    @Query(
        """
        SELECT *
        FROM companies
        WHERE isActive = 1
        ORDER BY name COLLATE NOCASE ASC
        """
    )
    fun observeActiveCompanies(): Flow<List<CompanyEntity>>

    @Query(
        """
        SELECT *
        FROM companies
        ORDER BY name COLLATE NOCASE ASC
        """
    )
    fun observeAllCompanies(): Flow<List<CompanyEntity>>

    @Query(
        """
        SELECT *
        FROM companies
        WHERE id = :companyId
        LIMIT 1
        """
    )
    fun observeCompanyById(
        companyId: Long
    ): Flow<CompanyEntity?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCompany(
        company: CompanyEntity
    ): Long

    @Update
    suspend fun updateCompany(
        company: CompanyEntity
    )

    @Query(
        """
        UPDATE companies
        SET isActive = 0,
            updatedAtEpochMillis = :updatedAt
        WHERE id = :companyId
        """
    )
    suspend fun archiveCompany(
        companyId: Long,
        updatedAt: Long
    )

    @Query(
        """
        SELECT COUNT(*)
        FROM companies
        WHERE LOWER(name) = LOWER(:name)
          AND isActive = 1
        """
    )
    suspend fun countActiveCompaniesWithName(
        name: String
    ): Int
}