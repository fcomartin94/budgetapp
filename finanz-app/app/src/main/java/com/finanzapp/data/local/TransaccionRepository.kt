package com.finanzapp.data.local

import com.finanzapp.data.model.TipoTransaccion
import com.finanzapp.data.model.Transaccion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class TransaccionRepository(private val dao: TransaccionDao) {

    val transacciones: Flow<List<Transaccion>> = dao.obtenerTodas()

    suspend fun insertar(transaccion: Transaccion): Long = dao.insertar(transaccion)

    suspend fun eliminar(id: Long) = dao.eliminarPorId(id)

    suspend fun obtenerTodas(): List<Transaccion> = dao.obtenerTodas().first()

    suspend fun obtenerPorTipo(tipo: TipoTransaccion): List<Transaccion> =
        dao.obtenerPorTipo(tipo)

    suspend fun obtenerEntreFechas(inicio: LocalDate, fin: LocalDate): List<Transaccion> =
        dao.obtenerEntreFechas(inicio, fin)
}
