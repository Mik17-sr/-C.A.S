package com.example.cas.ui.home

data class HomeCaseItem(
    val id: Long,
    val title: String,
    val date: String,
    val interviewCount: Int,
    val status: String
)

data class HomeUiState(
    val activeCases: Int = 0,
    val interviews: Int = 0,
    val conclusions: Int = 0,
    val investigatingCases: List<HomeCaseItem> = emptyList()
)