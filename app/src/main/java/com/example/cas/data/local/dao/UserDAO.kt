package com.example.cas.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.cas.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDAO : InterfaceDAO<UserEntity> {

    @Query("SELECT * FROM users ORDER BY user_id DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT *  FROM users WHERE user_id = :userId")
    suspend fun getUserById(userId: Long): UserEntity?

    @Query("SELECT * FROM users WHERE username = :username AND password = :password")
    suspend fun getUserByUsername(username: String, password : String): UserEntity?

    @Query("SELECT COUNT(*) FROM users")
    suspend fun countUsers(): Int
}