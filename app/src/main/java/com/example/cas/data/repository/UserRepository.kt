package com.example.cas.data.repository

import com.example.cas.data.local.dao.UserDAO
import com.example.cas.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

class UserRepository(
    private val userDao: UserDAO
) {

    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsers()

    suspend fun getUserById(userId: Long): UserEntity? {
        return userDao.getUserById(userId)
    }

    suspend fun getUserByEmail(email: String): UserEntity? {
        return userDao.getUserByEmail(email)
    }

    suspend fun login(email: String, password: String): UserEntity? {
        return userDao.getUserByUsername(email, password)
    }

    suspend fun insertUser(user: UserEntity): Long {
        return userDao.insert(user)
    }

    suspend fun updateUser(user: UserEntity) {
        userDao.update(user)
    }

    suspend fun deleteUser(user: UserEntity) {
        userDao.delete(user)
    }
}