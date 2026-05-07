package com.example.appbanco_s8.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.appbanco_s8.data.model.Cuenta
import com.example.appbanco_s8.data.model.Transaccion
import com.example.appbanco_s8.ui.viewmodel.CuentaUiState
import com.example.appbanco_s8.ui.viewmodel.CuentaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CuentaScreen(
    token: String,
    cuenta: Cuenta,
    navController: NavController,
    viewModel: CuentaViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(cuenta.id) {
        viewModel.loadTransacciones(token, cuenta.id)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${cuenta.tipo} - ${cuenta.numeroCuenta.takeLast(4)}", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF020B18))
            )
        },
        containerColor = Color(0xFF020B18)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header con saldo
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF041B3B)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Saldo disponible", color = Color(0xFFB0B8C8), fontSize = 14.sp)
                    Text(
                        text = "S/ ${cuenta.saldo}",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            Text(
                "Movimientos",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 16.dp)
            )

            when (val state = uiState) {
                is CuentaUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF1A5DC8))
                    }
                }
                is CuentaUiState.Success -> {
                    if (state.transacciones.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No hay movimientos registrados", color = Color(0xFFB0B8C8))
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(state.transacciones) { tx ->
                                TransaccionItem(tx)
                                Divider(color = Color(0xFF1C2E4A), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                            }
                        }
                    }
                }
                is CuentaUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: ${state.message}", color = Color.Red)
                    }
                }
            }
        }
    }
}

@Composable
fun TransaccionItem(tx: Transaccion) {
    val isIngreso = tx.tipo.contains("RECIBIDA") || tx.tipo.contains("DEPOSITO")
    val color = if (isIngreso) Color(0xFF2ECC71) else Color(0xFFE74C3C)
    val icon = if (isIngreso) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Text(tx.tipo, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Text(tx.fecha.take(10), color = Color(0xFFB0B8C8), fontSize = 12.sp)
        }

        Text(
            text = "${if (isIngreso) "+" else "-"} S/ ${tx.monto}",
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}
