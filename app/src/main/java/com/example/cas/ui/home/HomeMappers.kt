package com.example.cas.ui.home

import com.example.cas.data.model.CaseWithInterviewCount
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

private const val STORED_DATE_PATTERN = "yyyy-MM-dd"
private const val DISPLAY_DATE_PATTERN = "d MMM yyyy"

fun formatCaseDate(storedDate: String): String {
    return try {
        val parser = SimpleDateFormat(STORED_DATE_PATTERN, Locale.US).apply { isLenient = false }
        val date = parser.parse(storedDate) ?: return storedDate
        SimpleDateFormat(DISPLAY_DATE_PATTERN, Locale.forLanguageTag("es")).format(date)
    } catch (e: ParseException) {
        storedDate
    }
}

fun CaseWithInterviewCount.toHomeItem(): HomeCaseItem = HomeCaseItem(
    id = case.case_id,
    title = case.title,
    date = formatCaseDate(case.date),
    interviewCount = interviewCount,
    status = case.status
)