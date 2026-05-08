package com.example.appbanco_s8.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.appbanco_s8.data.model.RecargaRequest
import com.example.appbanco_s8.ui.theme.*
import com.example.appbanco_s8.ui.viewmodel.RecargaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecargaScreen(
    token: String,
    userId: String,
    navController: NavHostController,
    initialOperadora: String = "",
    initialCelular: String = "",
    initialMonto: Double = 0.0,
    viewModel: RecargaViewModel = viewModel()
) {
    val context = LocalContext.current
    var operadora by remember { mutableStateOf(if (initialOperadora.isNotBlank()) initialOperadora else "Movistar") }
    var celular by remember { mutableStateOf(if (initialCelular.isNotBlank()) initialCelular else "") }
    var monto by remember { mutableStateOf(if (initialMonto > 0) initialMonto.toString() else "") }
    
    val operadoras = listOf("Movistar", "Claro", "Entel", "Bitel")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AzulMarino)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White)
            }
            Text(
                text = "Nueva Recarga",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(24.dp))

        Text("Selecciona tu operadora", color = GrisTexto, fontSize = 14.sp)
        Row(
            Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            operadoras.forEach { op ->
                FilterChip(
                    selected = operadora == op,
                    onClick = { operadora = op },
                    label = { Text(op) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = GrisSurface,
                        labelColor = GrisTexto,
                        selectedContainerColor = AzulBanco,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = celular,
            onValueChange = { if (it.length <= 9) celular = it },
            label = { Text("Número de celular") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AzulBanco,
                unfocusedBorderColor = GrisBorde,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = AzulBanco,
                unfocusedLabelColor = GrisTexto
            )
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = monto,
            onValueChange = { monto = it },
            label = { Text("Monto a recargar") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            prefix = { Text("S/ ", color = Color.White) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AzulBanco,
                unfocusedBorderColor = GrisBorde,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = AzulBanco,
                unfocusedLabelColor = GrisTexto
            )
        )

        Spacer(Modifier.weight(1f))

        Button(
            onClick = {
                val m = monto.toDoubleOrNull() ?: 0.0
                if (celular.length < 9) {
                    Toast.makeText(context, "Celular inválido", Toast.LENGTH_SHORT).show()
                } else if (m <= 0) {
                    Toast.makeText(context, "Monto inválido", Toast.LENGTH_SHORT).show()
                } else {
                    val request = RecargaRequest(userId, operadora, celular, m)
                    viewModel.realizarRecarga(token, request) { exito ->
                        if (exito) {
                            Toast.makeText(context, "Recarga exitosa", Toast.LENGTH_LONG).show()
                            navController.popBackStack()
                        } else {
                            Toast.makeText(context, "Error al procesar recarga", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AzulBanco),
            shape = RoundedCornerShape(12.dp),
            enabled = !viewModel.isLoading
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Confirmar Recarga", fontWeight = FontWeight.Bold)
            }
        }
    }
}
