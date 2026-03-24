package com.finanzapi.service;

import com.finanzapi.model.TipoTransaccion;
import com.finanzapi.model.Transaccion;
import com.finanzapi.repository.TransaccionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class BudgetService {

    private final TransaccionRepository repositorio;

    public BudgetService(TransaccionRepository repositorio) {
        this.repositorio = repositorio;
    }

    public Transaccion registrar(Transaccion transaccion) {
        transaccion.setFecha(LocalDate.now());
        return repositorio.save(transaccion);
    }

    public Transaccion registrarSimple(String descripcion, double monto, TipoTransaccion tipo) {
        Transaccion transaccion = new Transaccion(
                descripcion,
                monto,
                tipo,
                LocalDate.now()
        );
        return repositorio.save(transaccion);
    }

    public List<Transaccion> obtenerTodas() {
        return repositorio.findAll();
    }

    public Optional<Transaccion> obtenerPorId(Long id) {
        return repositorio.findById(id);
    }

    public boolean eliminar(Long id) {
        if (repositorio.existsById(id)) {
            repositorio.deleteById(id);
            return true;
        }
        return false;
    }

    public double calcularSaldo() {
        double ingresos = repositorio.findByTipo(TipoTransaccion.INGRESO)
                .stream()
                .mapToDouble(Transaccion::getMonto)
                .sum();

        double gastos = repositorio.findByTipo(TipoTransaccion.GASTO)
                .stream()
                .mapToDouble(Transaccion::getMonto)
                .sum();

        return ingresos - gastos;
    }

    public double calcularSaldoMesActual() {
        LocalDate inicio = LocalDate.now().withDayOfMonth(1);
        LocalDate fin = LocalDate.now();

        List<Transaccion> transacciones = repositorio.findByFechaBetween(inicio, fin);

        double ingresos = transacciones.stream()
                .filter(t -> t.getTipo() == TipoTransaccion.INGRESO)
                .mapToDouble(Transaccion::getMonto)
                .sum();

        double gastos = transacciones.stream()
                .filter(t -> t.getTipo() == TipoTransaccion.GASTO)
                .mapToDouble(Transaccion::getMonto)
                .sum();

        return ingresos - gastos;
    }

    public Map<String, Object> obtenerResumenMensual() {
        LocalDate inicio = LocalDate.now().withDayOfMonth(1);
        LocalDate fin = LocalDate.now();

        List<Transaccion> transacciones = repositorio.findByFechaBetween(inicio, fin);

        double ingresos = transacciones.stream()
                .filter(t -> t.getTipo() == TipoTransaccion.INGRESO)
                .mapToDouble(Transaccion::getMonto)
                .sum();

        double gastos = transacciones.stream()
                .filter(t -> t.getTipo() == TipoTransaccion.GASTO)
                .mapToDouble(Transaccion::getMonto)
                .sum();

        Map<String, Object> resumen = new HashMap<>();
        resumen.put("mes", LocalDate.now().getMonthValue());
        resumen.put("anio", LocalDate.now().getYear());
        resumen.put("ingresos", ingresos);
        resumen.put("gastos", gastos);
        resumen.put("saldo", ingresos - gastos);
        resumen.put("transacciones", transacciones);

        return resumen;
    }
}
