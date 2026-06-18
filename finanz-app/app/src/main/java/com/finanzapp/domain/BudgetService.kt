package com.finanzapp.domain

import com.finanzapp.data.local.TransaccionRepository
import com.finanzapp.data.model.Categoria
import com.finanzapp.data.model.TipoTransaccion
import com.finanzapp.data.model.Transaccion
import java.time.LocalDate

/**
 * Business logic for the FinanzApp budget domain.
 *
 * Sits between [com.finanzapp.ui.viewmodel.BudgetViewModel] and [TransaccionRepository],
 * keeping financial calculations out of the ViewModel. All methods are `suspend` and
 * must be called from a coroutine scope.
 *
 * This service is the Kotlin/Android counterpart of `finanz-core`'s `BudgetService`
 * and `finanz-api`'s `BudgetService` — same domain logic, different delivery mechanism.
 */
class BudgetService(private val repositorio: TransaccionRepository) {

    /**
     * Records a new transaction with today's date.
     *
     * [Categoria] is always set to `"General"` / `0.0` for domain compatibility;
     * the UI does not expose category or budget-limit input.
     *
     * @param descripcion a short description of the movement
     * @param monto       the absolute amount (positive; direction is set by [tipo])
     * @param tipo        [TipoTransaccion.INGRESO] for income or [TipoTransaccion.GASTO] for expense
     * @return the auto-generated row ID assigned by Room
     */
    suspend fun registrarTransaccion(
        descripcion: String,
        monto: Double,
        tipo: TipoTransaccion
    ): Long {
        val transaccion = Transaccion(
            descripcion = descripcion,
            monto = monto,
            tipo = tipo,
            fecha = LocalDate.now(),
            categoria = Categoria(
                nombre = "General",
                presupuestoLimite = 0.0
            )
        )
        return repositorio.insertar(transaccion)
    }

    /**
     * Calculates the all-time net balance (total income minus total expenses).
     *
     * @return net balance across all recorded transactions
     */
    suspend fun calcularSaldo(): Double {
        val todas = repositorio.obtenerTodas()
        val ingresos = todas.filter { it.tipo == TipoTransaccion.INGRESO }.sumOf { it.monto }
        val gastos = todas.filter { it.tipo == TipoTransaccion.GASTO }.sumOf { it.monto }
        return ingresos - gastos
    }

    /**
     * Calculates the net balance for the current calendar month.
     *
     * @return net balance from the first day of the current month to today
     */
    suspend fun calcularSaldoMesActual(): Double {
        val inicio = LocalDate.now().withDayOfMonth(1)
        val fin = LocalDate.now()
        val transacciones = repositorio.obtenerEntreFechas(inicio, fin)
        val ingresos = transacciones.filter { it.tipo == TipoTransaccion.INGRESO }.sumOf { it.monto }
        val gastos = transacciones.filter { it.tipo == TipoTransaccion.GASTO }.sumOf { it.monto }
        return ingresos - gastos
    }

    /**
     * Builds a full [ResumenMensual] for the current calendar month.
     *
     * @return aggregated income, expenses, balance, and transaction list for this month
     */
    suspend fun obtenerResumenMensual(): ResumenMensual {
        val inicio = LocalDate.now().withDayOfMonth(1)
        val fin = LocalDate.now()
        val transacciones = repositorio.obtenerEntreFechas(inicio, fin)
        val ingresos = transacciones.filter { it.tipo == TipoTransaccion.INGRESO }.sumOf { it.monto }
        val gastos = transacciones.filter { it.tipo == TipoTransaccion.GASTO }.sumOf { it.monto }
        return ResumenMensual(
            mes = LocalDate.now().monthValue,
            anio = LocalDate.now().year,
            ingresos = ingresos,
            gastos = gastos,
            saldo = ingresos - gastos,
            transacciones = transacciones
        )
    }

    /**
     * Deletes the transaction with the given [id].
     *
     * @param id primary key of the transaction to remove
     */
    suspend fun eliminarTransaccion(id: Long) = repositorio.eliminar(id)
}

/**
 * Aggregated monthly financial figures for the current calendar month.
 *
 * @property mes          current month number (1–12)
 * @property anio         current year
 * @property ingresos     total income this month
 * @property gastos       total expenses this month
 * @property saldo        net balance (ingresos − gastos)
 * @property transacciones all transactions recorded this month
 */
data class ResumenMensual(
    val mes: Int,
    val anio: Int,
    val ingresos: Double,
    val gastos: Double,
    val saldo: Double,
    val transacciones: List<Transaccion>
)
