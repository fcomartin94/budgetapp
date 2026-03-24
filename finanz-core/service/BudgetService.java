package service;

import model.TipoTransaccion;
import model.Transaccion;
import repository.TransaccionRepository;
import util.MoneyFormatter;

import java.time.LocalDate;
import java.util.List;

public class BudgetService {

    private final TransaccionRepository repositorio;

    public BudgetService(TransaccionRepository repositorio) {
        this.repositorio = repositorio;
    }

    public void registrarTransaccion(
            String descripcion,
            double monto,
            TipoTransaccion tipo
    ) {
        Transaccion transaccion = new Transaccion(
                0,
                descripcion,
                monto,
                tipo,
                LocalDate.now()
        );
        repositorio.guardar(transaccion);
        System.out.println("Transaccion registrada correctamente.");
    }

    public double calcularSaldo() {
        double ingresos = repositorio.obtenerTodas().stream()
                .filter(t -> t.getTipo() == TipoTransaccion.INGRESO)
                .mapToDouble(Transaccion::getMonto)
                .sum();

        double gastos = repositorio.obtenerTodas().stream()
                .filter(t -> t.getTipo() == TipoTransaccion.GASTO)
                .mapToDouble(Transaccion::getMonto)
                .sum();

        return ingresos - gastos;
    }

    public double calcularSaldoMesActual() {
        int mes = LocalDate.now().getMonthValue();
        int anio = LocalDate.now().getYear();

        double ingresos = repositorio.obtenerTodas().stream()
                .filter(t -> t.getTipo() == TipoTransaccion.INGRESO
                        && t.getFecha().getMonthValue() == mes
                        && t.getFecha().getYear() == anio)
                .mapToDouble(Transaccion::getMonto)
                .sum();

        double gastos = repositorio.obtenerTodas().stream()
                .filter(t -> t.getTipo() == TipoTransaccion.GASTO
                        && t.getFecha().getMonthValue() == mes
                        && t.getFecha().getYear() == anio)
                .mapToDouble(Transaccion::getMonto)
                .sum();

        return ingresos - gastos;
    }

    public void mostrarResumenMensual() {
        List<Transaccion> todas = repositorio.obtenerTodas();
        int mesActual = LocalDate.now().getMonthValue();
        int anioActual = LocalDate.now().getYear();

        double ingresos = 0.0;
        double gastos = 0.0;

        System.out.println("\n--- Resumen del mes " + mesActual + "/" + anioActual + " ---");

        for (Transaccion transaccion : todas) {
            if (transaccion.getFecha().getMonthValue() == mesActual
                    && transaccion.getFecha().getYear() == anioActual) {
                System.out.println(transaccion);
                if (transaccion.getTipo() == TipoTransaccion.INGRESO) {
                    ingresos += transaccion.getMonto();
                } else {
                    gastos += transaccion.getMonto();
                }
            }
        }

        System.out.println("--------------------------------");
        System.out.println("Total ingresos : " + MoneyFormatter.format(ingresos));
        System.out.println("Total gastos   : " + MoneyFormatter.format(gastos));
        System.out.println("--------------------------------");
        System.out.println("Saldo final    : " + MoneyFormatter.format(ingresos - gastos));
        System.out.println("--------------------------------");
    }

    public boolean eliminarTransaccion(int id) {
        return repositorio.eliminar(id);
    }

    public List<Transaccion> obtenerTodas() {
        return repositorio.obtenerTodas();
    }
}
