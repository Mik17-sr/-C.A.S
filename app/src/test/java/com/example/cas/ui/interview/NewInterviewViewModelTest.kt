package com.example.cas.ui.interview

import com.example.cas.data.repository.InterviewRepository
import com.example.cas.data.repository.RecordRepository
import com.example.cas.fakes.FakeInterviewDao
import com.example.cas.fakes.FakeRecordDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NewInterviewViewModelTest {

    private lateinit var interviewDao: FakeInterviewDao
    private lateinit var recordDao: FakeRecordDao
    private lateinit var viewModel: NewInterviewViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        interviewDao = FakeInterviewDao()
        recordDao = FakeRecordDao()
        viewModel = NewInterviewViewModel(
            InterviewRepository(interviewDao),
            RecordRepository(recordDao)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `caso invalido no guarda la entrevista`() = runTest {
        viewModel.createInterview(
            caseId = 0L,
            person = "Ana",
            findings = "Algo",
            description = "Detalle",
            date = "2025-03-12",
            audioPath = null,
            audioDurationSeconds = 0
        )

        assertTrue(viewModel.saveState.value is SaveInterviewState.Error)
    }

    @Test
    fun `entrevistado vacio no guarda la entrevista`() = runTest {
        viewModel.createInterview(
            caseId = 1L, person = "  ", findings = "Algo", description = "Detalle",
            date = "2025-03-12", audioPath = null, audioDurationSeconds = 0
        )

        assertTrue(viewModel.saveState.value is SaveInterviewState.Error)
    }

    @Test
    fun `hallazgos vacios no guardan la entrevista`() = runTest {
        viewModel.createInterview(
            caseId = 1L, person = "Ana", findings = " ", description = "Detalle",
            date = "2025-03-12", audioPath = null, audioDurationSeconds = 0
        )

        assertTrue(viewModel.saveState.value is SaveInterviewState.Error)
    }

    @Test
    fun `datos validos sin audio guardan la entrevista sin grabacion`() = runTest {
        viewModel.createInterview(
            caseId = 1L,
            person = "Ana Torres",
            findings = "Confirma el pago",
            description = "Reunión en la fiscalía",
            date = "2025-03-12",
            audioPath = null,
            audioDurationSeconds = 0
        )

        assertEquals(SaveInterviewState.Success, viewModel.saveState.value)
        val savedInterviews = interviewDao.getInterviewsByCase(1L).first()
        assertEquals(1, savedInterviews.size)
        assertEquals(0, recordDao.getRecordsByInterview(savedInterviews.first().interview_id).first().size)
    }

    @Test
    fun `datos validos con audio guardan tambien la grabacion`() = runTest {
        viewModel.createInterview(
            caseId = 1L,
            person = "Ana Torres",
            findings = "Confirma el pago",
            description = "Reunión en la fiscalía",
            date = "2025-03-12",
            audioPath = "/data/data/com.example.cas/files/audio_recordings/interview_123.m4a",
            audioDurationSeconds = 45
        )

        assertEquals(SaveInterviewState.Success, viewModel.saveState.value)
        val savedInterview = interviewDao.getInterviewsByCase(1L).first().first()
        val records = recordDao.getRecordsByInterview(savedInterview.interview_id).first()

        assertEquals(1, records.size)
        assertEquals(
            "/data/data/com.example.cas/files/audio_recordings/interview_123.m4a",
            records.first().audio_path
        )
        assertTrue(records.first().content.contains("45"))
    }
}