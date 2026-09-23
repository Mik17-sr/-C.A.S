package com.example.cas.ui.case

import com.example.cas.data.model.CaseStatus
import com.example.cas.data.repository.CaseRepository
import com.example.cas.data.session.SessionManager
import com.example.cas.fakes.FakeCaseDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CaseFormViewModelTest {

    private lateinit var caseDao: FakeCaseDao
    private lateinit var viewModel: CaseFormViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        caseDao = FakeCaseDao()
        viewModel = CaseFormViewModel(CaseRepository(caseDao))
    }

    @After
    fun tearDown() {
        SessionManager.logout()
        Dispatchers.resetMain()
    }

    @Test
    fun `titulo vacio no guarda el caso`() = runTest {
        SessionManager.login(1L)

        viewModel.createCase("", "Descripción", "2025-03-12", CaseStatus.INVESTIGATING)

        assertTrue(viewModel.saveState.value is SaveCaseState.Error)
        assertEquals(0, caseDao.countCases())
    }

    @Test
    fun `descripcion vacia no guarda el caso`() = runTest {
        SessionManager.login(1L)

        viewModel.createCase("Título", "  ", "2025-03-12", CaseStatus.INVESTIGATING)

        assertTrue(viewModel.saveState.value is SaveCaseState.Error)
        assertEquals(0, caseDao.countCases())
    }

    @Test
    fun `sin fecha no guarda el caso`() = runTest {
        SessionManager.login(1L)

        viewModel.createCase("Título", "Descripción", "", CaseStatus.INVESTIGATING)

        assertTrue(viewModel.saveState.value is SaveCaseState.Error)
    }

    @Test
    fun `sin sesion activa no se puede guardar`() = runTest {
        SessionManager.logout()

        viewModel.createCase("Título", "Descripción", "2025-03-12", CaseStatus.INVESTIGATING)

        val state = viewModel.saveState.value
        assertTrue(state is SaveCaseState.Error)
        assertEquals(0, caseDao.countCases())
    }

    @Test
    fun `datos validos guardan el caso con el usuario de la sesion`() = runTest {
        SessionManager.login(42L)

        viewModel.createCase(
            title = "  Red de sobornos  ",
            description = "  Investigación en curso  ",
            date = "2025-03-12",
            status = CaseStatus.EDITING
        )

        assertEquals(SaveCaseState.Success, viewModel.saveState.value)
        assertEquals(1, caseDao.countCases())

        val saved = caseDao.getCaseById(1L)
        assertEquals("Red de sobornos", saved?.title) // recortado, sin espacios
        assertEquals("Investigación en curso", saved?.description)
        assertEquals(42L, saved?.user_id)
        assertEquals(CaseStatus.EDITING, saved?.status)
        assertNull(saved?.conclusion)
    }
}