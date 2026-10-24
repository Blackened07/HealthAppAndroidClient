package com.blackened.healthapp


import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.blackened.healthapp.data.SessionManager
import com.blackened.healthapp.presentation.family.FamilyScreen
import com.blackened.healthapp.presentation.home.CalendarScreen
import com.blackened.healthapp.presentation.login.AuthStateUi
import com.blackened.healthapp.presentation.login.LoginScreen
import com.blackened.healthapp.presentation.login.MainViewModel
import com.blackened.healthapp.presentation.report.ReportScreen
import com.blackened.healthapp.presentation.verification.VerificationScreen

const val VERIFY_SUCCESS_MSG = "Почта успешно подтверждена!"
const val LOGOUT_MSG = "Вы вышли из аккаунта"

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        SessionManager.init(applicationContext)

        setContent {
            val navController = rememberNavController()
            val authState by viewModel.authState.collectAsState()

            AppNavigation(navController, "loading")

            LaunchedEffect(authState) {
                when (authState) {
                    is AuthStateUi.Loading -> {}

                    is AuthStateUi.Authenticated -> {
                        navController.navigate("home") {
                            popUpTo("loading") { inclusive = true }
                        }
                    }

                    is AuthStateUi.NotAuthenticated -> {
                        navController.navigate("login") {
                            popUpTo("loading") { inclusive = true }
                            popUpTo("home") { inclusive = true }
                        }
                    }
                }
            }
        }
    }


    @Composable
    private fun AppNavigation(navController: NavHostController, startScreen: String) {

       /* val startScreen = if (SessionManager.isUserLoggedIn()) "home" else "login"*/

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = startScreen,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("loading") {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
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
                            navController.navigate("home") {
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
