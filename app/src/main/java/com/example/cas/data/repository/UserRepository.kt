package com.example.cas.data.repository

import com.example.cas.data.local.dao.UserDAO
import com.example.cas.data.local.entity.InterviewEntity
import com.example.cas.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

class UserRepository(
    val userDao: UserDAO
) {

    val allUsers : Flow<List<UserEntity>> = userDao.getAllUsers()

    suspend fun getUserById(userId: Long) : UserEntity? {
        return userDao.getUserById(userId)
    }

    suspend fun insertUser(user: UserEntity): Long {
        return userDao.insert(user)
    }

    suspend fun deleteUser(user: UserEntity) {
        userDao.delete(user)
    }
}