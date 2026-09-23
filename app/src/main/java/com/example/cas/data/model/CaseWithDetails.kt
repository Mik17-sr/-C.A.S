package com.example.cas.data.model

data class CaseWithDetails(
    val caseId: Long,
    val userId: Long,
    val title: String,
    val description: String,
    val date: String,
    val status: String,
    val userName: String?,
    val interviewCount: Int
)
