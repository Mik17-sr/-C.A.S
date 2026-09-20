package com.example.cas.ui.home

import com.example.cas.data.local.entity.CaseEntity
import com.example.cas.data.model.CaseStatus
import com.example.cas.data.model.CaseWithInterviewCount
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeMappersTest {

    @Test
    fun `formatCaseDate convierte una fecha valida al formato de pantalla`() {
        assertEquals("12 mar 2025", formatCaseDate("2025-03-12"))
    }

    @Test
    fun `formatCaseDate no agrega cero al dia`() {
        assertEquals("5 feb 2025", formatCaseDate("2025-02-05"))
    }

    @Test
    fun `formatCaseDate devuelve el texto original si no es una fecha`() {
        assertEquals("sin fecha", formatCaseDate("sin fecha"))
    }

    @Test
    fun `formatCaseDate devuelve el texto original si la fecha no existe`() {
        assertEquals("2025-13-45", formatCaseDate("2025-13-45"))
    }

    @Test
    fun `formatCaseDate devuelve vacio si recibe vacio`() {
        assertEquals("", formatCaseDate(""))
    }

    @Test
    fun `toHomeItem copia los datos del caso y formatea la fecha`() {
        val row = CaseWithInterviewCount(
            case = CaseEntity(
                case_id = 7L,
                user_id = 1L,
                title = "Red de sobornos",
                photo = "",
                description = "Descripción",
                date = "2025-03-12",
                status = CaseStatus.INVESTIGATING
            ),
            interviewCount = 4
        )

        val item = row.toHomeItem()

        assertEquals(
            HomeCaseItem(
                id = 7L,
                title = "Red de sobornos",
                date = "12 mar 2025",
                interviewCount = 4,
                status = CaseStatus.INVESTIGATING
            ),
            item
        )
    }
}