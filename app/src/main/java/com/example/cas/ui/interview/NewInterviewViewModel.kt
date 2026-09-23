package com.example.cas.ui.interview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.cas.CasApplication
import com.example.cas.data.local.entity.InterviewEntity
import com.example.cas.data.local.entity.RecordEntity
import com.example.cas.data.repository.InterviewRepository
import com.example.cas.data.repository.RecordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SaveInterviewState {
    object Idle : SaveInterviewState()
    object Saving : SaveInterviewState()
    object Success : SaveInterviewState()
    data class Error(val message: String) : SaveInterviewState()
}

class NewInterviewViewModel(
    private val interviewRepository: InterviewRepository,
    private val recordRepository: RecordRepository
) : ViewModel() {

    private val _saveState = MutableStateFlow<SaveInterviewState>(SaveInterviewState.Idle)
    val saveState: StateFlow<SaveInterviewState> = _saveState.asStateFlow()

    fun createInterview(
        caseId: Long,
        person: String,
        findings: String,
        description: String,
        date: String,
        audioPath: String?,
        audioDurationSeconds: Int
    ) {
        viewModelScope.launch {
            _saveState.value = SaveInterviewState.Saving

            val trimmedPerson = person.trim()
            val trimmedFindings = findings.trim()
            val trimmedDescription = description.trim()

            when {
                caseId <= 0 -> {
                    _saveState.value = SaveInterviewState.Error("Debes seleccionar un caso válido.")
                }
                trimmedPerson.isEmpty() -> {
                    _saveState.value = SaveInterviewState.Error("El nombre del entrevistado es obligatorio.")
                }
                trimmedFindings.isEmpty() -> {
                    _saveState.value = SaveInterviewState.Error("Registra los hallazgos principales.")
                }
                trimmedDescription.isEmpty() -> {
                    _saveState.value = SaveInterviewState.Error("La descripción es obligatoria.")
                }
                date.isEmpty() -> {
                    _saveState.value = SaveInterviewState.Error("Selecciona una fecha.")
                }
                else -> {
                    val interviewId = interviewRepository.insertInterview(
                        InterviewEntity(
                            case_id = caseId,
                            person = trimmedPerson,
                            findings = trimmedFindings,
                            description = trimmedDescription,
                            date = date
                        )
                    )

                    if (!audioPath.isNullOrEmpty()) {
                        recordRepository.insertRecord(
                            RecordEntity(
                                interview_id = interviewId,
                                content = "Grabación de audio (${audioDurationSeconds}s)",
                                audio_path = audioPath,
                                date = date
                            )
                        )
                    }

                    _saveState.value = SaveInterviewState.Success
                }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as CasApplication
                NewInterviewViewModel(app.interviewRepository, app.recordRepository)
            }
        }
    }
}