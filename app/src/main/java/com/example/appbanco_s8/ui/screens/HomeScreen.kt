package com.example.appbanco_s8.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.appbanco_s8.data.model.Cuenta
import com.example.appbanco_s8.data.model.Transaccion
import com.example.appbanco_s8.navigation.Screen
import com.example.appbanco_s8.ui.viewmodel.DataUiState
import com.example.appbanco_s8.ui.viewmodel.HomeViewModel

// ── Paleta ──────────────────────────────────────────────────
private val AzulMarino  = Color(0xFF020B18)
private val AzulMedio   = Color(0xFF041B3B)
private val AzulBanco   = Color(0xFF1A5DC8)
private val AzulClaro   = Color(0xFF1E3A6E)
private val DoradoBanco = Color(0xFFF5C842)
private val GrisTexto   = Color(0xFFB0B8C8)
private val GrisSurface = Color(0xFF0D1F3C)
private val VerdeExito  = Color(0xFF2ECC71)
private val RojoError   = Color(0xFFE74C3C)

// ── Pantalla principal ───────────────────────────────────────
@Composable
fun HomeScreen(
    token: String,
    email: String,
    navController: NavHostController,
    onLogout: () -> Unit,
    onMenuClick: () -> Unit = {},           // ← NUEVO: abre el drawer lateral
    viewModel: HomeViewModel = viewModel()
) {
    val cuentasState       by viewModel.cuentas.collectAsStateWithLifecycle()
    val transaccionesState by viewModel.transacciones.collectAsStateWithLifecycle()

    LaunchedEffect(token) {
        viewModel.cargarDatos(token)
    }

    val nombreCorto = email.substringBefore("@")
        .replaceFirstChar { it.uppercase() }

    // ── Sin Scaffold propio — AppScaffold ya lo provee ───────
    LazyColumn(
        modifier            = Modifier
            .fillMaxSize()
            .background(AzulMarino),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {

        // ── 1. Header ────────────────────────────────────────
        item {
            HeaderSection(
                nombreCorto = nombreCorto,
                onMenuClick = onMenuClick   // ← pasa al header
            )
        }

        // ── 2. Resumen ingresos / gastos ─────────────────────
        item {
            when (val state = transaccionesState) {
                is DataUiState.Success -> ResumenSection(
                    transacciones = state.data,
                    ingresos      = viewModel.ingresosMes,
                    gastos        = viewModel.gastosMes
                )
                is DataUiState.Loading -> LoadingCard()
                is DataUiState.Error   -> Unit
            }
        }

        // ── 3. Accesos rápidos ───────────────────────────────
        item {
            AccesosRapidosSection()
        }

        // ── 4. Cuentas ───────────────────────────────────────
        item {
            SectionTitle(title = "Cuentas")
        }

        when (val state = cuentasState) {
            is DataUiState.Loading -> item { LoadingCard() }
            is DataUiState.Error   -> item { ErrorCard(mensaje = state.mensaje) }
            is DataUiState.Success -> {
                items(state.data) { cuenta ->
                    CuentaCard(
                        cuenta  = cuenta,
                        onClick = {
                            navController.navigate(Screen.Cuenta.createRoute(token, cuenta.id, cuenta.saldo))
                        }
                    )
                }
            }
        }

        // ── 5. Últimos movimientos ───────────────────────────
        item {
            SectionTitle(title = "Últimos movimientos")
        }

        when (val state = transaccionesState) {
            is DataUiState.Loading -> item { LoadingCard() }
            is DataUiState.Error   -> item { ErrorCard(mensaje = state.mensaje) }
            is DataUiState.Success -> {
                val ultimos = state.data.take(5)
                if (ultimos.isEmpty()) {
                    item {
                        Text(
                            text     = "Sin movimientos recientes",
                            color    = GrisTexto,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(
                                horizontal = 16.dp,
                                vertical   = 8.dp
                            )
                        )
                    }
                } else {
                    items(ultimos) { tx ->
                        MovimientoRow(transaccion = tx)
                    }
                }
            }
        }

        // ── 6. Análisis de gasto ─────────────────────────────
        item {
            AnalisisGastoCard()
        }

        item {
            Spacer(Modifier.height(16.dp))
        }
    }
}

// ── Header ───────────────────────────────────────────────────
// CAMBIO: recibe onMenuClick en vez de onLogout
@Composable
private fun HeaderSection(
    nombreCorto: String,
    onMenuClick: () -> Unit                 // ← abre el drawer
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(AzulBanco.copy(alpha = 0.3f), Color.Transparent)
                )
            )
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            // Saludo
            Column {
                Text(
                    text       = "Hola, $nombreCorto",
                    color      = Color.White,
                    fontSize   = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text     = "Bienvenido a Mi Banco",
                    color    = GrisTexto,
                    fontSize = 13.sp
                )
            }

            // Botones Ayuda + Menú
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { /* TODO: ayuda */ }) {
                    Icon(
                        imageVector        = Icons.Outlined.HelpOutline,
                        contentDescription = "Ayuda",
                        tint               = Color.White
                    )
                }
                // CAMBIO: antes llamaba onLogout, ahora abre el drawer
                IconButton(onClick = onMenuClick) {
                    Icon(
                        imageVector        = Icons.Default.Menu,
                        contentDescription = "Menú",
                        tint               = Color.White
                    )
                }
            }
        }
    }
}

