package com.example.cas

import android.app.Application
import com.example.cas.data.local.AppDatabase
import com.example.cas.data.local.entity.UserEntity
import com.example.cas.data.repository.CaseRepository
import com.example.cas.data.repository.InterviewRepository
import com.example.cas.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CasApplication : Application() {

    private val database by lazy { AppDatabase.getDatabase(this) }

    val caseRepository by lazy { CaseRepository(database.caseDao()) }
    val interviewRepository by lazy { InterviewRepository(database.interviewDao()) }
    val userRepository by lazy { UserRepository(database.userDao()) }

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            val userDao = database.userDao()
            if (userDao.countUsers() == 0) {
                userDao.insert(
                    UserEntity(
                        name = "Periodista",
                        photo = "",
                        email = "periodista@notaviva.com",
                        username = "periodista",
                        password = ""
                    )
                )
            }
        }
    }
}