package com.myrota.app.data.repository

import com.myrota.app.local.ShiftDao
import com.myrota.app.local.ShiftEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ShiftRepository(
    private val shiftDao: ShiftDao
) {

    fun observeAllShifts(): Flow<List<ShiftEntity>> =
        shiftDao.observeAllShifts()

    fun observeShiftById(
        shiftId: Long
    ): Flow<ShiftEntity?> =
        shiftDao.observeShiftById(
            shiftId
        )

    suspend fun getShiftByIdOnce(
        shiftId: Long
    ): ShiftEntity? {

        return shiftDao
            .observeShiftById(
                shiftId
            )
            .first()
    }

    fun observeShiftsForCompany(
        companyId: Long
    ): Flow<List<ShiftEntity>> =
        shiftDao.observeShiftsForCompany(
            companyId
        )

    fun observeShiftsBetween(
        startMillis: Long,
        endMillis: Long
    ): Flow<List<ShiftEntity>> =
        shiftDao.observeShiftsBetween(
            startMillis = startMillis,
            endMillis = endMillis
        )

    suspend fun findOverlappingShifts(
        startMillis: Long,
        endMillis: Long
    ): List<ShiftEntity> =
        shiftDao.findOverlappingShifts(
            startMillis = startMillis,
            endMillis = endMillis
        )

    suspend fun findOverlappingShiftsExcluding(
        shiftId: Long,
        startMillis: Long,
        endMillis: Long
    ): List<ShiftEntity> =
        shiftDao.findOverlappingShiftsExcluding(
            shiftId = shiftId,
            startMillis = startMillis,
            endMillis = endMillis
        )

    suspend fun insertShift(
        shift: ShiftEntity
    ): Long =
        shiftDao.insertShift(
            shift
        )

    suspend fun updateShift(
        shift: ShiftEntity
    ) {

        shiftDao.updateShift(
            shift
        )
    }

    suspend fun cancelShift(
        shiftId: Long,
        updatedAt: Long
    ) {

        shiftDao.cancelShift(
            shiftId = shiftId,
            updatedAt = updatedAt
        )
    }

    suspend fun startShift(
        shiftId: Long,
        actualStart: Long,
        updatedAt: Long
    ): Int {

        return shiftDao.startShift(
            shiftId = shiftId,
            actualStart = actualStart,
            updatedAt = updatedAt
        )
    }

    suspend fun endShift(
        shiftId: Long,
        actualEnd: Long,
        updatedAt: Long
    ): Int {

        return shiftDao.endShift(
            shiftId = shiftId,
            actualEnd = actualEnd,
            updatedAt = updatedAt
        )
    }
}