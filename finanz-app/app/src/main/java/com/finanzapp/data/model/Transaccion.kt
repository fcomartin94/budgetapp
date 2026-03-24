package com.finanzapp.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "transacciones")
data class Transaccion(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val descripcion: String,
    val monto: Double,
    val tipo: TipoTransaccion,
    val fecha: LocalDate,
    @Embedded(prefix = "cat_")
    val categoria: Categoria
)
