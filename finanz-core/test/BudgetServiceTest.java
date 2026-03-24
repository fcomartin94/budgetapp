package test;

import model.TipoTransaccion;
import model.Transaccion;
import repository.TransaccionRepository;
import service.BudgetService;

import java.time.LocalDate;

public final class BudgetServiceTest {

    private BudgetServiceTest() {
    }

    public static void runAll() {
        testRegistrarYCalcularSaldo();
        testSaldoMesActualSoloCuentaMesActual();
    }

    private static void testRegistrarYCalcularSaldo() {
        String csvPath = TestUtils.tempCsvPath();
        try {
            TransaccionRepository repo = new TransaccionRepository(csvPath);
            BudgetService service = new BudgetService(repo);

            service.registrarTransaccion("Nomina", 1200.0, TipoTransaccion.INGRESO);
            service.registrarTransaccion("Supermercado", 200.5, TipoTransaccion.GASTO);

            TestAssertions.assertEquals(2, service.obtenerTodas().size(), "Debe guardar 2 transacciones");
            TestAssertions.assertEquals(999.5, service.calcularSaldo(), 0.0001, "Saldo general incorrecto");
        } finally {
            TestUtils.deleteIfExists(csvPath);
        }
    }

    private static void testSaldoMesActualSoloCuentaMesActual() {
        String csvPath = TestUtils.tempCsvPath();
        try {
            TransaccionRepository repo = new TransaccionRepository(csvPath);
            BudgetService service = new BudgetService(repo);

            LocalDate hoy = LocalDate.now();
            LocalDate mesAnterior = hoy.minusMonths(1);

            repo.guardar(new Transaccion(0, "Ingreso actual", 1000.0, TipoTransaccion.INGRESO, hoy));
            repo.guardar(new Transaccion(0, "Gasto actual", 300.0, TipoTransaccion.GASTO, hoy));
            repo.guardar(new Transaccion(0, "Gasto antiguo", 999.0, TipoTransaccion.GASTO, mesAnterior));

            TestAssertions.assertEquals(700.0, service.calcularSaldoMesActual(), 0.0001,
                    "El saldo mensual debe ignorar movimientos de otros meses");
        } finally {
            TestUtils.deleteIfExists(csvPath);
        }
    }
}
