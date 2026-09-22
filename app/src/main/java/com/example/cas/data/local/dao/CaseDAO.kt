package com.example.cas.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.cas.data.local.entity.CaseEntity
import com.example.cas.data.model.CaseStatus
import com.example.cas.data.model.CaseWithInterviewCount
import kotlinx.coroutines.flow.Flow

@Dao
interface CaseDAO : InterfaceDAO<CaseEntity> {

    @Query("SELECT *  FROM cases ORDER BY date DESC")
    fun getAllCases(): Flow<List<CaseEntity>>

    @Query("SELECT * FROM cases WHERE case_id = :caseId")
    suspend fun getCaseById(caseId: Long): CaseEntity?

    @Query("SELECT * FROM cases WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchCases(query: String): Flow<List<CaseEntity>>

    @Query("SELECT COUNT(*) FROM cases WHERE status != :closedStatus")
    fun countActiveCases(closedStatus: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM cases WHERE conclusion IS NOT NULL AND conclusion != ''")
    fun countConclusions(): Flow<Int>

    @Query("""
    SELECT c.*, (SELECT COUNT(*) FROM interviews i WHERE i.case_id = c.case_id) AS interview_count
    FROM cases c
    WHERE c.status = :status
    ORDER BY c.date DESC
    LIMIT :limit
""")
    fun getCasesWithInterviewCount(status: String, limit: Int): Flow<List<CaseWithInterviewCount>>
}