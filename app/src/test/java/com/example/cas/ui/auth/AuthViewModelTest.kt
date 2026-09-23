package com.example.cas.ui.auth

import com.example.cas.data.repository.UserRepository
import com.example.cas.data.session.SessionManager
import com.example.cas.fakes.FakeUserDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private lateinit var userDao: FakeUserDao
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        userDao = FakeUserDao()
        viewModel = AuthViewModel(UserRepository(userDao))
        SessionManager.logout()
    }

    @After
    fun tearDown() {
        SessionManager.logout()
        Dispatchers.resetMain()
    }

    @Test
    fun `login vacio muestra error`() = runTest {
        viewModel.login("", "")
        assertTrue(viewModel.authState.value is AuthState.Error)
    }

    @Test
    fun `login con credenciales incorrectas muestra error`() = runTest {
        viewModel.login("no-existe@test.com", "123456")

        assertTrue(viewModel.authState.value is AuthState.Error)
        assertNull(SessionManager.currentUserId)
    }

    @Test
    fun `registro con username vacio muestra error`() = runTest {
        viewModel.register("Ana Torres", "  ", "ana@test.com", "123456")

        assertTrue(viewModel.authState.value is AuthState.Error)
        assertEquals(0, userDao.countUsers())
    }

    @Test
    fun `registro exitoso inicia sesion automaticamente`() = runTest {
        viewModel.register("Ana Torres", "anatorres", "ana@test.com", "123456")

        val state = viewModel.authState.value
        assertTrue(state is AuthState.Success)
        val userId = (state as AuthState.Success).userId
        assertEquals(userId, SessionManager.currentUserId)
        assertNotNull(userDao.getUserByEmail("ana@test.com"))
    }

    @Test
    fun `registro con correo repetido muestra error`() = runTest {
        viewModel.register("Ana Torres", "anatorres", "ana@test.com", "123456")
        viewModel.register("Otra Persona", "otrapersona", "ana@test.com", "654321")

        assertTrue(viewModel.authState.value is AuthState.Error)
        assertEquals(1, userDao.countUsers())
    }

    @Test
    fun `login exitoso despues de registrarse`() = runTest {
        viewModel.register("Ana Torres", "anatorres", "ana@test.com", "123456")
        SessionManager.logout()

        viewModel.login("ana@test.com", "123456")

        val state = viewModel.authState.value
        assertTrue(state is AuthState.Success)
        assertNotNull(SessionManager.currentUserId)
    }

    @Test
    fun `login funciona tambien con el nombre de usuario en vez del correo`() = runTest {
        viewModel.register("Ana Torres", "anatorres", "ana@test.com", "123456")
        SessionManager.logout()

        viewModel.login("anatorres", "123456")

        assertTrue(viewModel.authState.value is AuthState.Success)
    }

    @Test
    fun `resetPassword con correo inexistente muestra error`() = runTest {
        viewModel.resetPassword("nadie@test.com", "nueva1234")
        assertTrue(viewModel.authState.value is AuthState.Error)
    }

    @Test
    fun `resetPassword actualiza la contrasena y permite iniciar sesion`() = runTest {
        viewModel.register("Ana Torres", "anatorres", "ana@test.com", "123456")
        SessionManager.logout()

        viewModel.resetPassword("ana@test.com", "nueva1234")
        assertTrue(viewModel.authState.value is AuthState.ResetSuccess)

        viewModel.login("ana@test.com", "nueva1234")
        assertTrue(viewModel.authState.value is AuthState.Success)
    }
}