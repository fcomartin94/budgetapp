package ui;

import model.TipoTransaccion;
import service.BudgetService;
import util.MoneyFormatter;

import java.util.Scanner;

public class ConsolaMenu {

    private final BudgetService servicio;
    private final Scanner scanner;

    public ConsolaMenu(BudgetService servicio) {
        this.servicio = servicio;
        this.scanner = new Scanner(System.in);
    }

    public void iniciar() {
        int opcion = -1;

        while (opcion != 0) {
            mostrarMenu();
            opcion = leerEntero("Elige una opcion: ");

            switch (opcion) {
                case 1 -> registrarIngreso();
                case 2 -> registrarGasto();
                case 3 -> mostrarTodasLasTransacciones();
                case 4 -> mostrarResumenMensual();
                case 5 -> eliminarTransaccion();
                case 6 -> mostrarSaldoMesActual();
                case 0 -> System.out.println("Saliendo del programa...");
                default -> System.out.println("Opcion no valida");
            }
        }

        scanner.close();
    }

    private void mostrarMenu() {
        System.out.println("\n================================================");
        System.out.println("                    BUDGET APP");
        System.out.println("================================================");
        System.out.println("1. Registrar ingreso");
        System.out.println("2. Registrar gasto");
        System.out.println("3. Mostrar todas las transacciones");
        System.out.println("4. Mostrar resumen mensual");
        System.out.println("5. Eliminar transaccion");
        System.out.println("6. Mostrar saldo del mes actual");
        System.out.println("0. Salir");
        System.out.println("================================================\n");
    }

    private void registrarIngreso() {
        System.out.println("\n-- Nuevo ingreso --");
        String descripcion = leerTexto("Descripcion: ");
        double monto = leerDecimal("Monto (€): ");
        servicio.registrarTransaccion(descripcion, monto, TipoTransaccion.INGRESO);
    }

    private void registrarGasto() {
        System.out.println("\n-- Nuevo gasto --");
        String descripcion = leerTexto("Descripcion: ");
        double monto = leerDecimal("Monto (€): ");
        servicio.registrarTransaccion(descripcion, monto, TipoTransaccion.GASTO);
    }

    private void mostrarTodasLasTransacciones() {
        System.out.println("\n-- Todas las transacciones --");
        var todas = servicio.obtenerTodas();

        if (todas.isEmpty()) {
            System.out.println("No hay transacciones registradas.");
            return;
        }

        for (var transaccion : todas) {
            System.out.println(transaccion);
        }
    }

    private void mostrarResumenMensual() {
        servicio.mostrarResumenMensual();
    }

    private void eliminarTransaccion() {
        mostrarTodasLasTransacciones();
        int id = leerEntero("ID de la transaccion a eliminar: ");
        boolean eliminado = servicio.eliminarTransaccion(id);

        if (eliminado) {
            System.out.println("Transaccion eliminada correctamente.");
        } else {
            System.out.println("No se encontro la transaccion con ID: " + id);
        }
    }

    private void mostrarSaldoMesActual() {
        double saldo = servicio.calcularSaldoMesActual();
        System.out.println("Saldo del mes actual: " + MoneyFormatter.format(saldo));
    }

    private String leerTexto(String mensaje) {
        System.out.print(mensaje);
        return scanner.nextLine().trim();
    }

    private int leerEntero(String mensaje) {
        while (true) {
            try {
                System.out.print(mensaje);
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Por favor, ingrese un numero entero valido.");
            }
        }
    }

    private double leerDecimal(String mensaje) {
        while (true) {
            try {
                System.out.print(mensaje);
                String valor = scanner.nextLine().trim().replace(',', '.');
                return Double.parseDouble(valor);
            } catch (NumberFormatException e) {
                System.out.println("Por favor, ingrese un numero decimal valido.");
            }
        }
    }
}
