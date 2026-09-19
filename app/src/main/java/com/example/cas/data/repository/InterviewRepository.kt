package com.example.cas.data.repository

import com.example.cas.data.local.dao.InterviewDAO
import com.example.cas.data.local.entity.InterviewEntity
import kotlinx.coroutines.flow.Flow

class InterviewRepository(
    private val interviewDao: InterviewDAO
) {

    fun getInterviewsByCase(caseId: Long): Flow<List<InterviewEntity>> {
        return interviewDao.getInterviewsByCase(caseId)
    }

    suspend fun insertInterview(interview: InterviewEntity): Long {
        return interviewDao.insert(interview)
    }

    suspend fun deleteInterview(interview: InterviewEntity) {
        interviewDao.delete(interview)
    }
}