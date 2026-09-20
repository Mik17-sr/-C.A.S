package com.example.cas

import android.app.Application
import com.example.cas.data.local.AppDatabase
import com.example.cas.data.local.entity.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CasApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            val userDao = AppDatabase.getDatabase(applicationContext).userDao()
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