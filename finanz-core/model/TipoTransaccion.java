package model;

/**
 * The two possible directions of a financial transaction.
 *
 * <p>Used as a discriminator throughout the service and repository layers
 * to separate income from expenses when computing balances and summaries.
 * Persisted as its name string in the CSV backing file.</p>
 */
public enum TipoTransaccion {
    /** Money coming in (salary, freelance income, etc.). */
    INGRESO,
    /** Money going out (rent, groceries, bills, etc.). */
    GASTO
}
