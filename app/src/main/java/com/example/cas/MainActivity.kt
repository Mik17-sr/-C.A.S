package com.example.cas

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cas.ui.auth.ForgotPasswordScreen
import com.example.cas.ui.auth.LoginScreen
import com.example.cas.ui.auth.RegisterScreen
import com.example.cas.ui.navigation.CasApp
import com.example.cas.ui.theme.CASTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        setContent {
            CASTheme {
                RootNavHost()
            }
        }
    }
}

@Composable
private fun RootNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("app") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate("register") },
                onNavigateToForgotPassword = { navController.navigate("forgot_password") }
            )
        }
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("app") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }
        composable("forgot_password") {
            ForgotPasswordScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable("app") {
            CasApp(
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("app") { inclusive = true }
                    }
                }
            )
        }
    }
}