package com.finanzapp.data.local

import androidx.room.*
import com.finanzapp.data.model.TipoTransaccion
import com.finanzapp.data.model.Transaccion
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Room DAO for the `transacciones` table.
 *
 * Provides reactive ([Flow]-based) and one-shot (`suspend`) access to transaction
 * data. Callers should go through [TransaccionRepository] rather than using this
 * interface directly.
 *
 * - [obtenerTodas] is the primary reactive source: Room re-emits on every write.
 * - All other reads and writes are `suspend` for use in coroutine scopes.
 */
@Dao
interface TransaccionDao {

    /** Returns all transactions ordered by date descending. Re-emits on every table change. */
    @Query("SELECT * FROM transacciones ORDER BY fecha DESC")
    fun obtenerTodas(): Flow<List<Transaccion>>

    /** Returns the transaction with the given [id], or `null` if not found. */
    @Query("SELECT * FROM transacciones WHERE id = :id")
    suspend fun obtenerPorId(id: Long): Transaccion?

    /** Returns all transactions matching the given [tipo]. */
    @Query("SELECT * FROM transacciones WHERE tipo = :tipo")
    suspend fun obtenerPorTipo(tipo: TipoTransaccion): List<Transaccion>

    /** Returns transactions whose date falls within [[inicio], [fin]] inclusive. */
    @Query("SELECT * FROM transacciones WHERE fecha BETWEEN :inicio AND :fin")
    suspend fun obtenerEntreFechas(inicio: LocalDate, fin: LocalDate): List<Transaccion>

    /** Inserts or replaces a transaction and returns the assigned row ID. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(transaccion: Transaccion): Long

    /** Deletes the given transaction by object identity. */
    @Delete
    suspend fun eliminar(transaccion: Transaccion)

    /** Deletes the transaction with the given [id]. */
    @Query("DELETE FROM transacciones WHERE id = :id")
    suspend fun eliminarPorId(id: Long)
}
