package com.example.cas.ui.navigation

object Routes {
    // Pestañas de la barra inferior
    const val HOME = "home"
    const val CASES = "cases"
    const val SETTINGS = "settings"

    // Pantallas que se abren desde otras
    const val NEW_CASE = "new_case"
    const val NEW_INTERVIEW = "new_interview"

    const val CASE_ID_ARG = "caseId"
    const val CASE_DETAIL = "case_detail/{$CASE_ID_ARG}"

    fun caseDetail(caseId: Long) = "case_detail/$caseId"
}