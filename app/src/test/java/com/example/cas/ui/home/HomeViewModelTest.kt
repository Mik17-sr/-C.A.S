package com.example.cas.ui.home

import com.example.cas.data.local.entity.CaseEntity
import com.example.cas.data.model.CaseStatus
import com.example.cas.data.repository.CaseRepository
import com.example.cas.data.repository.InterviewRepository
import com.example.cas.data.session.SessionManager
import com.example.cas.fakes.FakeCaseDao
import com.example.cas.fakes.FakeInterviewDao
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
class HomeViewModelTest {

    private lateinit var caseDao: FakeCaseDao
    private lateinit var interviewDao: FakeInterviewDao

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        caseDao = FakeCaseDao()
        interviewDao = FakeInterviewDao()
        SessionManager.logout() // sin sesión -> HomeViewModel cae al usuario 1L por defecto
    }

    @After
    fun tearDown() {
        SessionManager.logout()
        Dispatchers.resetMain()
    }

    private fun createViewModel() = HomeViewModel(
        caseRepository = CaseRepository(caseDao),
        interviewRepository = InterviewRepository(interviewDao)
    )

    private fun TestScope.keepStateHot(viewModel: HomeViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
    }

    private fun case(
        id: Long,
        userId: Long,
        title: String,
        date: String,
        status: String,
        conclusion: String? = null
    ) = CaseEntity(
        case_id = id,
        user_id = userId,
        title = title,
        photo = "",
        description = "",
        date = date,
        status = status,
        conclusion = conclusion
    )

    @Test
    fun `combina conteos y lista de casos en un solo estado`() = runTest {
        SessionManager.login(1L)

        caseDao.insert(case(1L, 1L, "Red de sobornos", "2025-03-12", CaseStatus.INVESTIGATING))
        caseDao.insert(case(2L, 1L, "Homicidio industrial", "2025-02-20", CaseStatus.INVESTIGATING))
        caseDao.insert(case(3L, 1L, "Caso cerrado", "2025-01-01", CaseStatus.CLOSED, conclusion = "Resuelto"))
        interviewDao.interviewCountForUser.value = 5

        val viewModel = createViewModel()
        keepStateHot(viewModel)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.activeCases) // el cerrado no cuenta
        assertEquals(5, state.interviews)
        assertEquals(1, state.conclusions)
        assertEquals(
            listOf("Red de sobornos", "Homicidio industrial"),
            state.investigatingCases.map { it.title }
        )
    }

    @Test
    fun `sin casos en investigacion la lista queda vacia`() = runTest {
        SessionManager.login(1L)
        caseDao.insert(case(1L, 1L, "Caso en edición", "2025-01-01", CaseStatus.EDITING))

        val viewModel = createViewModel()
        keepStateHot(viewModel)
        advanceUntilIdle()

        assertEquals(emptyList<HomeCaseItem>(), viewModel.uiState.value.investigatingCases)
    }

    @Test
    fun `el estado se actualiza cuando cambian los datos`() = runTest {
        SessionManager.login(1L)

        val viewModel = createViewModel()
        keepStateHot(viewModel)
        advanceUntilIdle()
        assertEquals(0, viewModel.uiState.value.activeCases)

        caseDao.insert(case(9L, 1L, "Caso nuevo", "2025-04-01", CaseStatus.INVESTIGATING))
        interviewDao.interviewCountForUser.value = 3
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.activeCases)
        assertEquals(3, state.interviews)
        assertEquals(listOf(9L), state.investigatingCases.map { it.id })
    }

    @Test
    fun `los casos de otro usuario no se mezclan con los de la sesion actual`() = runTest {
        SessionManager.login(1L)
        caseDao.insert(case(1L, 1L, "Caso propio", "2025-03-01", CaseStatus.INVESTIGATING))
        caseDao.insert(case(2L, 2L, "Caso de otro usuario", "2025-03-05", CaseStatus.INVESTIGATING))

        val viewModel = createViewModel()
        keepStateHot(viewModel)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.activeCases)
        assertEquals(
            listOf("Caso propio"),
            viewModel.uiState.value.investigatingCases.map { it.title }
        )
    }

    @Test
    fun `sin sesion activa usa el usuario 1 por defecto`() {
        // SessionManager.logout() ya se llamó en setUp()
        createViewModel()

        assertEquals(1L, caseDao.lastActiveUserId)
        assertEquals(CaseStatus.CLOSED, caseDao.lastActiveClosedStatus)
    }

    @Test
    fun `la lista pide casos en investigacion con limite de cinco`() {
        SessionManager.login(7L)
        createViewModel()

        assertEquals(7L, caseDao.lastInvestigatingUserId)
        assertEquals(CaseStatus.INVESTIGATING, caseDao.lastInvestigatingStatus)
        assertEquals(5, caseDao.lastInvestigatingLimit)
    }
}