package com.example.cas.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.cas.CasApplication
import com.example.cas.data.local.entity.UserEntity
import com.example.cas.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: UserEntity) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val user = userRepository.userDao.getUserByUsername(username, password)
            if (user != null) {
                _authState.value = AuthState.Success(user)
            } else {
                _authState.value = AuthState.Error("Usuario o contraseña incorrectos")
            }
        }
    }

    fun register(name: String, email: String, username: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val newUser = UserEntity(
                name = name,
                photo = "",
                email = email,
                username = username,
                password = password
            )
            val id = userRepository.insertUser(newUser)
            if (id > 0) {
                _authState.value = AuthState.Success(newUser.copy(user_id = id))
            } else {
                _authState.value = AuthState.Error("Error al registrar el usuario")
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as CasApplication
                return AuthViewModel(application.userRepository) as T
            }
        }
    }
}