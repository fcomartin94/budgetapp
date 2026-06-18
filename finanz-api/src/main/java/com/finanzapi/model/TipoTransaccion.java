package com.finanzapi.model;

/**
 * The two possible directions of a financial transaction.
 *
 * <p>Used as a discriminator throughout the service and repository layers
 * to separate income from expenses when computing balances and summaries.</p>
 *
 * <p>Persisted as a string column ({@code @Enumerated(EnumType.STRING)}) in
 * the {@code transacciones} table so the stored value is human-readable.</p>
 */
public enum TipoTransaccion {
    /** Money coming in (salary, freelance income, etc.). */
    INGRESO,
    /** Money going out (rent, groceries, bills, etc.). */
    GASTO
}
