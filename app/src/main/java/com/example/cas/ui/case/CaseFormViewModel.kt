package com.example.cas.ui.case

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.cas.CasApplication
import com.example.cas.data.local.entity.CaseEntity
import com.example.cas.data.repository.CaseRepository
import com.example.cas.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SaveCaseState {
    object Idle : SaveCaseState()
    object Saving : SaveCaseState()
    object Success : SaveCaseState()
    data class Error(val message: String) : SaveCaseState()
}

class CaseFormViewModel(
    private val caseRepository: CaseRepository
) : ViewModel() {

    private val _saveState = MutableStateFlow<SaveCaseState>(SaveCaseState.Idle)
    val saveState: StateFlow<SaveCaseState> = _saveState.asStateFlow()

    fun createCase(title: String, description: String, date: String, status: String) {
        viewModelScope.launch {
            _saveState.value = SaveCaseState.Saving

            val trimmedTitle = title.trim()
            val trimmedDescription = description.trim()
            val userId = SessionManager.currentUserId

            when {
                trimmedTitle.isEmpty() -> {
                    _saveState.value = SaveCaseState.Error("El título es obligatorio.")
                }
                trimmedDescription.isEmpty() -> {
                    _saveState.value = SaveCaseState.Error("La descripción es obligatoria.")
                }
                date.isEmpty() -> {
                    _saveState.value = SaveCaseState.Error("Elige una fecha.")
                }
                userId == null -> {
                    _saveState.value = SaveCaseState.Error("No hay una sesión activa. Inicia sesión de nuevo.")
                }
                else -> {
                    caseRepository.insertCase(
                        CaseEntity(
                            user_id = userId,
                            title = trimmedTitle,
                            photo = "",
                            description = trimmedDescription,
                            date = date,
                            status = status
                        )
                    )
                    _saveState.value = SaveCaseState.Success
                }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as CasApplication
                CaseFormViewModel(app.caseRepository)
            }
        }
    }
}