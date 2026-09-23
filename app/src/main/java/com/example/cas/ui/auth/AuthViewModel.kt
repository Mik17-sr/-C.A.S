package com.example.cas.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.cas.CasApplication
import com.example.cas.data.local.entity.UserEntity
import com.example.cas.data.repository.UserRepository
import com.example.cas.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val userId: Long) : AuthState()
    data class Error(val message: String) : AuthState()
    data class ResetSuccess(val message: String) : AuthState()
}

class AuthViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val trimmedEmail = email.trim()

            if (trimmedEmail.isEmpty() || password.isEmpty()) {
                _authState.value = AuthState.Error("Ingresa tu correo y tu contraseña.")
                return@launch
            }

            val user = userRepository.login(trimmedEmail, password)
            _authState.value = if (user != null) {
                SessionManager.login(user.user_id)
                AuthState.Success(user.user_id)
            } else {
                AuthState.Error("Correo o contraseña incorrectos.")
            }
        }
    }

    fun register(name: String, username: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val trimmedName = name.trim()
            val trimmedUsername = username.trim()
            val trimmedEmail = email.trim()

            when {
                trimmedName.isEmpty() -> {
                    _authState.value = AuthState.Error("Ingresa tu nombre.")
                }
                trimmedUsername.isEmpty() -> {
                    _authState.value = AuthState.Error("Ingresa tu nombre de usuario.")
                }
                trimmedEmail.isEmpty() -> {
                    _authState.value = AuthState.Error("Ingresa tu correo.")
                }
                password.length < 4 -> {
                    _authState.value = AuthState.Error("La contraseña debe tener al menos 6 caracteres.")
                }
                userRepository.getUserByEmail(trimmedEmail) != null -> {
                    _authState.value = AuthState.Error("Ya existe una cuenta con ese correo.")
                }
                else -> {
                    val newId = userRepository.insertUser(
                        UserEntity(
                            name = trimmedName,
                            photo = "",
                            email = trimmedEmail,
                            username = trimmedUsername,
                            password = password
                        )
                    )
                    SessionManager.login(newId)
                    _authState.value = AuthState.Success(newId)
                }
            }
        }
    }

    fun resetPassword(email: String, newPassword: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val trimmedEmail = email.trim()
            val user = userRepository.getUserByEmail(trimmedEmail)

            when {
                user == null -> {
                    _authState.value = AuthState.Error("No encontramos una cuenta con ese correo.")
                }
                newPassword.length < 4 -> {
                    _authState.value = AuthState.Error("La nueva contraseña debe tener al menos 4 caracteres.")
                }
                else -> {
                    userRepository.updateUser(user.copy(password = newPassword))
                    _authState.value = AuthState.ResetSuccess("Contraseña actualizada. Ya puedes iniciar sesión.")
                }
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as CasApplication
                AuthViewModel(app.userRepository)
            }
        }
    }
}