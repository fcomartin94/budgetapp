package com.finanzapi.controller.dto;

import com.finanzapi.model.TipoTransaccion;

/**
 * Simplified request payload for the {@code POST /api/transacciones/simple} endpoint.
 *
 * <p>Accepts only the three fields a client strictly needs to record a transaction.
 * Server-side validation in {@link com.finanzapi.controller.BudgetController} ensures
 * that {@code descripcion} is not blank, {@code monto} is positive, and {@code tipo}
 * is non-null before the request is forwarded to the service layer.</p>
 */
public class SimpleTransaccionRequest {
    private String descripcion;
    private double monto;
    private TipoTransaccion tipo;

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public TipoTransaccion getTipo() {
        return tipo;
    }

    public void setTipo(TipoTransaccion tipo) {
        this.tipo = tipo;
    }
}
