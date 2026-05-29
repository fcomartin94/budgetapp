package com.finanzapp.data.local

import com.finanzapp.data.model.TipoTransaccion
import com.finanzapp.data.model.Transaccion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate

/**
 * Repository that mediates between the domain layer and the Room DAO.
 *
 * This class provides a clean API over [TransaccionDao], abstracting away
 * Room-specific details from the domain and ViewModel layers. It follows the
 * Repository pattern: callers never interact with the DAO directly.
 *
 * ## Reactive vs. suspend
 * - [transacciones] is a [Flow] that Room automatically updates whenever the
 *   underlying `transacciones` table changes — the UI observes it and re-renders
 *   without explicit refresh calls.
 * - Write operations ([insertar], [eliminar]) and one-shot reads ([obtenerTodas],
 *   [obtenerPorTipo], [obtenerEntreFechas]) are `suspend` functions meant to be
 *   called from a coroutine scope (e.g. [kotlinx.coroutines.launch] in a ViewModel).
 *
 * @param dao the Room DAO for the `transacciones` table
 */
class TransaccionRepository(private val dao: TransaccionDao) {

    /**
     * A live [Flow] of all transactions, ordered by date descending.
     *
     * Room emits a new list every time the `transacciones` table changes,
     * making this the primary reactive data source for the UI.
     */
    val transacciones: Flow<List<Transaccion>> = dao.obtenerTodas()

    /**
     * Inserts a new transaction into the local database.
     *
     * @param transaccion the transaction to persist (id should be 0 for auto-generation)
     * @return the auto-generated row ID assigned by Room
     */
    suspend fun insertar(transaccion: Transaccion): Long = dao.insertar(transaccion)

    /**
     * Deletes the transaction with the given ID.
     *
     * @param id the primary key of the transaction to remove
     */
    suspend fun eliminar(id: Long) = dao.eliminarPorId(id)

    /**
     * Returns a one-shot snapshot of all transactions.
     *
     * This collects the first emission from the DAO's [Flow], making it
     * suitable for one-time calculations that do not need to stay reactive.
     *
     * @return the current list of all transactions
     */
    suspend fun obtenerTodas(): List<Transaccion> = dao.obtenerTodas().first()

    /**
     * Returns all transactions matching the given type.
     *
     * @param tipo [TipoTransaccion.INGRESO] for income or [TipoTransaccion.GASTO] for expenses
     * @return transactions of the requested type, ordered by date descending
     */
    suspend fun obtenerPorTipo(tipo: TipoTransaccion): List<Transaccion> =
        dao.obtenerPorTipo(tipo)

    /**
     * Returns all transactions whose date falls within the given range (inclusive).
     *
     * @param inicio the start date of the range
     * @param fin    the end date of the range
     * @return transactions within the date range, ordered by date descending
     */
    suspend fun obtenerEntreFechas(inicio: LocalDate, fin: LocalDate): List<Transaccion> =
        dao.obtenerEntreFechas(inicio, fin)
}
