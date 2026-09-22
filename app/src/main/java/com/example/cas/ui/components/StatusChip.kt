package com.example.cas.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cas.data.model.CaseStatus
import com.example.cas.ui.theme.StatusClosedBg
import com.example.cas.ui.theme.StatusClosedText
import com.example.cas.ui.theme.StatusEditingBg
import com.example.cas.ui.theme.StatusEditingText
import com.example.cas.ui.theme.StatusInvestigatingBg
import com.example.cas.ui.theme.StatusInvestigatingText
import com.example.cas.ui.theme.StatusPublishedBg
import com.example.cas.ui.theme.StatusPublishedText

@Composable
fun StatusChip(status: String, modifier: Modifier = Modifier) {
    val (background, content) = when (status) {
        CaseStatus.INVESTIGATING -> StatusInvestigatingBg to StatusInvestigatingText
        CaseStatus.EDITING -> StatusEditingBg to StatusEditingText
        CaseStatus.PUBLISHED -> StatusPublishedBg to StatusPublishedText
        else -> StatusClosedBg to StatusClosedText
    }

    Text(
        text = status,
        style = MaterialTheme.typography.labelMedium,
        color = content,
        modifier = modifier
            .background(background, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}