package model;

import util.MoneyFormatter;

import java.time.LocalDate;

public class Transaccion {
    private int id;
    private String descripcion;
    private double monto;
    private TipoTransaccion tipo;
    private LocalDate fecha;

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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    @Override
    public String toString() {
        return "[" + id + "] " + fecha + " | " + tipo + " | "
                + descripcion + " | "
                + MoneyFormatter.format(monto);
    }
}
