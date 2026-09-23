package com.example.cas.ui.case

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.cas.CasApplication
import com.example.cas.data.local.entity.CaseEntity
import com.example.cas.data.local.entity.EvidenceEntity
import com.example.cas.data.local.entity.InterviewEntity
import com.example.cas.data.local.entity.RecordEntity
import com.example.cas.data.repository.CaseRepository
import com.example.cas.data.repository.EvidenceRepository
import com.example.cas.data.repository.InterviewRepository
import com.example.cas.data.repository.RecordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.io.File

sealed class DetailUiState {
    object Loading : DetailUiState()
    data class Success(
        val case: CaseEntity,
        val interviews: List<InterviewEntity>,
        val evidences: List<EvidenceEntity>,
        val records: List<RecordEntity>
    ) : DetailUiState()
    object Deleted : DetailUiState()
    data class Error(val message: String) : DetailUiState()
}

class CaseDetailViewModel(
    private val caseId: Long,
    private val caseRepository: CaseRepository,
    private val interviewRepository: InterviewRepository,
    private val evidenceRepository: EvidenceRepository,
    private val recordRepository: RecordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    private val _saveStatus = MutableStateFlow<String?>(null)
    val saveStatus: StateFlow<String?> = _saveStatus.asStateFlow()

    init {
        loadCaseDetails()
    }

    fun loadCaseDetails() {
        viewModelScope.launch {
            val caseEntity = caseRepository.getCaseById(caseId)
            if (caseEntity == null) {
                _uiState.value = DetailUiState.Error("Caso no encontrado.")
                return@launch
            }

            combine(
                interviewRepository.getInterviewsByCase(caseId),
                evidenceRepository.getEvidencesByCase(caseId),
                recordRepository.getRecordsByCase(caseId)
            ) { interviews, evidences, records ->
                DetailUiState.Success(
                    case = caseEntity,
                    interviews = interviews,
                    evidences = evidences,
                    records = records
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun toggleEdit() {
        _isEditing.value = !_isEditing.value
        _saveStatus.value = null
    }

    fun updateCase(
        title: String,
        description: String,
        date: String,
        status: String,
        conclusion: String?
    ) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is DetailUiState.Success) {
                val updated = currentState.case.copy(
                    title = title.trim(),
                    description = description.trim(),
                    date = date,
                    status = status,
                    conclusion = conclusion?.trim()
                )
                caseRepository.updateCase(updated)
                _saveStatus.value = "Caso actualizado correctamente."
                _isEditing.value = false
                _uiState.value = currentState.copy(case = updated)
            }
        }
    }

    fun addEvidence(photoPath: String, description: String, date: String) {
        viewModelScope.launch {
            if (photoPath.isBlank()) return@launch
            evidenceRepository.insertEvidence(
                EvidenceEntity(
                    case_id = caseId,
                    photo = photoPath,
                    date = date,
                    description = description.trim()
                )
            )
            _saveStatus.value = "Evidencia agregada correctamente."
        }
    }

    fun deleteEvidence(evidence: EvidenceEntity) {
        viewModelScope.launch {
            evidenceRepository.deleteEvidence(evidence)
            runCatching { File(evidence.photo).delete() }
        }
    }

    fun deleteCase() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is DetailUiState.Success) {
                caseRepository.deleteCase(currentState.case)
                _uiState.value = DetailUiState.Deleted
            }
        }
    }

    fun clearSaveStatus() {
        _saveStatus.value = null
    }

    companion object {
        fun provideFactory(caseId: Long): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as CasApplication
                CaseDetailViewModel(
                    caseId,
                    app.caseRepository,
                    app.interviewRepository,
                    app.evidenceRepository,
                    app.recordRepository
                )
            }
        }
    }
}