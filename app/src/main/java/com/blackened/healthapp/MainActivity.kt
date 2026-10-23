package com.blackened.healthapp


import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.blackened.healthapp.data.SessionManager
import com.blackened.healthapp.data.repository.AuthRepositoryImpl
import com.blackened.healthapp.presentation.family.FamilyScreen
import com.blackened.healthapp.presentation.home.CalendarScreen
import com.blackened.healthapp.presentation.login.LoginScreen
import com.blackened.healthapp.presentation.report.ReportScreen
import com.blackened.healthapp.presentation.verification.VerificationScreen

const val VERIFY_SUCCESS_MSG = "Почта успешно подтверждена!"
const val LOGOUT_MSG = "Вы вышли из аккаунта"

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        SessionManager.init(applicationContext)

        setContent {
            var startScreen by remember {mutableStateOf<String?>(null)}
            val navController = rememberNavController()

            LaunchedEffect(Unit) {
                if (SessionManager.token.isNullOrBlank()) {
                    startScreen = "login"
                } else {
                   val isTokenAlive = AuthRepositoryImpl().isTokenValid()

                    if (isTokenAlive) {
                        startScreen = "home"
                    } else {
                        SessionManager.clearSession()
                        startScreen = "login"
                    }
                }
            }

            if (startScreen == null) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                AppNavigation(navController)
            }
        }
    }

    @Composable
    private fun AppNavigation(navController: NavHostController) {

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
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    )
                }
                composable(
                    route = "verification/{email}",
                    arguments = listOf(navArgument("email") { type = NavType.StringType })
                ) { backStackEntry ->
                    val email = backStackEntry.arguments?.getString("email")
                    VerificationScreen(
                        email = email ?: "",
                        onSuccessRoute = {
                            navController.navigate("home") {
                                popUpTo("login") { inclusive = true }
                            }
                            Toast.makeText(
                                this@MainActivity,
                                VERIFY_SUCCESS_MSG,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }

                composable(route = "home") {
                    CalendarScreen(
                        onNavigateToFamily = {
                            navController.navigate("family")
                        },
                        onNavigateToReport = {
                            navController.navigate("report")
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

                composable(route = "report") {
                    ReportScreen(
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(route = "family") {
                    FamilyScreen(
                        onNavigateToCalendar = {
                            navController.navigate("calendar") {
                                popUpTo("family") { inclusive = true }
                            }
                        },
                        onLogout = {
                            SessionManager.clearSession()
                            navController.navigate("login") {
                                popUpTo("family") { inclusive = true }
                            }
                        }
                    )
                }

            }
        }

    }

}
