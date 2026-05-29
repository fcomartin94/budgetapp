package service;

import model.TipoTransaccion;
import model.Transaccion;
import repository.TransaccionRepository;
import util.MoneyFormatter;

import java.time.LocalDate;
import java.util.List;

/**
 * Core business logic for personal budget management.
 *
 * <p>This service sits between the UI layer ({@code ConsolaMenu}) and the
 * persistence layer ({@code TransaccionRepository}). It encapsulates all
 * financial calculations and keeps the UI free of business rules — the same
 * pattern used by the Spring Boot version ({@code finanz-api}).</p>
 *
 * <p>Dependency injection is manual: a {@code TransaccionRepository} is passed
 * via constructor, making the service independently testable without a framework
 * (see {@code test/BudgetServiceTest.java}).</p>
 */
public class BudgetService {

    private final TransaccionRepository repositorio;

    /**
     * @param repositorio the repository that handles CSV persistence
     */
    public BudgetService(TransaccionRepository repositorio) {
        this.repositorio = repositorio;
    }

    /**
     * Records a new transaction with the current date.
     *
     * <p>The ID is assigned automatically by the repository on save. The
     * transaction is immediately persisted to the CSV file.</p>
     *
     * @param descripcion a short description of the transaction
     * @param monto       the amount (must be positive; sign is determined by {@code tipo})
     * @param tipo        {@code INGRESO} (income) or {@code GASTO} (expense)
     */
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

    /**
     * Calculates the all-time net balance: total income minus total expenses.
     *
     * @return the net balance across all recorded transactions
     */
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

    /**
     * Calculates the net balance for the current calendar month.
     *
     * <p>Only transactions whose date falls in the current month and year are
     * included. Transactions from previous months are ignored.</p>
     *
     * @return the net balance for the current month (income minus expenses)
     */
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

    /**
     * Prints a formatted monthly summary to stdout.
     *
     * <p>Lists all transactions in the current month along with totals for
     * income, expenses, and the resulting balance. Amounts are formatted using
     * {@link MoneyFormatter} (locale {@code es-ES}, EUR currency).</p>
     */
    public void mostrarResumenMensual() {
        List<Transaccion> todas = repositorio.obtenerTodas();
        int mesActual = LocalDate.now().getMonthValue();
        int anioActual = LocalDate.now().getYear();

        double ingresos = 0.0;
        double gastos = 0.0;

        System.out.println("\n--- Monthly summary " + mesActual + "/" + anioActual + " ---");

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
        System.out.println("Total income   : " + MoneyFormatter.format(ingresos));
        System.out.println("Total expenses : " + MoneyFormatter.format(gastos));
        System.out.println("--------------------------------");
        System.out.println("Net balance    : " + MoneyFormatter.format(ingresos - gastos));
        System.out.println("--------------------------------");
    }

    /**
     * Deletes a transaction by its ID.
     *
     * @param id the transaction ID to remove
     * @return {@code true} if the transaction was found and deleted;
     *         {@code false} if no transaction with that ID exists
     */
    public boolean eliminarTransaccion(int id) {
        return repositorio.eliminar(id);
    }

    /**
     * Returns all recorded transactions across all time periods.
     *
     * @return a copy of the in-memory transaction list
     */
    public List<Transaccion> obtenerTodas() {
        return repositorio.obtenerTodas();
    }
}
