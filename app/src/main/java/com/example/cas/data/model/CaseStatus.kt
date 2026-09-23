package com.example.cas.data.model

object CaseStatus {
    const val INVESTIGATING = "En investigación"
    const val EDITING = "En edición"
    const val PUBLISHED = "Publicado"
    const val CLOSED: String = "Cerrado"

    val all = listOf(INVESTIGATING, EDITING, PUBLISHED, CLOSED)
}