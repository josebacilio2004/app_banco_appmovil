package com.example.appbanco_s8.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.appbanco_s8.navigation.Screen
import kotlinx.coroutines.launch

private val AzulMarino  = Color(0xFF020B18)
private val AzulMedio   = Color(0xFF041B3B)
private val AzulBanco   = Color(0xFF1A5DC8)
private val AzulClaro   = Color(0xFF1E3A6E)
private val DoradoBanco = Color(0xFFF5C842)
private val GrisTexto   = Color(0xFFB0B8C8)
private val GrisSurface = Color(0xFF0D1F3C)

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String,
    val isCentral: Boolean = false
)

data class DrawerItem(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val isDestructive: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    token: String,
    email: String,
    navController: NavHostController,
    onLogout: () -> Unit,
    onOpenDrawer: ((openFn: () -> Unit) -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope       = rememberCoroutineScope()

    // ── CORRECCIÓN: pasar la lambda al caller usando SideEffect ──
    // SideEffect se ejecuta después de cada recomposición exitosa.
    // No es suspend, así que scope.launch sí funciona aquí.
    SideEffect {
        onOpenDrawer?.invoke {
            scope.launch { drawerState.open() }
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route?.substringBefore("/") ?: ""

    val nombreCorto = email.substringBefore("@").replaceFirstChar { it.uppercase() }
    val iniciales   = nombreCorto.take(2).uppercase()

    val bottomItems = listOf(
        BottomNavItem("Inicio",   Icons.Filled.Home,          Screen.Home.route.substringBefore("/")),
        BottomNavItem("Opera",    Icons.Filled.SwapHoriz,     Screen.Opera.route.substringBefore("/")),
        BottomNavItem("Para mí",  Icons.Filled.Add,           Screen.Cuenta.route.substringBefore("/"), isCentral = true),
        BottomNavItem("Notifica", Icons.Filled.Notifications, Screen.Notifica.route.substringBefore("/")),
        BottomNavItem("Contacto", Icons.Filled.HeadsetMic,    Screen.Contacto.route)
    )

    val drawerItems = listOf(
        DrawerItem("Configuración",          Icons.Outlined.Settings,       {}),
        DrawerItem("Depósito",               Icons.Outlined.AccountBalanceWallet, {
            navController.navigate(Screen.Deposito.createRoute(token))
        }),
        DrawerItem("Token Digital",          Icons.Outlined.Shield,         {}),
        DrawerItem("Seguridad y privacidad", Icons.Outlined.Lock,           {}),
        DrawerItem("Operativas",             Icons.Outlined.AccountBalance, {}),
        DrawerItem("Operar con QR / Plin",   Icons.Outlined.QrCode,         {}),
        DrawerItem("Puntos y promociones",   Icons.Outlined.Stars,          {}),
        DrawerItem("Experiencias",           Icons.Outlined.Explore,        {}),
        DrawerItem("Historial retiro",       Icons.Outlined.History,        {}),
        DrawerItem("Puntos de atención",     Icons.Outlined.LocationOn,     {}),
        DrawerItem("Zona de cobro",          Icons.Outlined.Calculate,      {}),
        DrawerItem("Aplicaciones",           Icons.Outlined.Apps,           {}),
        DrawerItem("Ayúdanos a mejorar",     Icons.Outlined.Favorite,       {}),
        DrawerItem("Acerca de Mi Banco",     Icons.Outlined.Info,           {}),
        DrawerItem("Salir",                  Icons.Outlined.Logout,         onLogout, isDestructive = true)
    )

    ModalNavigationDrawer(
        drawerState   = drawerState,
        drawerContent = {
            DrawerContent(
                nombreCorto = nombreCorto,
                iniciales   = iniciales,
                items       = drawerItems,
                onClose     = { scope.launch { drawerState.close() } }
            )
        },
        scrimColor = Color.Black.copy(alpha = 0.6f)
    ) {
        Scaffold(
            containerColor = AzulMarino,
            bottomBar = {
                BottomNavBar(
                    items        = bottomItems,
                    currentRoute = currentRoute,
                    onItemClick  = { item ->
                        if (!item.isCentral) {
                            val route = when (item.label) {
                                "Inicio"   -> Screen.Home.createRoute(token, email)
                                "Opera"    -> Screen.Opera.createRoute(token)
                                "Notifica" -> Screen.Notifica.createRoute(token)
                                "Contacto" -> Screen.Contacto.route
                                else       -> null
                            }
                            route?.let {
                                navController.navigate(it) {
                                    popUpTo(Screen.Home.route.substringBefore("/")) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState    = true
                                }
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->
            content(paddingValues)
        }
    }
}

@Composable
private fun BottomNavBar(
    items: List<BottomNavItem>,
    currentRoute: String,
    onItemClick: (BottomNavItem) -> Unit
) {
    NavigationBar(
        containerColor = AzulMedio,
        tonalElevation = 0.dp,
        modifier       = Modifier.height(64.dp)
    ) {
        items.forEach { item ->
            val selected = currentRoute.startsWith(item.route.substringBefore("/"))
            if (item.isCentral) {
                NavigationBarItem(
                    selected = false,
                    onClick  = { onItemClick(item) },
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .background(AzulBanco, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector        = item.icon,
                                contentDescription = item.label,
                                tint               = Color.White,
                                modifier           = Modifier.size(24.dp)
                            )
                        }
                    },
                    label  = { Text(item.label, fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = GrisTexto,
                        unselectedTextColor = GrisTexto,
                        indicatorColor      = Color.Transparent
                    )
                )
            } else {
                NavigationBarItem(
                    selected = selected,
                    onClick  = { onItemClick(item) },
                    icon = {
                        Icon(
                            imageVector        = item.icon,
                            contentDescription = item.label,
                            modifier           = Modifier.size(22.dp)
                        )
                    },
                    label  = { Text(item.label, fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor   = AzulBanco,
                        selectedTextColor   = AzulBanco,
                        unselectedIconColor = GrisTexto,
                        unselectedTextColor = GrisTexto,
                        indicatorColor      = AzulBanco.copy(alpha = 0.12f)
                    )
                )
            }
        }
    }
}

@Composable
private fun DrawerContent(
    nombreCorto: String,
    iniciales: String,
    items: List<DrawerItem>,
    onClose: () -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = AzulMedio,
        modifier             = Modifier.width(300.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AzulBanco.copy(alpha = 0.2f))
                .padding(24.dp)
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(AzulBanco, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = iniciales,
                        color      = Color.White,
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text       = nombreCorto,
                    color      = Color.White,
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text     = "Puntos Mi Banco: 0",
                    color    = GrisTexto,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(4.dp))
                TextButton(
                    onClick  = { onClose() },
                    modifier = Modifier.padding(0.dp)
                ) {
                    Text(
                        text     = "Perfil",
                        color    = AzulBanco,
                        fontSize = 13.sp
                    )
                }
            }
        }

        HorizontalDivider(color = AzulClaro, thickness = 0.5.dp)

        items.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        item.onClick()
                        if (!item.isDestructive) onClose()
                    }
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector        = item.icon,
                    contentDescription = item.label,
                    tint               = if (item.isDestructive) DoradoBanco else AzulBanco,
                    modifier           = Modifier.size(20.dp)
                )
                Text(
                    text       = item.label,
                    color      = if (item.isDestructive) DoradoBanco else Color.White,
                    fontSize   = 14.sp,
                    fontWeight = if (item.isDestructive) FontWeight.Bold else FontWeight.Normal
                )
            }

            if (item.label == "Experiencias" || item.label == "Acerca de Mi Banco") {
                HorizontalDivider(
                    color     = AzulClaro.copy(alpha = 0.4f),
                    thickness = 0.5.dp,
                    modifier  = Modifier.padding(horizontal = 20.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}