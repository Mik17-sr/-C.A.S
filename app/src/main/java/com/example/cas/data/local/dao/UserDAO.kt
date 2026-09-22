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

    @Query("SELECT * FROM users WHERE (username = :username OR email = :username) AND password = :password")
    suspend fun getUserByUsername(username: String, password : String): UserEntity?

    @Query("SELECT COUNT(*) FROM users")
    suspend fun countUsers(): Int

    @Query("SELECT * FROM users ORDER BY user_id ASC")
    suspend fun getAllUsersList(): List<UserEntity>

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): UserEntity?
}