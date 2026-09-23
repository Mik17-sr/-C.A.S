package com.example.cas.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.cas.data.local.entity.RecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordDAO : InterfaceDAO<RecordEntity> {

    @Query("SELECT * FROM records WHERE interview_id = :interviewId")
    fun getRecordsByInterview(interviewId: Long): Flow<List<RecordEntity>>

    @Query("""
    SELECT r.* FROM records r
    INNER JOIN interviews i ON r.interview_id = i.interview_id
    WHERE i.case_id = :caseId
""")
    fun getRecordsByCase(caseId: Long): Flow<List<RecordEntity>>
}