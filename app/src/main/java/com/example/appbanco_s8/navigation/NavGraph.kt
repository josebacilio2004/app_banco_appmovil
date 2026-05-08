package com.example.appbanco_s8.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.appbanco_s8.ui.components.AppScaffold
import com.example.appbanco_s8.ui.screens.*

@Composable
fun AppNavGraph(navController: NavHostController) {

    var tokenGlobal by remember { mutableStateOf("") }
    var emailGlobal by remember { mutableStateOf("") }
    var userIdGlobal by remember { mutableStateOf("") }

    val doLogout: () -> Unit = {
        tokenGlobal = ""
        emailGlobal = ""
        navController.navigate(Screen.Login.route) {
            popUpTo(0) { inclusive = true }
        }
    }

    NavHost(
        navController    = navController,
        startDestination = Screen.Login.route
    ) {

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { token, email, userId ->
                    tokenGlobal  = token
                    emailGlobal  = email
                    userIdGlobal = userId
                    navController.navigate(Screen.Home.createRoute(token, email)) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route     = Screen.Home.route,
            arguments = listOf(
                navArgument("token") { type = NavType.StringType },
                navArgument("email") { type = NavType.StringType }
            )
        ) { back ->
            val token = back.arguments?.getString("token") ?: tokenGlobal
            val email = back.arguments?.getString("email") ?: emailGlobal
            if (token.isNotEmpty()) {
                tokenGlobal = token
                emailGlobal = email
            }

            var openDrawer by remember { mutableStateOf<(() -> Unit)?>(null) }

            AppScaffold(
                token         = tokenGlobal,
                email         = emailGlobal,
                navController = navController,
                onLogout      = doLogout,
                onOpenDrawer  = { fn -> openDrawer = fn }
            ) { padding ->
                Box(Modifier.fillMaxSize().padding(padding)) {
                    HomeScreen(
                        token         = tokenGlobal,
                        email         = emailGlobal,
                        navController = navController,
                        onLogout      = doLogout,
                        onMenuClick   = { openDrawer?.invoke() }
                    )
                }
            }
        }

        composable(
            route = Screen.Cuenta.route,
            arguments = listOf(
                navArgument("token") { type = NavType.StringType },
                navArgument("accountId") { type = NavType.StringType },
                navArgument("saldo") { type = NavType.FloatType }
            )
        ) { back ->
            val tokenArg = back.arguments?.getString("token") ?: tokenGlobal
            val accountId = back.arguments?.getString("accountId") ?: ""
            val saldo = back.arguments?.getFloat("saldo")?.toDouble() ?: 0.0
            
            val dummyCuenta = com.example.appbanco_s8.data.model.Cuenta(id = accountId, saldo = saldo, tipo = "Cuenta")
            
            AppScaffold(
                token         = tokenGlobal,
                email         = emailGlobal,
                navController = navController,
                onLogout      = doLogout
            ) { padding ->
                Box(Modifier.fillMaxSize().padding(padding)) {
                    CuentaScreen(
                        token         = tokenArg,
                        cuenta        = dummyCuenta,
                        navController = navController
                    )
                }
            }
        }

        composable(
            route     = Screen.Tarjeta.route,
            arguments = listOf(navArgument("token") { type = NavType.StringType })
        ) { back ->
            val token = back.arguments?.getString("token") ?: tokenGlobal
            AppScaffold(
                token         = tokenGlobal,
                email         = emailGlobal,
                navController = navController,
                onLogout      = doLogout
            ) { padding ->
                Box(Modifier.fillMaxSize().padding(padding)) {
                    TarjetaScreen(token = token, navController = navController)
                }
            }
        }

        composable(
            route     = Screen.Prestamo.route,
            arguments = listOf(navArgument("token") { type = NavType.StringType })
        ) { back ->
            val token = back.arguments?.getString("token") ?: tokenGlobal
            AppScaffold(
                token         = tokenGlobal,
                email         = emailGlobal,
                navController = navController,
                onLogout      = doLogout
            ) { padding ->
                Box(Modifier.fillMaxSize().padding(padding)) {
                    PrestamoScreen(token = token, navController = navController)
                }
            }
        }

        composable(
            route     = Screen.Opera.route,
            arguments = listOf(navArgument("token") { type = NavType.StringType })
        ) { back ->
            val token = back.arguments?.getString("token") ?: tokenGlobal
            AppScaffold(
                token         = tokenGlobal,
                email         = emailGlobal,
                navController = navController,
                onLogout      = doLogout
            ) { padding ->
                Box(Modifier.fillMaxSize().padding(padding)) {
                    OperaScreen(token = token, userId = userIdGlobal, navController = navController)
                }
            }
        }

        composable(
            route     = Screen.Notifica.route,
            arguments = listOf(navArgument("token") { type = NavType.StringType })
        ) { back ->
            val token = back.arguments?.getString("token") ?: tokenGlobal
            AppScaffold(
                token         = tokenGlobal,
                email         = emailGlobal,
                navController = navController,
                onLogout      = doLogout
            ) { padding ->
                Box(Modifier.fillMaxSize().padding(padding)) {
                    NotificaScreen(token = token, navController = navController)
                }
            }
        }

        composable(Screen.Contacto.route) {
            AppScaffold(
                token         = tokenGlobal,
                email         = emailGlobal,
                navController = navController,
                onLogout      = doLogout
            ) { padding ->
                Box(Modifier.fillMaxSize().padding(padding)) {
                    ContactoScreen(navController = navController)
                }
            }
        }

        composable(
            route     = Screen.Perfil.route,
            arguments = listOf(navArgument("token") { type = NavType.StringType })
        ) { back ->
            val token = back.arguments?.getString("token") ?: tokenGlobal
            AppScaffold(
                token         = tokenGlobal,
                email         = emailGlobal,
                navController = navController,
                onLogout      = doLogout
            ) { padding ->
                Box(Modifier.fillMaxSize().padding(padding)) {
                    PerfilScreen(token = token, navController = navController)
                }
            }
        }

        composable(
            route     = Screen.Deposito.route,
            arguments = listOf(navArgument("token") { type = NavType.StringType })
        ) { back ->
            val token = back.arguments?.getString("token") ?: tokenGlobal
            AppScaffold(
                token         = tokenGlobal,
                email         = emailGlobal,
                navController = navController,
                onLogout      = doLogout
            ) { padding ->
                Box(Modifier.fillMaxSize().padding(padding)) {
                    DepositoScreen(
                        token = token,
                        userId = userIdGlobal,
                        navController = navController
                    )
                }
            }
        }

        composable(
            route = Screen.HistorialRecargas.route,
            arguments = listOf(
                navArgument("token") { type = NavType.StringType },
                navArgument("userId") { type = NavType.StringType }
            )
        ) { back ->
            val token = back.arguments?.getString("token") ?: tokenGlobal
            val userId = back.arguments?.getString("userId") ?: userIdGlobal
            AppScaffold(
                token = tokenGlobal,
                email = emailGlobal,
                navController = navController,
                onLogout = doLogout
            ) { padding ->
                Box(Modifier.fillMaxSize().padding(padding)) {
                    HistorialRecargasScreen(token = token, userId = userId, navController = navController)
                }
            }
        }

        composable(
            route = Screen.Recarga.route,
            arguments = listOf(
                navArgument("token") { type = NavType.StringType },
                navArgument("userId") { type = NavType.StringType },
                navArgument("operadora") { type = NavType.StringType },
                navArgument("celular") { type = NavType.StringType },
                navArgument("monto") { type = NavType.FloatType }
            )
        ) { back ->
            val token = back.arguments?.getString("token") ?: tokenGlobal
            val userId = back.arguments?.getString("userId") ?: userIdGlobal
            val operadora = back.arguments?.getString("operadora")?.trim() ?: ""
            val celular = back.arguments?.getString("celular")?.trim() ?: ""
            val monto = back.arguments?.getFloat("monto")?.toDouble() ?: 0.0

            AppScaffold(
                token = tokenGlobal,
                email = emailGlobal,
                navController = navController,
                onLogout = doLogout
            ) { padding ->
                Box(Modifier.fillMaxSize().padding(padding)) {
                    RecargaScreen(
                        token = token,
                        userId = userId,
                        navController = navController,
                        initialOperadora = operadora,
                        initialCelular = celular,
                        initialMonto = monto
                    )
                }
            }
        }
    }
}