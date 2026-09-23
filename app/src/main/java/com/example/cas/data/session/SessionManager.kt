package com.example.cas.data.session

object SessionManager {

    var currentUserId: Long? = null
        private set

    fun login(userId: Long) {
        currentUserId = userId
    }

    fun logout() {
        currentUserId = null
    }
}