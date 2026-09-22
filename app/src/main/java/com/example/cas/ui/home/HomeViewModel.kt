package com.example.cas.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.cas.CasApplication
import com.example.cas.data.model.CaseStatus
import com.example.cas.data.repository.CaseRepository
import com.example.cas.data.repository.InterviewRepository
import com.example.cas.data.session.SessionManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    caseRepository: CaseRepository,
    interviewRepository: InterviewRepository
) : ViewModel() {

    private val currentUserId = SessionManager.currentUserId ?: 1L

    val uiState: StateFlow<HomeUiState> = combine(
        caseRepository.activeCasesCountForUser(currentUserId),
        interviewRepository.countInterviewsForUser(currentUserId),
        caseRepository.conclusionsCountForUser(currentUserId),
        caseRepository.getCasesWithInterviewCountForUser(currentUserId, CaseStatus.INVESTIGATING, RECENT_LIMIT)
    ) { activeCases, interviews, conclusions, investigating ->
        HomeUiState(
            activeCases = activeCases,
            interviews = interviews,
            conclusions = conclusions,
            investigatingCases = investigating.map { it.toHomeItem() }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

    companion object {
        private const val RECENT_LIMIT = 5

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as CasApplication
                HomeViewModel(app.caseRepository, app.interviewRepository)
            }
        }
    }
}