package com.example.cas.ui.interview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.cas.CasApplication
import com.example.cas.data.repository.CaseRepository
import com.example.cas.ui.home.formatCaseDate
import com.example.cas.data.session.SessionManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class SelectableCase(
    val id: Long,
    val title: String,
    val date: String,
    val status: String
)

class SelectCaseViewModel(
    caseRepository: CaseRepository
) : ViewModel() {

    private val currentUserId = SessionManager.currentUserId ?: 1L

    val cases: StateFlow<List<SelectableCase>> = caseRepository.getAllCasesForUser(currentUserId)
        .map { list ->
            list.map { case ->
                SelectableCase(
                    id = case.case_id,
                    title = case.title,
                    date = formatCaseDate(case.date),
                    status = case.status
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as CasApplication
                SelectCaseViewModel(app.caseRepository)
            }
        }
    }
}