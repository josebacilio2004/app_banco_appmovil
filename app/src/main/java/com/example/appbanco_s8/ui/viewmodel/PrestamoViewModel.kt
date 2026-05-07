
package com.example.appbanco_s8.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appbanco_s8.data.model.Prestamo
import com.example.appbanco_s8.data.repository.PrestamoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PrestamoViewModel : ViewModel() {
    private val repository = PrestamoRepository()

    private val _prestamos = MutableStateFlow<DataUiState<List<Prestamo>>>(DataUiState.Loading)
    val prestamos: StateFlow<DataUiState<List<Prestamo>>> = _prestamos

    fun cargarPrestamos(token: String) {
        viewModelScope.launch {
            _prestamos.value = DataUiState.Loading
            val result = repository.getPrestamos(token)
            _prestamos.value = if (result.isSuccess)
                DataUiState.Success(result.getOrNull()!!)
            else DataUiState.Error(result.exceptionOrNull()?.message ?: "Error")
        }
    }
}