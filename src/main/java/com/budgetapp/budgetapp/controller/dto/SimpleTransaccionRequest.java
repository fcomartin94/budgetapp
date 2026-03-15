package com.budgetapp.budgetapp.controller.dto;

import com.budgetapp.budgetapp.model.TipoTransaccion;

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
