package com.example.appbanco_s8.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.appbanco_s8.data.model.Recarga
import com.example.appbanco_s8.navigation.Screen
import com.example.appbanco_s8.ui.theme.*
import com.example.appbanco_s8.ui.viewmodel.RecargaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialRecargasScreen(
    token: String,
    userId: String,
    navController: NavHostController,
    viewModel: RecargaViewModel = viewModel()
) {
    val listState = rememberLazyListState()
    val operadoras = listOf("Movistar", "Claro", "Entel", "Bitel")
    
    var recargaAEliminar by remember { mutableStateOf<Recarga?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Carga inicial
    LaunchedEffect(Unit) {
        viewModel.cargarRecargas(token, userId, reset = true)
    }

    // Detectar scroll al final para paginación
    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem != null && lastVisibleItem.index >= viewModel.recargas.size - 5
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && !viewModel.isLoading && !viewModel.isLastPage) {
            viewModel.cargarRecargas(token, userId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AzulMarino)
            .padding(16.dp)
    ) {
        Text(
            text = "Historial de Recargas",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Filtros Operadora
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            operadoras.forEach { op ->
                FilterChip(
                    selected = viewModel.selectedOperadora == op,
                    onClick = {
                        viewModel.selectedOperadora = if (viewModel.selectedOperadora == op) null else op
                        viewModel.cargarRecargas(token, userId, reset = true)
                    },
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

        // Totales por Mes
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = AzulMedio),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(Modifier.padding(12.dp)) {
                Text("Resumen Mensual", color = AzulBanco, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                viewModel.montosPorMes.value.forEach { (mes, total) ->
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(mes, color = Color.White, fontSize = 13.sp)
                        Text("S/ %,.2f".format(total), color = DoradoBanco, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Lista de Recargas
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(viewModel.recargas) { recarga ->
                RecargaItem(
                    recarga = recarga,
                    onRepeat = {
                        navController.navigate(
                            Screen.Recarga.createRoute(
                                token, userId, recarga.operadora, recarga.celular, recarga.monto
                            )
                        )
                    },
                    onDelete = {
                        recargaAEliminar = recarga
                        showDeleteDialog = true
                    }
                )
            }

            if (viewModel.isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AzulBanco)
                    }
                }
            }
        }
    }

    // Dialogo de Eliminación
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = AzulMedio,
            title = { Text("Eliminar Registro", color = Color.White) },
            text = { Text("¿Estás seguro de que deseas eliminar este registro del historial?", color = GrisTexto) },
            confirmButton = {
                TextButton(onClick = {
                    recargaAEliminar?.let {
                        viewModel.eliminarRecarga(token, it.id) { exito ->
                            if (exito) showDeleteDialog = false
                        }
                    }
                }) {
                    Text("Eliminar", color = RojoError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar", color = Color.White)
                }
            }
        )
    }
}

@Composable
fun RecargaItem(
    recarga: Recarga,
    onRepeat: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GrisSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(recarga.operadora, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(recarga.celularEnmascarado(), color = GrisTexto, fontSize = 14.sp)
                Text(recarga.fecha.take(10), color = GrisTexto, fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(recarga.montoFormateado(), color = DoradoBanco, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Row {
                    IconButton(onClick = onRepeat) {
                        Icon(Icons.Default.Refresh, "Repetir", tint = VerdeExito)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, "Eliminar", tint = RojoError)
                    }
                }
            }
        }
    }
}
