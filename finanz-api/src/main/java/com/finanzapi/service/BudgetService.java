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

/**
 * Business logic for the Finanz API.
 *
 * <p>The Spring Boot counterpart to {@code finanz-core}'s {@code BudgetService} —
 * same domain logic, now backed by a JPA {@link TransaccionRepository} instead of
 * a CSV file. The service sits between {@code BudgetController} and the repository,
 * keeping HTTP concerns out of the business layer.</p>
 *
 * <h3>Architecture note</h3>
 * <p>Both {@code registrar} and {@code registrarSimple} persist via Spring Data JPA.
 * The difference is validation: {@code registrarSimple} builds the entity in the
 * service from validated primitives, whereas {@code registrar} accepts the full
 * entity from the controller (for integration-friendly use).</p>
 */
@Service
public class BudgetService {

    private final TransaccionRepository repositorio;

    /**
     * @param repositorio the JPA repository backed by H2 ({@code transactions} table)
     */
    public BudgetService(TransaccionRepository repositorio) {
        this.repositorio = repositorio;
    }

    /**
     * Persists a full transaction, overriding its date to today.
     *
     * @param transaccion the transaction to save; its {@code fecha} will be set to now
     * @return the saved entity with auto-assigned ID and today's date
     */
    public Transaccion registrar(Transaccion transaccion) {
        transaccion.setFecha(LocalDate.now());
        return repositorio.save(transaccion);
    }

    /**
     * Builds and persists a transaction from primitive inputs.
     *
     * <p>Intended for the {@code /transacciones/simple} endpoint; the controller
     * validates inputs before calling this method.</p>
     *
     * @param descripcion a short description of the transaction
     * @param monto       the absolute amount (positive)
     * @param tipo        {@code INGRESO} (income) or {@code GASTO} (expense)
     * @return the saved transaction entity with auto-assigned ID
     */
    public Transaccion registrarSimple(String descripcion, double monto, TipoTransaccion tipo) {
        Transaccion transaccion = new Transaccion(
                descripcion,
                monto,
                tipo,
                LocalDate.now()
        );
        return repositorio.save(transaccion);
    }

    /**
     * Returns all recorded transactions.
     *
     * @return a list of all transactions; empty list if none exist
     */
    public List<Transaccion> obtenerTodas() {
        return repositorio.findAll();
    }

    /**
     * Retrieves a single transaction by its primary key.
     *
     * @param id the transaction ID
     * @return an {@link Optional} with the transaction, or empty if not found
     */
    public Optional<Transaccion> obtenerPorId(Long id) {
        return repositorio.findById(id);
    }

    /**
     * Deletes the transaction with the given ID.
     *
     * @param id the transaction ID
     * @return {@code true} if the transaction existed and was deleted;
     *         {@code false} if no matching ID was found
     */
    public boolean eliminar(Long id) {
        if (repositorio.existsById(id)) {
            repositorio.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Calculates the all-time net balance (total income minus total expenses).
     *
     * <p>Uses {@link TransaccionRepository#findByTipo} — a Spring Data derived
     * query — to avoid loading all transactions when type filtering is needed.</p>
     *
     * @return the net balance across all recorded transactions
     */
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

    /**
     * Calculates the net balance for the current calendar month.
     *
     * <p>Uses {@link TransaccionRepository#findByFechaBetween} (first day of
     * current month → today) to restrict the query to this month's transactions.</p>
     *
     * @return net balance for the current month (income minus expenses)
     */
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

    /**
     * Builds a full monthly summary map for the current calendar month.
     *
     * <p>Response map keys:</p>
     * <ul>
     *   <li>{@code mes} — current month number (1–12)</li>
     *   <li>{@code anio} — current year</li>
     *   <li>{@code ingresos} — total income this month</li>
     *   <li>{@code gastos} — total expenses this month</li>
     *   <li>{@code saldo} — net balance ({@code ingresos − gastos})</li>
     *   <li>{@code transacciones} — list of all transactions this month</li>
     * </ul>
     *
     * @return a {@link Map} with the keys described above
     */
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
