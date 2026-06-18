package com.finanzapp.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * Room entity representing a single financial transaction.
 *
 * Persisted in the `transacciones` table. [LocalDate] and [TipoTransaccion] are
 * stored via [com.finanzapp.data.local.Converters]. [Categoria] is embedded with
 * the `cat_` column prefix so its fields live in the same row.
 *
 * The [categoria] is always populated with a default `"General"` / `0.0` value by
 * [com.finanzapp.domain.BudgetService]; the UI does not expose it to the user.
 */
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
