package com.example.cas.data.repository

import com.example.cas.data.local.dao.RecordDAO
import com.example.cas.data.local.entity.RecordEntity
import kotlinx.coroutines.flow.Flow

class RecordRepository(
    private val recordDao: RecordDAO
) {
    fun getRecordsByInterview(interviewId: Long): Flow<List<RecordEntity>> {
        return recordDao.getRecordsByInterview(interviewId)
    }

    suspend fun insertRecord(record: RecordEntity): Long {
        return recordDao.insert(record)
    }

    fun getRecordsByCase(caseId: Long): Flow<List<RecordEntity>> {
        return recordDao.getRecordsByCase(caseId)
    }
}