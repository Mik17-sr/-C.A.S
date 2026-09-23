package com.example.cas.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cases",
    indices = [Index(value = ["user_id"])],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CaseEntity (
    @PrimaryKey(autoGenerate = true)
    val case_id: Long = 0L,
    val user_id: Long,
    val title: String,
    val photo: String,
    val description: String,
    val date: String,
    val status: String,
    val conclusion: String? = null
)