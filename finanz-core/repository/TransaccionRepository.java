package repository;

import model.TipoTransaccion;
import model.Transaccion;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * File-backed in-memory repository for financial transactions.
 *
 * <p>Transactions are kept in an {@link ArrayList} for fast reads and written
 * to a simple CSV file after every mutation. The CSV format is:</p>
 * <pre>id,description,amount,type,date</pre>
 *
 * <p>The repository accepts a configurable file path to enable isolated test
 * runs with temporary files (see {@code test/TransaccionRepositoryTest.java}).</p>
 *
 * <h3>Design notes</h3>
 * <ul>
 *   <li>The ID counter is an incrementing integer; it restores to
 *       {@code max(existing ids) + 1} on load so IDs stay unique across sessions.</li>
 *   <li>Commas in descriptions are replaced with spaces before writing to avoid
 *       breaking the single-pass CSV parser on reload.</li>
 *   <li>Malformed lines (fewer than 5 fields) are silently skipped for
 *       backwards compatibility with older file formats.</li>
 * </ul>
 */
public class TransaccionRepository {

    private final String archivoPath;
    private final List<Transaccion> transacciones;
    private int contadorId;

    /**
     * Creates a repository backed by the default file {@code transacciones.csv}
     * in the current working directory.
     */
    public TransaccionRepository() {
        this("transacciones.csv");
    }

    /**
     * Creates a repository backed by the specified file.
     *
     * <p>If the file already exists its contents are loaded into memory.
     * If the file does not exist it is created on the first write.</p>
     *
     * @param archivoPath path to the CSV backing file
     */
    public TransaccionRepository(String archivoPath) {
        this.archivoPath = archivoPath;
        this.transacciones = new ArrayList<>();
        this.contadorId = 1;
        cargarDesdeArchivo();
    }

    /**
     * Persists a new transaction, assigning it an auto-incremented ID.
     *
     * <p>The ID is set on the passed instance (side-effect), the transaction
     * is added to the in-memory list, and the CSV file is rewritten.</p>
     *
     * @param transaccion the transaction to save (its {@code id} will be overwritten)
     */
    public void guardar(Transaccion transaccion) {
        transaccion.setId(contadorId++);
        transacciones.add(transaccion);
        guardarEnArchivo();
    }

    /**
     * Returns a defensive copy of all in-memory transactions.
     *
     * @return a new list containing all current transactions
     */
    public List<Transaccion> obtenerTodas() {
        return new ArrayList<>(transacciones);
    }

    /**
     * Looks up a transaction by its ID.
     *
     * @param id the transaction ID
     * @return an {@link Optional} containing the matching transaction, or empty if not found
     */
    public Optional<Transaccion> buscarPorId(int id) {
        return transacciones.stream()
                .filter(t -> t.getId() == id)
                .findFirst();
    }

    /**
     * Removes the transaction with the given ID and rewrites the CSV file.
     *
     * @param id the transaction ID to delete
     * @return {@code true} if a matching transaction was found and removed;
     *         {@code false} if no transaction with that ID exists
     */
    public boolean eliminar(int id) {
        boolean eliminado = transacciones.removeIf(t -> t.getId() == id);
        if (eliminado) {
            guardarEnArchivo();
        }
        return eliminado;
    }

    /**
     * Serialises all in-memory transactions to the CSV backing file.
     *
     * <p>The entire file is rewritten on every mutation (no append mode).
     * Commas in descriptions are replaced with spaces to preserve the simple
     * comma-delimited format.</p>
     */
    private void guardarEnArchivo() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivoPath))) {
            for (Transaccion t : transacciones) {
                // Replace commas in description to avoid breaking the CSV format
                String descripcion = t.getDescripcion().replace(",", " ");

                writer.write(
                        t.getId() + "," +
                                descripcion + "," +
                                t.getMonto() + "," +
                                t.getTipo() + "," +
                                t.getFecha()
                );
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving file: " + e.getMessage());
        }
    }

    /**
     * Loads transactions from the CSV backing file into memory on startup.
     *
     * <p>If the file does not exist the method returns silently (first run).
     * Lines with fewer than 5 fields are skipped to tolerate old file formats.
     * The ID counter is set to {@code max(loaded id) + 1} to prevent ID
     * collisions across sessions.</p>
     */
    private void cargarDesdeArchivo() {
        File archivo = new File(archivoPath);
        if (!archivo.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(archivoPath))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] partes = linea.split(",");
                if (partes.length < 5) {
                    continue; // skip malformed or legacy rows
                }

                int id = Integer.parseInt(partes[0]);
                String descripcion = partes[1];
                double monto = Double.parseDouble(partes[2]);
                TipoTransaccion tipo = TipoTransaccion.valueOf(partes[3]);
                LocalDate fecha = LocalDate.parse(partes[4]);

                transacciones.add(new Transaccion(id, descripcion, monto, tipo, fecha));
                if (id >= contadorId) {
                    contadorId = id + 1;
                }
            }
        } catch (IOException | RuntimeException e) {
            System.out.println("Error loading file: " + e.getMessage());
        }
    }
}
