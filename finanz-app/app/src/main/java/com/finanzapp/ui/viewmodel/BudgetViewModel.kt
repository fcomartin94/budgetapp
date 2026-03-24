package com.finanzapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.finanzapp.FinanzApp
import com.finanzapp.data.model.TipoTransaccion
import com.finanzapp.domain.BudgetService
import com.finanzapp.domain.ResumenMensual
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UiState(
    val transacciones: List<com.finanzapp.data.model.Transaccion> = emptyList(),
    val saldoTotal: Double = 0.0,
    val saldoMesActual: Double = 0.0,
    val resumenMensual: ResumenMensual? = null,
    val isLoading: Boolean = false
)

class BudgetViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as FinanzApp
    private val budgetService = BudgetService(app.transaccionRepository)

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        cargarDatos()
        observarTransacciones()
    }

    private fun observarTransacciones() {
        viewModelScope.launch {
            app.transaccionRepository.transacciones.collect { transacciones ->
                _uiState.update { it.copy(transacciones = transacciones) }
                actualizarCalculos()
            }
        }
    }

    private suspend fun actualizarCalculos() {
        val saldoTotal = budgetService.calcularSaldo()
        val saldoMesActual = budgetService.calcularSaldoMesActual()
        val resumenMensual = budgetService.obtenerResumenMensual()
        _uiState.update {
            it.copy(
                saldoTotal = saldoTotal,
                saldoMesActual = saldoMesActual,
                resumenMensual = resumenMensual
            )
        }
    }

    fun cargarDatos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            actualizarCalculos()
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun registrarTransaccion(
        descripcion: String,
        monto: Double,
        tipo: TipoTransaccion
    ) {
        viewModelScope.launch {
            budgetService.registrarTransaccion(
                descripcion = descripcion,
                monto = monto,
                tipo = tipo
            )
        }
    }

    fun eliminarTransaccion(id: Long) {
        viewModelScope.launch {
            budgetService.eliminarTransaccion(id)
        }
    }
}
