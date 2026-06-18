package com.finanzapp.data.model

/**
 * The two possible directions of a financial transaction.
 *
 * Stored as its name string in the `transacciones` table via
 * [com.finanzapp.data.local.Converters.fromTipoTransaccion].
 */
enum class TipoTransaccion {
    /** Money coming in (salary, freelance income, etc.). */
    INGRESO,
    /** Money going out (rent, groceries, bills, etc.). */
    GASTO
}
