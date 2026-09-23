package com.example.cas.fakes

import com.example.cas.data.local.dao.CaseDAO
import com.example.cas.data.local.dao.InterviewDAO
import com.example.cas.data.local.dao.RecordDAO
import com.example.cas.data.local.dao.UserDAO
import com.example.cas.data.local.entity.CaseEntity
import com.example.cas.data.local.entity.InterviewEntity
import com.example.cas.data.local.entity.RecordEntity
import com.example.cas.data.local.entity.UserEntity
import com.example.cas.data.model.CaseWithDetails
import com.example.cas.data.model.CaseWithInterviewCount
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeCaseDao : CaseDAO {

    private var nextId = 1L
    private val casesFlow = MutableStateFlow<List<CaseEntity>>(emptyList())

    // Lo que pidieron por última vez, para comprobar los parámetros
    var lastActiveUserId: Long? = null
    var lastActiveClosedStatus: String? = null
    var lastInvestigatingUserId: Long? = null
    var lastInvestigatingStatus: String? = null
    var lastInvestigatingLimit: Int? = null

    override fun getAllCases(): Flow<List<CaseEntity>> = casesFlow

    override fun getAllCasesForUser(userId: Long): Flow<List<CaseEntity>> =
        casesFlow.map { list -> list.filter { it.user_id == userId } }

    override suspend fun countCases(): Int = casesFlow.value.size

    override suspend fun getCaseById(caseId: Long): CaseEntity? =
        casesFlow.value.find { it.case_id == caseId }

    override fun searchCases(query: String): Flow<List<CaseEntity>> =
        casesFlow.map { list ->
            list.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true)
            }
        }

    override fun countActiveCases(closedStatus: String): Flow<Int> =
        casesFlow.map { list -> list.count { it.status != closedStatus } }

    override fun countActiveCasesForUser(userId: Long, closedStatus: String): Flow<Int> {
        lastActiveUserId = userId
        lastActiveClosedStatus = closedStatus
        return casesFlow.map { list -> list.count { it.user_id == userId && it.status != closedStatus } }
    }

    override fun countConclusions(): Flow<Int> =
        casesFlow.map { list -> list.count { !it.conclusion.isNullOrEmpty() } }

    override fun countConclusionsForUser(userId: Long): Flow<Int> =
        casesFlow.map { list -> list.count { it.user_id == userId && !it.conclusion.isNullOrEmpty() } }

    override fun getCasesWithInterviewCount(status: String, limit: Int): Flow<List<CaseWithInterviewCount>> =
        casesFlow.map { list ->
            list.filter { it.status == status }
                .sortedByDescending { it.date }
                .take(limit)
                .map { CaseWithInterviewCount(case = it, interviewCount = 0) }
        }

    override fun getCasesWithInterviewCountForUser(
        userId: Long,
        status: String,
        limit: Int
    ): Flow<List<CaseWithInterviewCount>> {
        lastInvestigatingUserId = userId
        lastInvestigatingStatus = status
        lastInvestigatingLimit = limit
        return casesFlow.map { list ->
            list.filter { it.user_id == userId && it.status == status }
                .sortedByDescending { it.date }
                .take(limit)
                .map { CaseWithInterviewCount(case = it, interviewCount = 0) }
        }
    }

    override fun getCasesWithDetails(query: String): Flow<List<CaseWithDetails>> =
        casesFlow.map { list -> list.map { it.toDetails() }.filterByTitle(query) }

    override fun getCasesWithDetailsForUser(userId: Long, query: String): Flow<List<CaseWithDetails>> =
        casesFlow.map { list ->
            list.filter { it.user_id == userId }.map { it.toDetails() }.filterByTitle(query)
        }

    override suspend fun insert(entity: CaseEntity): Long {
        val id = if (entity.case_id != 0L) entity.case_id else nextId++
        val saved = entity.copy(case_id = id)
        casesFlow.value = casesFlow.value.filterNot { it.case_id == id } + saved
        return id
    }

    override suspend fun insertAll(entities: List<CaseEntity>) {
        entities.forEach { insert(it) }
    }

    override suspend fun update(entity: CaseEntity) {
        casesFlow.value = casesFlow.value.map { if (it.case_id == entity.case_id) entity else it }
    }

    override suspend fun delete(entity: CaseEntity) {
        casesFlow.value = casesFlow.value.filterNot { it.case_id == entity.case_id }
    }

    private fun CaseEntity.toDetails() = CaseWithDetails(
        caseId = case_id,
        userId = user_id,
        title = title,
        description = description,
        date = date,
        status = status,
        userName = null,
        interviewCount = 0
    )

    private fun List<CaseWithDetails>.filterByTitle(query: String): List<CaseWithDetails> =
        if (query.isBlank()) this else filter { it.title.contains(query, ignoreCase = true) }
}

