package com.finanzapp.domain

import com.finanzapp.data.local.TransaccionRepository
import com.finanzapp.data.model.Categoria
import com.finanzapp.data.model.TipoTransaccion
import com.finanzapp.data.model.Transaccion
import java.time.LocalDate

class BudgetService(private val repositorio: TransaccionRepository) {

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

    suspend fun calcularSaldo(): Double {
        val todas = repositorio.obtenerTodas()
        val ingresos = todas.filter { it.tipo == TipoTransaccion.INGRESO }.sumOf { it.monto }
        val gastos = todas.filter { it.tipo == TipoTransaccion.GASTO }.sumOf { it.monto }
        return ingresos - gastos
    }

    suspend fun calcularSaldoMesActual(): Double {
        val inicio = LocalDate.now().withDayOfMonth(1)
        val fin = LocalDate.now()
        val transacciones = repositorio.obtenerEntreFechas(inicio, fin)
        val ingresos = transacciones.filter { it.tipo == TipoTransaccion.INGRESO }.sumOf { it.monto }
        val gastos = transacciones.filter { it.tipo == TipoTransaccion.GASTO }.sumOf { it.monto }
        return ingresos - gastos
    }

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

    suspend fun eliminarTransaccion(id: Long) = repositorio.eliminar(id)
}

data class ResumenMensual(
    val mes: Int,
    val anio: Int,
    val ingresos: Double,
    val gastos: Double,
    val saldo: Double,
    val transacciones: List<Transaccion>
)
