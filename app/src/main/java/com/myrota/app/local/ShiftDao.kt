package com.myrota.app.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ShiftDao {

    @Query(
        """
        SELECT *
        FROM shifts
        ORDER BY scheduledStartEpochMillis ASC
        """
    )
    fun observeAllShifts(): Flow<List<ShiftEntity>>

    @Query(
        """
        SELECT *
        FROM shifts
        WHERE id = :shiftId
        LIMIT 1
        """
    )
    fun observeShiftById(
        shiftId: Long
    ): Flow<ShiftEntity?>

    @Query(
        """
        SELECT *
        FROM shifts
        WHERE companyId = :companyId
        ORDER BY scheduledStartEpochMillis ASC
        """
    )
    fun observeShiftsForCompany(
        companyId: Long
    ): Flow<List<ShiftEntity>>

    @Query(
        """
        SELECT *
        FROM shifts
        WHERE scheduledStartEpochMillis >= :startMillis
          AND scheduledStartEpochMillis < :endMillis
          AND status != 'CANCELLED'
        ORDER BY scheduledStartEpochMillis ASC
        """
    )
    fun observeShiftsBetween(
        startMillis: Long,
        endMillis: Long
    ): Flow<List<ShiftEntity>>

    @Query(
        """
        SELECT *
        FROM shifts
        WHERE status != 'CANCELLED'
          AND scheduledStartEpochMillis < :endMillis
          AND scheduledEndEpochMillis > :startMillis
        ORDER BY scheduledStartEpochMillis ASC
        """
    )
    suspend fun findOverlappingShifts(
        startMillis: Long,
        endMillis: Long
    ): List<ShiftEntity>

    @Query(
        """
        SELECT *
        FROM shifts
        WHERE id != :shiftId
          AND status != 'CANCELLED'
          AND scheduledStartEpochMillis < :endMillis
          AND scheduledEndEpochMillis > :startMillis
        ORDER BY scheduledStartEpochMillis ASC
        """
    )
    suspend fun findOverlappingShiftsExcluding(
        shiftId: Long,
        startMillis: Long,
        endMillis: Long
    ): List<ShiftEntity>

    @Insert(
        onConflict = OnConflictStrategy.ABORT
    )
    suspend fun insertShift(
        shift: ShiftEntity
    ): Long

    @Update
    suspend fun updateShift(
        shift: ShiftEntity
    )

    @Query(
        """
        UPDATE shifts
        SET status = 'CANCELLED',
            updatedAtEpochMillis = :updatedAt
        WHERE id = :shiftId
        """
    )
    suspend fun cancelShift(
        shiftId: Long,
        updatedAt: Long
    )

    @Query(
        """
        UPDATE shifts
        SET status = 'IN_PROGRESS',
            actualStartEpochMillis = :actualStart,
            updatedAtEpochMillis = :updatedAt
        WHERE id = :shiftId
          AND status = 'SCHEDULED'
        """
    )
    suspend fun startShift(
        shiftId: Long,
        actualStart: Long,
        updatedAt: Long
    ): Int

    @Query(
        """
        UPDATE shifts
        SET status = 'COMPLETED',
            actualEndEpochMillis = :actualEnd,
            updatedAtEpochMillis = :updatedAt
        WHERE id = :shiftId
          AND status = 'IN_PROGRESS'
          AND actualStartEpochMillis IS NOT NULL
        """
    )
    suspend fun endShift(
        shiftId: Long,
        actualEnd: Long,
        updatedAt: Long
    ): Int
}