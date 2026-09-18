package com.myrota.app.data.repository

import com.myrota.app.local.BreakDao
import com.myrota.app.local.BreakEntity
import kotlinx.coroutines.flow.Flow

class BreakRepository(
    private val breakDao: BreakDao
) {

    fun observeBreaksForShift(
        shiftId: Long
    ): Flow<List<BreakEntity>> =
        breakDao.observeBreaksForShift(
            shiftId
        )

    fun observeBreakById(
        breakId: Long
    ): Flow<BreakEntity?> =
        breakDao.observeBreakById(
            breakId
        )

    suspend fun insertBreak(
        breakEntity: BreakEntity
    ): Long =
        breakDao.insertBreak(
            breakEntity
        )

    suspend fun updateBreak(
        breakEntity: BreakEntity
    ) {
        breakDao.updateBreak(
            breakEntity
        )
    }

    suspend fun deleteBreak(
        breakEntity: BreakEntity
    ) {
        breakDao.deleteBreak(
            breakEntity
        )
    }

    suspend fun deleteBreakById(
        breakId: Long
    ): Int =
        breakDao.deleteBreakById(
            breakId
        )

    suspend fun getTotalUnpaidBreakMinutes(
        shiftId: Long
    ): Int =
        breakDao.getTotalUnpaidBreakMinutes(
            shiftId
        )

    suspend fun getTotalPaidBreakMinutes(
        shiftId: Long
    ): Int =
        breakDao.getTotalPaidBreakMinutes(
            shiftId
        )
}