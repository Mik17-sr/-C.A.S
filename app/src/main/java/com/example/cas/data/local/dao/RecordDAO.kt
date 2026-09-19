package com.example.cas.data.local.dao

import androidx.room.Query
import com.example.cas.data.local.entity.RecordEntity
import kotlinx.coroutines.flow.Flow

interface RecordDAO : InterfaceDAO<RecordEntity> {

    @Query("SELECT * FROM records WHERE interview_id = :interviewId")
    fun getRecordsByInterview(interviewId: Long): Flow<List<RecordEntity>>
}