package com.example.cas

import android.app.Application
import com.example.cas.data.local.AppDatabase
import com.example.cas.data.local.entity.CaseEntity
import com.example.cas.data.local.entity.InterviewEntity
import com.example.cas.data.local.entity.UserEntity
import com.example.cas.data.model.CaseStatus
import com.example.cas.data.repository.CaseRepository
import com.example.cas.data.repository.InterviewRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CasApplication : Application() {

    private val database by lazy { AppDatabase.getDatabase(this) }

    val caseRepository by lazy { CaseRepository(database.caseDao()) }
    val interviewRepository by lazy { InterviewRepository(database.interviewDao()) }

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
            // ===== TEMPORAL: solo para probar, borrar antes del commit =====
            if (database.caseDao().getAllCases().first().isEmpty()) {
                val caseA = caseRepository.insertCase(
                    CaseEntity(
                        user_id = 1L, title = "Red de sobornos en obra pública", photo = "",
                        description = "Prueba", date = "2025-03-12",
                        status = CaseStatus.INVESTIGATING, conclusion = "Posible direccionamiento de contratos"
                    )
                )
                caseRepository.insertCase(
                    CaseEntity(
                        user_id = 1L, title = "Homicidio en zona industrial", photo = "",
                        description = "Prueba", date = "2025-02-20", status = CaseStatus.EDITING
                    )
                )
                caseRepository.insertCase(
                    CaseEntity(
                        user_id = 1L, title = "Desaparición en el barrio norte", photo = "",
                        description = "Prueba", date = "2025-01-10",
                        status = CaseStatus.CLOSED, conclusion = "Caso resuelto"
                    )
                )
                interviewRepository.insertInterview(
                    InterviewEntity(
                        case_id = caseA, description = "Entrevista de prueba",
                        findings = "Hallazgo de prueba", date = "2025-03-10", person = "Fuente A"
                    )
                )
            }
            // ===== FIN TEMPORAL =====
        }
    }
}