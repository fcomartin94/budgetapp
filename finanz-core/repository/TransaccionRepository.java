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

public class TransaccionRepository {

    private final String archivoPath;
    private final List<Transaccion> transacciones;
    private int contadorId;

    public TransaccionRepository() {
        this("transacciones.csv");
    }

    public TransaccionRepository(String archivoPath) {
        this.archivoPath = archivoPath;
        this.transacciones = new ArrayList<>();
        this.contadorId = 1;
        cargarDesdeArchivo();
    }

    public void guardar(Transaccion transaccion) {
        transaccion.setId(contadorId++);
        transacciones.add(transaccion);
        guardarEnArchivo();
    }

    public List<Transaccion> obtenerTodas() {
        return new ArrayList<>(transacciones);
    }

    public Optional<Transaccion> buscarPorId(int id) {
        return transacciones.stream()
                .filter(t -> t.getId() == id)
                .findFirst();
    }

    public boolean eliminar(int id) {
        boolean eliminado = transacciones.removeIf(t -> t.getId() == id);
        if (eliminado) {
            guardarEnArchivo();
        }
        return eliminado;
    }

    private void guardarEnArchivo() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivoPath))) {
            for (Transaccion t : transacciones) {
                // Evita romper el parseo CSV simple.
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
            System.out.println("Error al guardar: " + e.getMessage());
        }
    }

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
                    continue;
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
            System.out.println("Error al cargar: " + e.getMessage());
        }
    }
}
