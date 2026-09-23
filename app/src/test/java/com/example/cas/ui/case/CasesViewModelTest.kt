package com.example.cas.ui.case

import com.example.cas.data.local.entity.CaseEntity
import com.example.cas.data.model.CaseStatus
import com.example.cas.data.repository.CaseRepository
import com.example.cas.data.session.SessionManager
import com.example.cas.fakes.FakeCaseDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CasesViewModelTest {

    private lateinit var caseDao: FakeCaseDao

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        caseDao = FakeCaseDao()
    }

    @After
    fun tearDown() {
        SessionManager.logout()
        Dispatchers.resetMain()
    }

    private fun TestScope.keepStateHot(viewModel: CasesViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.cases.collect { }
        }
    }

    private fun case(id: Long, userId: Long, title: String) = CaseEntity(
        case_id = id, user_id = userId, title = title, photo = "",
        description = "", date = "2025-03-0$id", status = CaseStatus.INVESTIGATING
    )

    @Test
    fun `solo muestra los casos del usuario logueado`() = runTest {
        SessionManager.login(1L)
        caseDao.insert(case(1L, 1L, "Red de sobornos"))
        caseDao.insert(case(2L, 1L, "Homicidio industrial"))
        caseDao.insert(case(3L, 2L, "Caso de otro investigador"))

        val viewModel = CasesViewModel(CaseRepository(caseDao))
        keepStateHot(viewModel)
        advanceUntilIdle()

        assertEquals(2, viewModel.cases.value.size)
    }

    @Test
    fun `buscar filtra por titulo`() = runTest {
        SessionManager.login(1L)
        caseDao.insert(case(1L, 1L, "Red de sobornos"))
        caseDao.insert(case(2L, 1L, "Homicidio industrial"))

        val viewModel = CasesViewModel(CaseRepository(caseDao))
        keepStateHot(viewModel)
        advanceUntilIdle()

        viewModel.onSearchQueryChange("sobornos")
        advanceUntilIdle()

        assertEquals(1, viewModel.cases.value.size)
        assertEquals("Red de sobornos", viewModel.cases.value.first().title)
    }

    @Test
    fun `buscar sin coincidencias deja la lista vacia`() = runTest {
        SessionManager.login(1L)
        caseDao.insert(case(1L, 1L, "Red de sobornos"))

        val viewModel = CasesViewModel(CaseRepository(caseDao))
        keepStateHot(viewModel)
        advanceUntilIdle()

        viewModel.onSearchQueryChange("palabra que no existe")
        advanceUntilIdle()

        assertEquals(emptyList<Any>(), viewModel.cases.value)
    }
}