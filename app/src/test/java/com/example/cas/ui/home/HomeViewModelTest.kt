package com.example.cas.ui.home

import com.example.cas.data.local.entity.CaseEntity
import com.example.cas.data.model.CaseStatus
import com.example.cas.data.model.CaseWithInterviewCount
import com.example.cas.data.repository.CaseRepository
import com.example.cas.data.repository.InterviewRepository
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
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = HomeViewModel(
        caseRepository = CaseRepository(caseDao),
        interviewRepository = InterviewRepository(interviewDao)
    )

    // El estado solo se calcula mientras alguien lo observa, igual que la pantalla real.
    private fun TestScope.keepStateHot(viewModel: HomeViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
    }

    private fun caseRow(id: Long, title: String, date: String, interviews: Int) =
        CaseWithInterviewCount(
            case = CaseEntity(
                case_id = id,
                user_id = 1L,
                title = title,
                photo = "",
                description = "",
                date = date,
                status = CaseStatus.INVESTIGATING
            ),
            interviewCount = interviews
        )

    @Test
    fun `combina conteos y lista de casos en un solo estado`() = runTest {
        caseDao.activeCount.value = 3
        interviewDao.interviewCount.value = 5
        caseDao.conclusionsCount.value = 2
        caseDao.investigating.value = listOf(
            caseRow(1L, "Red de sobornos", "2025-03-12", 4),
            caseRow(2L, "Homicidio industrial", "2025-02-20", 1)
        )

        val viewModel = createViewModel()
        keepStateHot(viewModel)
        advanceUntilIdle()

        assertEquals(
            HomeUiState(
                activeCases = 3,
                interviews = 5,
                conclusions = 2,
                investigatingCases = listOf(
                    HomeCaseItem(1L, "Red de sobornos", "12 mar 2025", 4, CaseStatus.INVESTIGATING),
                    HomeCaseItem(2L, "Homicidio industrial", "20 feb 2025", 1, CaseStatus.INVESTIGATING)
                )
            ),
            viewModel.uiState.value
        )
    }

    @Test
    fun `sin casos en investigacion la lista queda vacia`() = runTest {
        caseDao.activeCount.value = 1

        val viewModel = createViewModel()
        keepStateHot(viewModel)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.activeCases)
        assertEquals(emptyList<HomeCaseItem>(), viewModel.uiState.value.investigatingCases)
    }

    @Test
    fun `el estado se actualiza cuando cambian los datos`() = runTest {
        val viewModel = createViewModel()
        keepStateHot(viewModel)
        advanceUntilIdle()
        assertEquals(0, viewModel.uiState.value.activeCases)

        caseDao.activeCount.value = 4
        interviewDao.interviewCount.value = 9
        caseDao.investigating.value = listOf(caseRow(7L, "Caso nuevo", "2025-04-01", 0))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(4, state.activeCases)
        assertEquals(9, state.interviews)
        assertEquals(listOf(7L), state.investigatingCases.map { it.id })
    }

    @Test
    fun `los casos cerrados se excluyen del conteo de activos`() {
        createViewModel()

        assertEquals(CaseStatus.CLOSED, caseDao.lastClosedStatus)
    }

    @Test
    fun `la lista pide casos en investigacion con limite de cinco`() {
        createViewModel()

        assertEquals(CaseStatus.INVESTIGATING, caseDao.lastRequestedStatus)
        assertEquals(5, caseDao.lastRequestedLimit)
    }
}