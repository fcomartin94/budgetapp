package com.finanzapp.data.model

/**
 * Embedded value object representing a transaction category.
 *
 * Stored inline in the `transacciones` table via `@Embedded(prefix = "cat_")`.
 * The UI does not expose category or budget-limit input; all transactions are
 * created with `nombre = "General"` and `presupuestoLimite = 0.0` to maintain
 * domain compatibility with the other modules in the FinanzApp ecosystem.
 */
data class Categoria(
    val nombre: String,
    val presupuestoLimite: Double
)