// ── Resumen ingresos / gastos ─────────────────────────────────
@Composable
private fun ResumenSection(
    transacciones: List<Transaccion>,
    ingresos: Double,
    gastos: Double
) {
    val total    = ingresos + gastos
    val progreso = if (total > 0) (ingresos / total).toFloat().coerceIn(0f, 1f) else 0.5f

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = GrisSurface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Ingresos
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Ingresos", color = GrisTexto, fontSize = 13.sp)
                Text(
                    text       = "S/ %,.2f".format(ingresos),
                    color      = Color.White,
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress   = { progreso },
                modifier   = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color      = AzulBanco,
                trackColor = AzulMedio
            )

            Spacer(Modifier.height(12.dp))

            // Gastos
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Gastos", color = GrisTexto, fontSize = 13.sp)
                Text(
                    text       = "S/ %,.2f".format(gastos),
                    color      = Color.White,
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress   = { (1f - progreso) },
                modifier   = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color      = AzulClaro,
                trackColor = AzulMedio
            )

            Spacer(Modifier.height(12.dp))
            TextButton(
                onClick  = { },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Ver más", color = AzulBanco, fontSize = 13.sp)
            }
        }
    }
}

// ── Accesos rápidos ───────────────────────────────────────────
@Composable
private fun AccesosRapidosSection() {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        AccesoItem(icon = Icons.Default.SwapHoriz,       label = "Transferir")
        AccesoItem(icon = Icons.Default.PhoneAndroid,    label = "PLIN")
        AccesoItem(icon = Icons.Default.CurrencyExchange, label = "T-Cambio")
        AccesoItem(icon = Icons.Default.MoreHoriz,       label = "Más")
    }
}

@Composable
private fun AccesoItem(icon: ImageVector, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier            = Modifier.clickable { }
    ) {
        Box(
            modifier        = Modifier
                .size(52.dp)
                .background(GrisSurface, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = label,
                tint               = Color.White,
                modifier           = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(label, color = GrisTexto, fontSize = 11.sp)
    }
}

// ── Título de sección ─────────────────────────────────────────
@Composable
private fun SectionTitle(title: String) {
    Text(
        text       = title,
        color      = Color.White,
        fontSize   = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier   = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

// ── Card de cuenta ────────────────────────────────────────────
@Composable
private fun CuentaCard(cuenta: Cuenta, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = GrisSurface
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text       = cuenta.tipo.replaceFirstChar { it.uppercase() },
                    color      = Color.White,
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text     = "• ${cuenta.numeroCuenta.takeLast(4)}",
                    color    = GrisTexto,
                    fontSize = 13.sp
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text       = "S/ %,.2f".format(cuenta.saldo),
                    color      = Color.White,
                    fontSize   = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text     = "Saldo disponible",
                    color    = GrisTexto,
                    fontSize = 11.sp
                )
            }
        }
    }
}

// ── Fila de movimiento ────────────────────────────────────────
@Composable
private fun MovimientoRow(transaccion: Transaccion) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    if (transaccion.esDebito()) RojoError.copy(alpha = 0.15f)
                    else VerdeExito.copy(alpha = 0.15f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (transaccion.esDebito())
                    Icons.Default.ArrowUpward
                else
                    Icons.Default.ArrowDownward,
                contentDescription = null,
                tint               = if (transaccion.esDebito()) RojoError else VerdeExito,
                modifier           = Modifier.size(20.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text     = transaccion.descripcion,
                color    = Color.White,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text     = transaccion.fecha.take(10),
                color    = GrisTexto,
                fontSize = 12.sp
            )
        }

        Text(
            text       = "${if (transaccion.esDebito()) "-" else "+"}${transaccion.montoFormateado()}",
            color      = if (transaccion.esDebito()) RojoError else VerdeExito,
            fontSize   = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }

    HorizontalDivider(
        modifier  = Modifier.padding(horizontal = 16.dp),
        color     = AzulMedio,
        thickness = 0.5.dp
    )
}

// ── Análisis de gasto ─────────────────────────────────────────
@Composable
private fun AnalisisGastoCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        color = GrisSurface
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector        = Icons.Default.BarChart,
                    contentDescription = null,
                    tint               = AzulBanco,
                    modifier           = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text       = "Análisis de Gasto",
                    color      = Color.White,
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Icon(
                imageVector        = Icons.Default.ChevronRight,
                contentDescription = null,
                tint               = GrisTexto
            )
        }
    }
}

// ── Componentes auxiliares ────────────────────────────────────
@Composable
private fun LoadingCard() {
    Box(
        modifier        = Modifier
            .fillMaxWidth()
            .height(80.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color       = AzulBanco,
            strokeWidth = 2.dp,
            modifier    = Modifier.size(28.dp)
        )
    }
}

@Composable
private fun ErrorCard(mensaje: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = RojoError.copy(alpha = 0.12f)
    ) {
        Row(
            modifier          = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector        = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint               = RojoError,
                modifier           = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text     = mensaje,
                color    = RojoError,
                fontSize = 13.sp
            )
        }
    }
}

