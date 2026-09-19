package com.example.cas.data.repository

import com.example.cas.data.local.dao.CaseDAO
import com.example.cas.data.local.entity.CaseEntity
import kotlinx.coroutines.flow.Flow

class CaseRepository(
    private val caseDao: CaseDAO
) {

    val allCases: Flow<List<CaseEntity>> = caseDao.getAllCases()

    suspend fun insertCase(case: CaseEntity): Long {
        return caseDao.insert(case)
    }

    suspend fun getCaseById(caseId: Long) : CaseEntity? {
        return caseDao.getCaseById(caseId)
    }

    fun searchCases(query: String): Flow<List<CaseEntity>> {
        return caseDao.searchCases(query)
    }

    suspend fun deleteCase(case: CaseEntity) {
        caseDao.delete(case)
    }
}