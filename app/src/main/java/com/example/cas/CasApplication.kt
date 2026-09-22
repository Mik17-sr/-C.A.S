package com.example.cas

import android.app.Application
import com.example.cas.data.local.AppDatabase
import com.example.cas.data.local.entity.CaseEntity
import com.example.cas.data.local.entity.UserEntity
import com.example.cas.data.model.CaseStatus
import com.example.cas.data.local.entity.EvidenceEntity
import com.example.cas.data.repository.CaseRepository
import com.example.cas.data.repository.EvidenceRepository
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
    val evidenceRepository by lazy { EvidenceRepository(database.evidenceDao()) }

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            val userDao = database.userDao()
            val caseDao = database.caseDao()

            if (userDao.countUsers() == 0) {
                userDao.insert(
                    UserEntity(
                        name = "Tomás David Lancheros Churque",
                        photo = "",
                        email = "tdlancherosc@udistrital.edu.co",
                        username = "tin",
                        password = "123456"
                    )
                )
                userDao.insert(
                    UserEntity(
                        name = "Dylan Gerhard Árce Triviño",
                        photo = "",
                        email = "dgarcet@udistrital.edu.co",
                        username = "miau",
                        password = "123456"
                    )
                )
                userDao.insert(
                    UserEntity(
                        name = "Miguel Ángel Sierra Larrota",
                        photo = "",
                        email = "masierral@udistrital.edu.co",
                        username = "TTT",
                        password = "123456"
                    )
                )
            }

            if (caseDao.countCases() == 0) {
                val users = userDao.getAllUsersList()
                if (users.isNotEmpty()) {
                    val u1 = users[0].user_id
                    val u2 = if (users.size > 1) users[1].user_id else u1
                    val u3 = if (users.size > 2) users[2].user_id else u1

                    val sampleCases = listOf(
                        CaseEntity(user_id = u1, title = "Red de sobornos en obra pública", photo = "", description = "Investigación sobre desvíos de fondos y sobornos a funcionarios en licitaciones viales.", date = "2026-09-10", status = CaseStatus.INVESTIGATING),
                        CaseEntity(user_id = u1, title = "Homicidio en zona industrial", photo = "", description = "Seguimiento a las pistas del caso ocurrido el mes pasado en las bodegas del sector oriente.", date = "2026-09-08", status = CaseStatus.INVESTIGATING),
                        CaseEntity(user_id = u1, title = "Fraude tributario en empresas fantasma", photo = "", description = "Auditoría periodística a redes de facturación falsa registradas en el centro financiero.", date = "2026-08-25", status = CaseStatus.EDITING),
                        CaseEntity(user_id = u1, title = "Contaminación ambiental en el río Bogotá", photo = "", description = "Monitoreo de descargas de residuos industriales sin tratamiento previo en cuencas medias.", date = "2026-08-15", status = CaseStatus.PUBLISHED),
                        CaseEntity(user_id = u1, title = "Desvío de fondos para alimentación escolar", photo = "", description = "Inconsistencias en los contratos del programa PAE en múltiples municipios.", date = "2026-07-30", status = CaseStatus.CLOSED, conclusion = "Informe presentado y denuncias radicadas ante la fiscalía."),

                        CaseEntity(user_id = u2, title = "Licitaciones irregulares en transporte público", photo = "", description = "Revisión de pliegos de condiciones dirigidos a un único oferente en la renovación de flotas.", date = "2026-09-18", status = CaseStatus.INVESTIGATING),
                        CaseEntity(user_id = u2, title = "Tráfico de influencias en el senado", photo = "", description = "Entrevistas confidenciales sobre presiones políticas en la aprobación de reformas clave.", date = "2026-09-12", status = CaseStatus.INVESTIGATING),
                        CaseEntity(user_id = u2, title = "Desaparición de archivo histórico municipal", photo = "", description = "Pérdida de tomos de propiedad de tierras que datan de la primera mitad del siglo XX.", date = "2026-08-28", status = CaseStatus.EDITING),
                        CaseEntity(user_id = u2, title = "Explotación ilegal de minería en reserva natural", photo = "", description = "Uso de maquinaria pesada sin licencia ambiental en áreas de recarga hídrica protegidas.", date = "2026-08-04", status = CaseStatus.PUBLISHED),
                        CaseEntity(user_id = u2, title = "Ciberataque a la red hospitalaria nacional", photo = "", description = "Análisis del secuestro de datos clínicos y caída del sistema de citas en hospitales públicos.", date = "2026-07-18", status = CaseStatus.CLOSED, conclusion = "Vulnerabilidad corregida tras publicación del reportaje especial."),

                        CaseEntity(user_id = u3, title = "Corrupción en contratos de salud departamental", photo = "", description = "Sobreprecios en la compra de insumos médicos durante emergencias sanitarias.", date = "2026-09-15", status = CaseStatus.INVESTIGATING),
                        CaseEntity(user_id = u3, title = "Especulación inmobiliaria en suelos protegidos", photo = "", description = "Cambios exprés en el Plan de Ordenamiento Territorial para valorización de terrenos.", date = "2026-09-02", status = CaseStatus.INVESTIGATING),
                        CaseEntity(user_id = u3, title = "Lavado de activos en clubes deportivos", photo = "", description = "Investigación sobre el origen de fondos no registrados en patrocinios y traspasos.", date = "2026-08-20", status = CaseStatus.EDITING),
                        CaseEntity(user_id = u3, title = "Falsificación de medicamentos de alto costo", photo = "", description = "Red de distribución ilegal de fármacos adulterados en farmacias no autorizadas.", date = "2026-08-10", status = CaseStatus.PUBLISHED),
                        CaseEntity(user_id = u3, title = "Apropiación indebida de predios rurales", photo = "", description = "Denuncias de comunidades campesinas afectadas por despojo mediante escrituras falsas.", date = "2026-07-12", status = CaseStatus.CLOSED, conclusion = "Restitución ordenada para tres familias afectadas.")
                    )

                    sampleCases.forEach { caseDao.insert(it) }
                }
            }

            val evidenceDao = database.evidenceDao()
            if (evidenceDao.countEvidences() == 0) {
                val firstCase = caseDao.getCaseById(1L)
                if (firstCase != null) {
                    evidenceDao.insert(
                        EvidenceEntity(
                            case_id = firstCase.case_id,
                            photo = "https://images.unsplash.com/photo-1589829545856-d10d557cf95f",
                            date = "2026-09-11",
                            description = "Copia digitalizada de factura con sobreprecio en material de construcción."
                        )
                    )
                    evidenceDao.insert(
                        EvidenceEntity(
                            case_id = firstCase.case_id,
                            photo = "https://images.unsplash.com/photo-1450133064473-71024230f91b",
                            date = "2026-09-12",
                            description = "Registro de transferencia bancaria no justificada entre contratista y funcionario."
                        )
                    )
                }
            }
        }
    }
}