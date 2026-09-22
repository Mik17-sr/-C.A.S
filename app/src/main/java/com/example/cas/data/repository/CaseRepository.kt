package com.example.cas.data.repository

import com.example.cas.data.local.dao.CaseDAO
import com.example.cas.data.local.entity.CaseEntity
import com.example.cas.data.model.CaseStatus
import com.example.cas.data.model.CaseWithInterviewCount
import kotlinx.coroutines.flow.Flow

class CaseRepository(
    private val caseDao: CaseDAO
) {

    val allCases: Flow<List<CaseEntity>> = caseDao.getAllCases()
    val activeCasesCount: Flow<Int> = caseDao.countActiveCases(CaseStatus.CLOSED)
    val conclusionsCount: Flow<Int> = caseDao.countConclusions()

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

    fun getCasesWithInterviewCount(status: String, limit: Int): Flow<List<CaseWithInterviewCount>> {
        return caseDao.getCasesWithInterviewCount(status, limit)
    }

    suspend fun updateCase(case: CaseEntity) {
        caseDao.update(case)
    }
}