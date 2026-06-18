package com.finanzapi.model;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * JPA entity representing a single financial transaction.
 *
 * <p>Persisted in the H2 {@code transacciones} table. The schema is managed
 * by Hibernate via the active Spring profile ({@code ddl-auto=update} for dev,
 * {@code create-drop} for the test profile).</p>
 *
 * <p>The {@code fecha} field is always overwritten to today by
 * {@link com.finanzapi.service.BudgetService#registrar} — values supplied by
 * the client are ignored.</p>
 *
 * <p>This entity is the Spring Data / JPA counterpart of {@code finanz-core}'s
 * plain {@code Transaccion} class; both share the same field names and semantics.</p>
 */
@Entity
@Table(name = "transacciones")
public class Transaccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String descripcion;
    private double monto;

    @Enumerated(EnumType.STRING)
    private TipoTransaccion tipo;

    private LocalDate fecha;

    public Transaccion() {}

    public Transaccion(String descripcion, double monto, TipoTransaccion tipo, LocalDate fecha) {
        this.descripcion = descripcion;
        this.monto = monto;
        this.tipo = tipo;
        this.fecha = fecha;
    }

    public Long getId() { return id; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }
    public TipoTransaccion getTipo() { return tipo; }
    public void setTipo(TipoTransaccion tipo) { this.tipo = tipo; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
}
