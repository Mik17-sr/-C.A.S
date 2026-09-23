package com.example.cas.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.cas.data.local.entity.CaseEntity
import com.example.cas.data.model.CaseWithDetails
import com.example.cas.data.model.CaseWithInterviewCount
import kotlinx.coroutines.flow.Flow

@Dao
interface CaseDAO : InterfaceDAO<CaseEntity> {

    @Query("SELECT * FROM cases ORDER BY date DESC")
    fun getAllCases(): Flow<List<CaseEntity>>

    @Query("SELECT * FROM cases WHERE user_id = :userId ORDER BY date DESC")
    fun getAllCasesForUser(userId: Long): Flow<List<CaseEntity>>

    @Query("SELECT COUNT(*) FROM cases")
    suspend fun countCases(): Int

    @Query("SELECT * FROM cases WHERE case_id = :caseId")
    suspend fun getCaseById(caseId: Long): CaseEntity?

    @Query("SELECT * FROM cases WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchCases(query: String): Flow<List<CaseEntity>>

    @Query("SELECT COUNT(*) FROM cases WHERE status != :closedStatus")
    fun countActiveCases(closedStatus: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM cases WHERE user_id = :userId AND status != :closedStatus")
    fun countActiveCasesForUser(userId: Long, closedStatus: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM cases WHERE conclusion IS NOT NULL AND conclusion != ''")
    fun countConclusions(): Flow<Int>

    @Query("SELECT COUNT(*) FROM cases WHERE user_id = :userId AND conclusion IS NOT NULL AND conclusion != ''")
    fun countConclusionsForUser(userId: Long): Flow<Int>

    @Query("""
    SELECT c.*, (SELECT COUNT(*) FROM interviews i WHERE i.case_id = c.case_id) AS interview_count
    FROM cases c
    WHERE c.status = :status
    ORDER BY c.date DESC
    LIMIT :limit
""")
    fun getCasesWithInterviewCount(status: String, limit: Int): Flow<List<CaseWithInterviewCount>>

    @Query("""
    SELECT c.*, (SELECT COUNT(*) FROM interviews i WHERE i.case_id = c.case_id) AS interview_count
    FROM cases c
    WHERE c.user_id = :userId AND c.status = :status
    ORDER BY c.date DESC
    LIMIT :limit
""")
    fun getCasesWithInterviewCountForUser(userId: Long, status: String, limit: Int): Flow<List<CaseWithInterviewCount>>

    @Query("""
    SELECT 
        c.case_id AS caseId,
        c.user_id AS userId,
        c.title AS title,
        c.description AS description,
        c.date AS date,
        c.status AS status,
        u.name AS userName,
        (SELECT COUNT(*) FROM interviews i WHERE i.case_id = c.case_id) AS interviewCount
    FROM cases c
    LEFT JOIN users u ON c.user_id = u.user_id
    WHERE (:query = '' OR LOWER(c.title) LIKE '%' || LOWER(:query) || '%')
    ORDER BY c.date DESC
""")
    fun getCasesWithDetails(query: String): Flow<List<CaseWithDetails>>

    @Query("""
    SELECT 
        c.case_id AS caseId,
        c.user_id AS userId,
        c.title AS title,
        c.description AS description,
        c.date AS date,
        c.status AS status,
        u.name AS userName,
        (SELECT COUNT(*) FROM interviews i WHERE i.case_id = c.case_id) AS interviewCount
    FROM cases c
    LEFT JOIN users u ON c.user_id = u.user_id
    WHERE c.user_id = :userId AND (:query = '' OR LOWER(c.title) LIKE '%' || LOWER(:query) || '%')
    ORDER BY c.date DESC
""")
    fun getCasesWithDetailsForUser(userId: Long, query: String): Flow<List<CaseWithDetails>>
}