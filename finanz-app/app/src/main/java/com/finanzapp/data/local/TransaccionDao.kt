package com.finanzapp.data.local

import androidx.room.*
import com.finanzapp.data.model.TipoTransaccion
import com.finanzapp.data.model.Transaccion
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TransaccionDao {

    @Query("SELECT * FROM transacciones ORDER BY fecha DESC")
    fun obtenerTodas(): Flow<List<Transaccion>>

    @Query("SELECT * FROM transacciones WHERE id = :id")
    suspend fun obtenerPorId(id: Long): Transaccion?

    @Query("SELECT * FROM transacciones WHERE tipo = :tipo")
    suspend fun obtenerPorTipo(tipo: TipoTransaccion): List<Transaccion>

    @Query("SELECT * FROM transacciones WHERE fecha BETWEEN :inicio AND :fin")
    suspend fun obtenerEntreFechas(inicio: LocalDate, fin: LocalDate): List<Transaccion>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(transaccion: Transaccion): Long

    @Delete
    suspend fun eliminar(transaccion: Transaccion)

    @Query("DELETE FROM transacciones WHERE id = :id")
    suspend fun eliminarPorId(id: Long)
}
