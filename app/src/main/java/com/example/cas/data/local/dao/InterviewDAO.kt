package com.example.cas.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.cas.data.local.entity.InterviewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InterviewDAO : InterfaceDAO<InterviewEntity> {

    @Query("SELECT * FROM interviews WHERE case_id = :caseId")
    fun getInterviewsByCase(caseId: Long) : Flow<List<InterviewEntity>>

    @Query("SELECT COUNT(*) FROM interviews")
    fun countInterviews(): Flow<Int>

    @Query("SELECT COUNT(*) FROM interviews i INNER JOIN cases c ON i.case_id = c.case_id WHERE c.user_id = :userId")
    fun countInterviewsForUser(userId: Long): Flow<Int>
}