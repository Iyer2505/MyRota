package com.myrota.app.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BreakDao {

    @Query(
        """
        SELECT *
        FROM breaks
        WHERE shiftId = :shiftId
        ORDER BY createdAtEpochMillis ASC
        """
    )
    fun observeBreaksForShift(
        shiftId: Long
    ): Flow<List<BreakEntity>>

    @Query(
        """
        SELECT *
        FROM breaks
        WHERE id = :breakId
        LIMIT 1
        """
    )
    fun observeBreakById(
        breakId: Long
    ): Flow<BreakEntity?>

    @Insert(
        onConflict = OnConflictStrategy.ABORT
    )
    suspend fun insertBreak(
        breakEntity: BreakEntity
    ): Long

    @Update
    suspend fun updateBreak(
        breakEntity: BreakEntity
    )

    @Delete
    suspend fun deleteBreak(
        breakEntity: BreakEntity
    )

    @Query(
        """
        SELECT COALESCE(
            SUM(durationMinutes),
            0
        )
        FROM breaks
        WHERE shiftId = :shiftId
          AND isPaid = 0
        """
    )
    suspend fun getTotalUnpaidBreakMinutes(
        shiftId: Long
    ): Int

    @Query(
        """
        SELECT COALESCE(
            SUM(durationMinutes),
            0
        )
        FROM breaks
        WHERE shiftId = :shiftId
          AND isPaid = 1
        """
    )
    suspend fun getTotalPaidBreakMinutes(
        shiftId: Long
    ): Int

    @Query(
        """
        DELETE FROM breaks
        WHERE id = :breakId
        """
    )
    suspend fun deleteBreakById(
        breakId: Long
    ): Int
}