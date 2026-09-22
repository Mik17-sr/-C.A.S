package com.example.cas.ui.navigation

object Routes {
    const val HOME = "home"
    const val CASES = "cases"
    const val SETTINGS = "settings"

    const val NEW_CASE = "new_case"

    const val CASE_ID_ARG = "caseId"

    const val CASE_DETAIL = "case_detail/{$CASE_ID_ARG}"
    fun caseDetail(caseId: Long) = "case_detail/$caseId"

    const val NEW_INTERVIEW = "new_interview/{$CASE_ID_ARG}"
    fun newInterview(caseId: Long) = "new_interview/$caseId"

    const val SELECT_CASE_FOR_INTERVIEW = "select_case_for_interview"
}