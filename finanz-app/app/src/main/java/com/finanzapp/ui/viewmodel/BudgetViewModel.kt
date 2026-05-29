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

/**
 * Immutable UI state snapshot consumed by all Compose screens.
 *
 * @property transacciones the full transaction list for the current session
 * @property saldoTotal     all-time net balance (income − expenses)
 * @property saldoMesActual net balance for the current calendar month
 * @property resumenMensual aggregated monthly figures (income, expenses, ratio)
 * @property isLoading      true while an async operation is in progress
 */
data class UiState(
    val transacciones: List<com.finanzapp.data.model.Transaccion> = emptyList(),
    val saldoTotal: Double = 0.0,
    val saldoMesActual: Double = 0.0,
    val resumenMensual: ResumenMensual? = null,
    val isLoading: Boolean = false
)

/**
 * ViewModel for the FinanzApp budget screens.
 *
 * Exposes a single [uiState] [StateFlow] that Compose screens collect.
 * All writes go through [BudgetService] and any Room change triggers a
 * reactive recalculation of balances.
 *
 * ## Data flow
 * ```
 * Room (Flow) → TransaccionRepository → BudgetService → UiState → Compose UI
 * ```
 *
 * ## Lifecycle
 * The ViewModel survives configuration changes. On init it:
 * 1. Triggers an initial [cargarDatos] call to populate the state immediately.
 * 2. Starts [observarTransacciones] to keep state in sync with every Room write.
 */
class BudgetViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as FinanzApp
    private val budgetService = BudgetService(app.transaccionRepository)

    private val _uiState = MutableStateFlow(UiState())

    /** Read-only view of the current UI state. Collected by Compose screens. */
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        cargarDatos()
        observarTransacciones()
    }

    /**
     * Collects the [TransaccionRepository.transacciones] Flow and recalculates
     * all financial figures on every emission (i.e. every database change).
     */
    private fun observarTransacciones() {
        viewModelScope.launch {
            app.transaccionRepository.transacciones.collect { transacciones ->
                _uiState.update { it.copy(transacciones = transacciones) }
                actualizarCalculos()
            }
        }
    }

    /**
     * Recomputes [UiState.saldoTotal], [UiState.saldoMesActual], and
     * [UiState.resumenMensual] by delegating to [BudgetService].
     */
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

    /**
     * Triggers a full reload of financial figures with a loading indicator.
     *
     * Called on init and can be invoked from the UI to force a refresh.
     */
    fun cargarDatos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            actualizarCalculos()
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    /**
     * Records a new transaction and triggers a reactive UI update via Room.
     *
     * @param descripcion a short description of the movement
     * @param monto       the absolute amount (positive; direction is set by [tipo])
     * @param tipo        [TipoTransaccion.INGRESO] for income or [TipoTransaccion.GASTO] for expense
     */
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

    /**
     * Deletes the transaction with the given ID and triggers a reactive UI update.
     *
     * @param id the primary key of the transaction to remove
     */
    fun eliminarTransaccion(id: Long) {
        viewModelScope.launch {
            budgetService.eliminarTransaccion(id)
        }
    }
}
