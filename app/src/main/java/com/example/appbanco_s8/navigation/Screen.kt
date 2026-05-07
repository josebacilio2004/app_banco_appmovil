
package com.example.appbanco_s8.navigation

sealed class Screen(val route: String) {
    object Login     : Screen("login")
    object Home      : Screen("home/{token}/{email}") {
        fun createRoute(token: String, email: String) = "home/$token/$email"
    }
    object Cuenta    : Screen("cuenta/{token}/{accountId}/{saldo}") {
        fun createRoute(token: String, accountId: String, saldo: Double) = "cuenta/$token/$accountId/$saldo"
    }
    object Tarjeta   : Screen("tarjeta/{token}") {
        fun createRoute(token: String) = "tarjeta/$token"
    }
    object Prestamo  : Screen("prestamo/{token}") {
        fun createRoute(token: String) = "prestamo/$token"
    }
    object Opera     : Screen("opera/{token}") {
        fun createRoute(token: String) = "opera/$token"
    }
    object Notifica  : Screen("notifica/{token}") {
        fun createRoute(token: String) = "notifica/$token"
    }
    object Contacto  : Screen("contacto")
    object Perfil    : Screen("perfil/{token}") {
        fun createRoute(token: String) = "perfil/$token"
    }
    object Deposito  : Screen("deposito/{token}") {
        fun createRoute(token: String) = "deposito/$token"
    }
}