package com.example.appbanco_s8.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appbanco_s8.data.model.Cuenta
import com.example.appbanco_s8.data.repository.CuentaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class DepositoUiState {
    object Idle : DepositoUiState()
    object Loading : DepositoUiState()
    data class Success(val monto: Double, val cuentaOrigen: String, val cuentaDestino: String) : DepositoUiState()
    data class Error(val message: String) : DepositoUiState()
}

class DepositoViewModel(
    private val repository: CuentaRepository = CuentaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<DepositoUiState>(DepositoUiState.Idle)
    val uiState: StateFlow<DepositoUiState> = _uiState.asStateFlow()

    private val _cuentas = MutableStateFlow<List<Cuenta>>(emptyList())
    val cuentas: StateFlow<List<Cuenta>> = _cuentas.asStateFlow()

    fun loadCuentas(token: String, userId: String) {
        viewModelScope.launch {
            _uiState.value = DepositoUiState.Loading
            repository.getCuentasByUser(token, userId).fold(
                onSuccess = {
                    _cuentas.value = it
                    _uiState.value = DepositoUiState.Idle
                },
                onFailure = {
                    _uiState.value = DepositoUiState.Error("Error al cargar cuentas: ${it.message}")
                }
            )
        }
    }

    fun realizarTransferencia(token: String, origen: Cuenta, destino: Cuenta, monto: Double) {
        if (origen.id == destino.id) {
            _uiState.value = DepositoUiState.Error("La cuenta origen y destino no pueden ser la misma")
            return
        }
        if (monto <= 0 || monto > 10000) {
            _uiState.value = DepositoUiState.Error("Monto inválido (debe ser > 0 y <= 10,000)")
            return
        }
        if (origen.saldo < monto) {
            _uiState.value = DepositoUiState.Error("Saldo insuficiente en la cuenta origen")
            return
        }

        viewModelScope.launch {
            _uiState.value = DepositoUiState.Loading
            val nuevoSaldoOrigen  = origen.saldo - monto
            val nuevoSaldoDestino = destino.saldo + monto
            
            repository.realizarTransferencia(
                token             = token,
                idOrigen          = origen.id,
                nuevoSaldoOrigen  = nuevoSaldoOrigen,
                idDestino         = destino.id,
                nuevoSaldoDestino = nuevoSaldoDestino,
                monto             = monto
            ).fold(
                onSuccess = {
                    _uiState.value = DepositoUiState.Success(monto, origen.numeroCuenta, destino.numeroCuenta)
                },
                onFailure = {
                    _uiState.value = DepositoUiState.Error("Error en transferencia: ${it.message}")
                }
            )
        }
    }

    fun resetState() {
        _uiState.value = DepositoUiState.Idle
    }
}
