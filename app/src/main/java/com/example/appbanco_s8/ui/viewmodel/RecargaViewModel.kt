package com.example.appbanco_s8.ui.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appbanco_s8.data.model.Recarga
import com.example.appbanco_s8.data.model.RecargaRequest
import com.example.appbanco_s8.data.repository.RecargaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RecargaViewModel : ViewModel() {
    private val repository = RecargaRepository()

    // Usamos mutableStateListOf para facilitar la paginación y el derivedStateOf
    private val _recargas = mutableStateListOf<Recarga>()
    val recargas: List<Recarga> get() = _recargas

    private var currentOffset = 0
    private val limit = 20
    
    var isLoading by mutableStateOf(false)
    var isLastPage by mutableStateOf(false)
    var selectedOperadora by mutableStateOf<String?>(null)

    // derivedStateOf para calcular montos por mes
    val montosPorMes = derivedStateOf {
        _recargas.groupBy { it.obtenerMesAnio() }
            .mapValues { entry -> entry.value.sumOf { it.monto } }
    }

    fun cargarRecargas(token: String, userId: String, reset: Boolean = false) {
        if (isLoading || (isLastPage && !reset)) return

        if (reset) {
            _recargas.clear()
            currentOffset = 0
            isLastPage = false
        }

        viewModelScope.launch {
            isLoading = true
            val nuevas = repository.getRecargas(
                token = token,
                userId = userId,
                limit = limit,
                offset = currentOffset,
                operadora = selectedOperadora
            )
            
            if (nuevas.isEmpty()) {
                isLastPage = true
            } else {
                _recargas.addAll(nuevas)
                currentOffset += limit
            }
            isLoading = false
        }
    }

    fun eliminarRecarga(token: String, recargaId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val exito = repository.deleteRecarga(token, recargaId)
            if (exito) {
                _recargas.removeAll { it.id == recargaId }
            }
            onResult(exito)
        }
    }

    fun realizarRecarga(token: String, request: RecargaRequest, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val exito = repository.createRecarga(token, request)
            onResult(exito)
        }
    }
}
