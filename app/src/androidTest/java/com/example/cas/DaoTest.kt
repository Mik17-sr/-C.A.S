package com.example.cas

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cas.data.local.AppDatabase
import com.example.cas.data.local.dao.CaseDAO
import com.example.cas.data.local.entity.CaseEntity
import com.example.cas.data.local.entity.UserEntity
import com.example.cas.data.repository.CaseRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DaoTest {

    private lateinit var database: AppDatabase
    private lateinit var caseDao: CaseDAO
    private lateinit var repository: CaseRepository

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        caseDao = database.caseDao()
        repository = CaseRepository(caseDao)
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAndReadCase() = runBlocking {
        val userDao = database.userDao()

        val user = UserEntity(
            name = "Inspector Test",
            photo = "url_foto",
            email = "test@cas.com",
            username = "inspector1",
            password = "123"
        )

        val userId = userDao.insert(user)

        val caseEntity = CaseEntity(
            user_id = userId,
            title = "Caso de Prueba 01",
            photo = "evidence.jpg",
            description = "Investigación en el callejón",
            date = "2026-09-19",
            status = "Abierto",
            conclusion = null
        )

        val caseId = repository.insertCase(caseEntity)

        val retrievedCase = repository.getCaseById(caseId)

        Assert.assertNotNull(retrievedCase)
        Assert.assertEquals("Caso de Prueba 01", retrievedCase?.title)
        Assert.assertEquals(userId, retrievedCase?.user_id)
    }

    @Test
    fun observeAllCasesFlow() = runBlocking {
        val userDao = database.userDao()

        val userId = userDao.insert(
            UserEntity(
                name = "User",
                photo = "",
                email = "u@u.com",
                username = "u",
                password = "p"
            )
        )

        val case1 = CaseEntity(
            user_id = userId,
            title = "Caso A",
            photo = "",
            description = "",
            date = "",
            status = ""
        )

        val case2 = CaseEntity(
            user_id = userId,
            title = "Caso B",
            photo = "",
            description = "",
            date = "",
            status = ""
        )

        repository.insertCase(case1)
        repository.insertCase(case2)

        val casesList = repository.allCases.first()

        Assert.assertEquals(2, casesList.size)
        Assert.assertEquals("Caso A", casesList[0].title)
        Assert.assertEquals("Caso B", casesList[1].title)
    }
}