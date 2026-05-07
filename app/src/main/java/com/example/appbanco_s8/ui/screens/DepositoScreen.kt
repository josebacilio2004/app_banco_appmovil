package com.example.appbanco_s8.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.appbanco_s8.data.model.Cuenta
import com.example.appbanco_s8.ui.viewmodel.DepositoUiState
import com.example.appbanco_s8.ui.viewmodel.DepositoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepositoScreen(
    token: String,
    userId: String,
    navController: NavController,
    viewModel: DepositoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cuentas by viewModel.cuentas.collectAsStateWithLifecycle()

    var monto           by remember { mutableStateOf("") }
    var cuentaOrigen    by remember { mutableStateOf<Cuenta?>(null) }
    var cuentaDestino   by remember { mutableStateOf<Cuenta?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadCuentas(token, userId)
    }

    LaunchedEffect(uiState) {
        if (uiState is DepositoUiState.Success) {
            val s = uiState as DepositoUiState.Success
            snackbarHostState.showSnackbar("Transferencia exitosa: S/ ${s.monto} de ${s.cuentaOrigen} a ${s.cuentaDestino}")
            monto = ""
            viewModel.resetState()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Transferencia entre Cuentas", color = Color.White) },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = null,
                modifier = Modifier.size(80.dp).padding(16.dp),
                tint = Color(0xFF1A5DC8)
            )

            // ── Monto ──────────────────────────────────────────
            OutlinedTextField(
                value = monto,
                onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) monto = it },
                label = { Text("Monto a transferir", color = Color(0xFFB0B8C8)) },
                prefix = { Text("S/ ", color = Color.White) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF1A5DC8),
                    unfocusedBorderColor = Color(0xFF1C2E4A)
                )
            )

            Spacer(Modifier.height(16.dp))

            // ── Selector Cuenta Origen ─────────────────────────
            AccountSelector(
                label   = "Cuenta Origen",
                cuentas = cuentas,
                selectedCuenta = cuentaOrigen,
                onSelected = { cuentaOrigen = it }
            )

            Spacer(Modifier.height(16.dp))

            // ── Selector Cuenta Destino ────────────────────────
            AccountSelector(
                label   = "Cuenta Destino",
                cuentas = cuentas,
                selectedCuenta = cuentaDestino,
                onSelected = { cuentaDestino = it }
            )

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = { showConfirmDialog = true },
                enabled = monto.isNotEmpty() && cuentaOrigen != null && cuentaDestino != null && uiState !is DepositoUiState.Loading,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A5DC8))
            ) {
                if (uiState is DepositoUiState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Confirmar Transferencia", fontWeight = FontWeight.Bold)
                }
            }

            if (uiState is DepositoUiState.Error) {
                Text(
                    text = (uiState as DepositoUiState.Error).message,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title   = { Text("Confirmar Operación") },
            text    = { Text("¿Deseas transferir S/ $monto de la cuenta ${cuentaOrigen?.numeroCuenta} a la cuenta ${cuentaDestino?.numeroCuenta}?") },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    val m = monto.toDoubleOrNull() ?: 0.0
                    viewModel.realizarTransferencia(token, cuentaOrigen!!, cuentaDestino!!, m)
                }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSelector(
    label: String,
    cuentas: List<Cuenta>,
    selectedCuenta: Cuenta?,
    onSelected: (Cuenta) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedCuenta?.let { "${it.tipo} - ${it.numeroCuenta}" } ?: "Seleccionar cuenta",
            onValueChange = {},
            readOnly = true,
            label = { Text(label, color = Color(0xFFB0B8C8)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF1A5DC8),
                unfocusedBorderColor = Color(0xFF1C2E4A)
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color(0xFF041B3B))
        ) {
            cuentas.forEach { cuenta ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text("${cuenta.tipo} - ${cuenta.numeroCuenta}", color = Color.White)
                            Text("Saldo: S/ ${cuenta.saldo}", color = Color(0xFFB0B8C8), fontSize = 12.sp)
                        }
                    },
                    onClick = {
                        onSelected(cuenta)
                        expanded = false
                    }
                )
            }
        }
    }
}
