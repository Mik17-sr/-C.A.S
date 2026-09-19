package com.example.cas.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity (
    @PrimaryKey(autoGenerate = true)
    val user_id: Long = 0L,
    val name: String,
    val photo: String,
    val email: String,
    val username: String,
    val password: String,
)