class FakeInterviewDao : InterviewDAO {

    private var nextId = 1L
    private val interviewsFlow = MutableStateFlow<List<InterviewEntity>>(emptyList())

    // Para las pruebas de Inicio, que solo necesitan fijar un número
    val interviewCount = MutableStateFlow(0)
    val interviewCountForUser = MutableStateFlow(0)

    override fun countInterviews(): Flow<Int> = interviewCount

    override fun countInterviewsForUser(userId: Long): Flow<Int> = interviewCountForUser

    override fun getInterviewsByCase(caseId: Long): Flow<List<InterviewEntity>> =
        interviewsFlow.map { list -> list.filter { it.case_id == caseId } }

    override suspend fun insert(entity: InterviewEntity): Long {
        val id = if (entity.interview_id != 0L) entity.interview_id else nextId++
        val saved = entity.copy(interview_id = id)
        interviewsFlow.value = interviewsFlow.value + saved
        return id
    }

    override suspend fun insertAll(entities: List<InterviewEntity>) {
        entities.forEach { insert(it) }
    }

    override suspend fun update(entity: InterviewEntity) {
        interviewsFlow.value = interviewsFlow.value.map {
            if (it.interview_id == entity.interview_id) entity else it
        }
    }

    override suspend fun delete(entity: InterviewEntity) {
        interviewsFlow.value = interviewsFlow.value.filterNot { it.interview_id == entity.interview_id }
    }
}

class FakeUserDao : UserDAO {

    private var nextId = 1L
    private val usersFlow = MutableStateFlow<List<UserEntity>>(emptyList())

    override fun getAllUsers(): Flow<List<UserEntity>> = usersFlow

    override suspend fun getUserById(userId: Long): UserEntity? =
        usersFlow.value.find { it.user_id == userId }

    override suspend fun getUserByUsername(username: String, password: String): UserEntity? =
        usersFlow.value.find { (it.username == username || it.email == username) && it.password == password }

    override suspend fun countUsers(): Int = usersFlow.value.size

    override suspend fun getAllUsersList(): List<UserEntity> = usersFlow.value

    override suspend fun getUserByEmail(email: String): UserEntity? =
        usersFlow.value.find { it.email == email }

    override suspend fun insert(entity: UserEntity): Long {
        val id = if (entity.user_id != 0L) entity.user_id else nextId++
        val saved = entity.copy(user_id = id)
        usersFlow.value = usersFlow.value.filterNot { it.user_id == id } + saved
        return id
    }

    override suspend fun insertAll(entities: List<UserEntity>) {
        entities.forEach { insert(it) }
    }

    override suspend fun update(entity: UserEntity) {
        usersFlow.value = usersFlow.value.map { if (it.user_id == entity.user_id) entity else it }
    }

    override suspend fun delete(entity: UserEntity) {
        usersFlow.value = usersFlow.value.filterNot { it.user_id == entity.user_id }
    }
}

class FakeRecordDao : RecordDAO {

    private var nextId = 1L
    private val recordsFlow = MutableStateFlow<List<RecordEntity>>(emptyList())

    override fun getRecordsByInterview(interviewId: Long): Flow<List<RecordEntity>> =
        recordsFlow.map { list -> list.filter { it.interview_id == interviewId } }

    override fun getRecordsByCase(caseId: Long): Flow<List<RecordEntity>> = recordsFlow

    override suspend fun insert(entity: RecordEntity): Long {
        val id = if (entity.record_id != 0L) entity.record_id else nextId++
        val saved = entity.copy(record_id = id)
        recordsFlow.value = recordsFlow.value + saved
        return id
    }

    override suspend fun insertAll(entities: List<RecordEntity>) {
        entities.forEach { insert(it) }
    }

    override suspend fun update(entity: RecordEntity) {
        recordsFlow.value = recordsFlow.value.map { if (it.record_id == entity.record_id) entity else it }
    }

    override suspend fun delete(entity: RecordEntity) {
        recordsFlow.value = recordsFlow.value.filterNot { it.record_id == entity.record_id }
    }
}