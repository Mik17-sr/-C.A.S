package com.example.cas.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "records",
    indices = [Index(value = ["interview_id"])],
    foreignKeys = [
        ForeignKey(
            entity = InterviewEntity::class,
            parentColumns = ["interview_id"],
            childColumns = ["interview_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class RecordEntity (
    @PrimaryKey(autoGenerate = true)
    val record_id: Long = 0L,
    val interview_id: Long,
    val content: String,
    val audio_path: String? = null,
    val date: String
)