package model;

import util.MoneyFormatter;

import java.time.LocalDate;

/**
 * Domain model representing a single financial transaction.
 *
 * <p>A transaction records one monetary movement — either an income
 * ({@link TipoTransaccion#INGRESO}) or an expense ({@link TipoTransaccion#GASTO}).
 * It is the core entity of Finanz Core and is shared conceptually with the
 * {@code finanz-api} and {@code finanz-app} modules of the FinanzApp ecosystem.</p>
 *
 * <p>The {@code id} field is assigned by {@code TransaccionRepository} at
 * persistence time; a value of {@code 0} indicates a transient (unsaved) instance.</p>
 */
public class Transaccion {

    /** Auto-assigned by the repository; {@code 0} for unsaved instances. */
    private int id;

    /** Short human-readable description of the transaction (e.g. "Salary", "Rent"). */
    private String descripcion;

    /** The absolute monetary amount (always positive; sign is implied by {@link #tipo}). */
    private double monto;

    /** Whether this is income or an expense. */
    private TipoTransaccion tipo;

    /** The date the transaction occurred; set to today when registered via {@code BudgetService}. */
    private LocalDate fecha;

    /**
     * Full constructor used by the repository when loading from CSV and by
     * the service when creating new transactions.
     *
     * @param id          transaction ID (0 for new, positive integer for persisted)
     * @param descripcion short description
     * @param monto       absolute amount (positive)
     * @param tipo        {@code INGRESO} or {@code GASTO}
     * @param fecha       the transaction date
     */
    public Transaccion(
            int id,
            String descripcion,
            double monto,
            TipoTransaccion tipo,
            LocalDate fecha
    ) {
        this.id = id;
        this.descripcion = descripcion;
        this.monto = monto;
        this.tipo = tipo;
        this.fecha = fecha;
    }

    /** @return the transaction ID */
    public int getId() {
        return id;
    }

    /** @param id the transaction ID (set by the repository on save) */
    public void setId(int id) {
        this.id = id;
    }

    /** @return the transaction description */
    public String getDescripcion() {
        return descripcion;
    }

    /** @param descripcion a new description */
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /** @return the absolute monetary amount */
    public double getMonto() {
        return monto;
    }

    /** @param monto the new amount (must be positive) */
    public void setMonto(double monto) {
        this.monto = monto;
    }

    /** @return the transaction type ({@code INGRESO} or {@code GASTO}) */
    public TipoTransaccion getTipo() {
        return tipo;
    }

    /** @param tipo the new transaction type */
    public void setTipo(TipoTransaccion tipo) {
        this.tipo = tipo;
    }

    /** @return the transaction date */
    public LocalDate getFecha() {
        return fecha;
    }

    /** @param fecha the new transaction date */
    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    /**
     * Returns a human-readable single-line summary formatted as:
     * {@code [id] date | type | description | amount}.
     *
     * @return formatted string using {@link MoneyFormatter}
     */
    @Override
    public String toString() {
        return "[" + id + "] " + fecha + " | " + tipo + " | "
                + descripcion + " | "
                + MoneyFormatter.format(monto);
    }
}
