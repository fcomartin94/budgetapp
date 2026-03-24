package test;

import model.TipoTransaccion;
import model.Transaccion;
import repository.TransaccionRepository;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;

public final class TransaccionRepositoryTest {

    private TransaccionRepositoryTest() {
    }

    public static void runAll() {
        testGuardarBuscarYEliminar();
        testCargaSoportaCsvAntiguoConColumnasExtra();
    }

    private static void testGuardarBuscarYEliminar() {
        String csvPath = TestUtils.tempCsvPath();
        try {
            TransaccionRepository repo = new TransaccionRepository(csvPath);
            repo.guardar(new Transaccion(0, "Freelance", 500.0, TipoTransaccion.INGRESO, LocalDate.now()));

            var guardada = repo.buscarPorId(1);
            TestAssertions.assertTrue(guardada.isPresent(), "Debe encontrar la transaccion por ID");
            TestAssertions.assertEquals("Freelance", guardada.get().getDescripcion(), "Descripcion incorrecta");

            boolean eliminado = repo.eliminar(1);
            TestAssertions.assertTrue(eliminado, "Debe eliminar transaccion existente");
            TestAssertions.assertEquals(0, repo.obtenerTodas().size(), "La lista debe quedar vacia");
        } finally {
            TestUtils.deleteIfExists(csvPath);
        }
    }

    private static void testCargaSoportaCsvAntiguoConColumnasExtra() {
        String csvPath = TestUtils.tempCsvPath();
        try {
            writeLine(csvPath, "7,Linea antigua,42.5,GASTO,2026-01-15,comida,150.0");
            TransaccionRepository repo = new TransaccionRepository(csvPath);

            TestAssertions.assertEquals(1, repo.obtenerTodas().size(), "Debe cargar una linea valida");
            Transaccion t = repo.obtenerTodas().get(0);
            TestAssertions.assertEquals(7, t.getId(), "ID cargado incorrecto");
            TestAssertions.assertEquals("Linea antigua", t.getDescripcion(), "Descripcion cargada incorrecta");
            TestAssertions.assertEquals(42.5, t.getMonto(), 0.0001, "Monto cargado incorrecto");
            TestAssertions.assertEquals(TipoTransaccion.GASTO, t.getTipo(), "Tipo cargado incorrecto");
        } finally {
            TestUtils.deleteIfExists(csvPath);
        }
    }

    private static void writeLine(String path, String line) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            writer.write(line);
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException("No se pudo preparar CSV de prueba", e);
        }
    }
}
