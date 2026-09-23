package com.example.cas.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "interviews",
    indices = [Index(value = ["case_id"])],
    foreignKeys = [
        ForeignKey(
            entity = CaseEntity::class,
            parentColumns = ["case_id"],
            childColumns = ["case_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class InterviewEntity (
    @PrimaryKey(autoGenerate = true)
    val interview_id: Long = 0L,
    val case_id: Long,
    val description: String,
    val findings: String,
    val date: String,
    val person: String
)
