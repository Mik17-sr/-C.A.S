package com.example.cas.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.cas.data.local.entity.InterviewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InterviewDAO : InterfaceDAO<InterviewEntity> {

    @Query("SELECT * FROM interviews WHERE case_id = :caseId")
    fun getInterviewsByCase(caseId: Long) : Flow<List<InterviewEntity>>
}