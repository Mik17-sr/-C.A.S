package com.example.cas.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.cas.data.local.entity.CaseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CaseDAO : InterfaceDAO<CaseEntity> {

    @Query("SELECT *  FROM cases ORDER BY date DESC")
    fun getAllCases(): Flow<List<CaseEntity>>

    @Query("SELECT * FROM cases WHERE case_id = :caseId")
    suspend fun getCaseById(caseId: Long): CaseEntity?

    @Query("SELECT * FROM cases WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchCases(query: String): Flow<List<CaseEntity>>
}