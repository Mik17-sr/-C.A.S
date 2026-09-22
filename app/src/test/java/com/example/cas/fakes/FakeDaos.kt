package com.example.cas.fakes

import com.example.cas.data.local.dao.CaseDAO
import com.example.cas.data.local.dao.InterviewDAO
import com.example.cas.data.local.entity.CaseEntity
import com.example.cas.data.local.entity.InterviewEntity
import com.example.cas.data.model.CaseWithInterviewCount
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

class FakeCaseDao : CaseDAO {

    val activeCount = MutableStateFlow(0)
    val conclusionsCount = MutableStateFlow(0)
    val investigating = MutableStateFlow<List<CaseWithInterviewCount>>(emptyList())

    // Con qué parámetros nos consultaron
    var lastClosedStatus: String? = null
    var lastRequestedStatus: String? = null
    var lastRequestedLimit: Int? = null

    override fun countActiveCases(closedStatus: String): Flow<Int> {
        lastClosedStatus = closedStatus
        return activeCount
    }

    override fun countConclusions(): Flow<Int> = conclusionsCount

    override fun getCasesWithInterviewCount(
        status: String,
        limit: Int
    ): Flow<List<CaseWithInterviewCount>> {
        lastRequestedStatus = status
        lastRequestedLimit = limit
        return investigating
    }

    // Lo que Inicio no usa: respuestas vacías
    override fun getAllCases(): Flow<List<CaseEntity>> = flowOf(emptyList())
    override suspend fun getCaseById(caseId: Long): CaseEntity? = null
    override fun searchCases(query: String): Flow<List<CaseEntity>> = flowOf(emptyList())
    override suspend fun insert(entity: CaseEntity): Long = 0L
    override suspend fun insertAll(entities: List<CaseEntity>) {}
    override suspend fun update(entity: CaseEntity) {}
    override suspend fun delete(entity: CaseEntity) {}
}

class FakeInterviewDao : InterviewDAO {

    val interviewCount = MutableStateFlow(0)

    override fun countInterviews(): Flow<Int> = interviewCount

    override fun getInterviewsByCase(caseId: Long): Flow<List<InterviewEntity>> = flowOf(emptyList())
    override suspend fun insert(entity: InterviewEntity): Long = 0L
    override suspend fun insertAll(entities: List<InterviewEntity>) {}
    override suspend fun update(entity: InterviewEntity) {}
    override suspend fun delete(entity: InterviewEntity) {}
}