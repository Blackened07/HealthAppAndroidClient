package com.blackened.healthapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.blackened.healthapp.data.SessionManager
import com.blackened.healthapp.presentation.CalendarScreen
import com.blackened.healthapp.presentation.LoginScreen
import com.blackened.healthapp.presentation.VerificationScreen

const val VERIFY_SUCCESS_MSG = "Почта успешно подтверждена!"
const val LOGOUT_MSG = "Вы вышли из аккаунта"
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        SessionManager.init(applicationContext)

        setContent {
            val navController = rememberNavController()

            val startScreen = if (SessionManager.isUserLoggedIn()) "home" else "login"

            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = startScreen,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable("login") {
                        LoginScreen(
                            onNavigateToVerification = { email ->
                                navController.navigate("verification/$email")
                            },
                            onLoginSuccess = {
                                navController.navigate("home") {
                                    popUpTo("login") {inclusive = true}
                                }
                            }
                        )
                    }
                    composable(
                        route = "verification/{email}",
                        arguments = listOf(navArgument("email") { type = NavType.StringType })
                    ) {
                        backStackEntry ->
                        val email = backStackEntry.arguments?.getString("email")
                        VerificationScreen(
                            email = email ?: "",
                            onSuccessRoute = {
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                                Toast.makeText(this@MainActivity, VERIFY_SUCCESS_MSG, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    composable(route = "home") {
                        CalendarScreen(
                            onNavigateToFamily = {
                                navController.navigate("family")
                            },
                            onLogout = {
                                SessionManager.clearSession()
                                navController.navigate("login") {
                                    popUpTo("home") { inclusive = true }
                                }
                                Toast.makeText(this@MainActivity, LOGOUT_MSG, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                }
            }
        }
    }
}
