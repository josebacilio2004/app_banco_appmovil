package com.example.appbanco_s8.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appbanco_s8.data.model.Transaccion
import com.example.appbanco_s8.data.repository.CuentaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CuentaUiState {
    object Loading : CuentaUiState()
    data class Success(val transacciones: List<Transaccion>) : CuentaUiState()
    data class Error(val message: String) : CuentaUiState()
}

class CuentaViewModel(
    private val repository: CuentaRepository = CuentaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<CuentaUiState>(CuentaUiState.Loading)
    val uiState: StateFlow<CuentaUiState> = _uiState.asStateFlow()

    fun loadTransacciones(token: String, cuentaId: String) {
        viewModelScope.launch {
            _uiState.value = CuentaUiState.Loading
            repository.getTransaccionesByCuenta(token, cuentaId).fold(
                onSuccess = {
                    _uiState.value = CuentaUiState.Success(it)
                },
                onFailure = {
                    _uiState.value = CuentaUiState.Error(it.message ?: "Error desconocido")
                }
            )
        }
    }
}