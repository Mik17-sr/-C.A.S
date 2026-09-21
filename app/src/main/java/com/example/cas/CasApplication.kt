package com.example.cas

import android.app.Application
import com.example.cas.data.local.AppDatabase
import com.example.cas.data.repository.UserRepository

class CasApplication : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }
    val userRepository by lazy { UserRepository(database.userDao()) }

    override fun onCreate() {
        super.onCreate()
    }
}