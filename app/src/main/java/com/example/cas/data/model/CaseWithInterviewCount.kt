package com.example.cas.data.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import com.example.cas.data.local.entity.CaseEntity

data class CaseWithInterviewCount(
    @Embedded val case: CaseEntity,
    @ColumnInfo(name = "interview_count") val interviewCount: Int
)