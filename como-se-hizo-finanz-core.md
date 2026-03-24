# Cómo se hizo: Finanz Core — Aplicación de consola en Java puro

> Documento técnico de construcción paso a paso. El objetivo es que cualquier persona con conocimientos básicos de Java pueda replicar esta aplicación desde cero.

---

## ¿Qué hace esta aplicación?

Es una aplicación de gestión de presupuesto personal que corre en la terminal. Permite registrar ingresos y gastos, consultar todas las transacciones, ver un resumen mensual y eliminar registros. Los datos se persisten en un archivo `.csv` en disco, por lo que sobreviven al cierre del programa.

---

## Requisitos previos

- Java 17 o superior (se usan switch expressions y `var`)
- Ninguna dependencia externa — solo la JDK estándar
- Un editor de texto o IDE (IntelliJ, VS Code, etc.)

---

## Estructura de carpetas

```
src/
├── Main.java
├── transacciones.csv          ← se crea automáticamente al ejecutar
├── model/
│   ├── TipoTransaccion.java
│   └── Transaccion.java
├── repository/
│   └── TransaccionRepository.java
├── service/
│   └── BudgetService.java
├── ui/
│   └── ConsolaMenu.java
├── util/
│   └── MoneyFormatter.java
└── test/
    ├── TestRunner.java
    ├── TestAssertions.java
    ├── TestUtils.java
    ├── BudgetServiceTest.java
    └── TransaccionRepositoryTest.java
```

La arquitectura sigue el patrón clásico de **capas**: cada capa solo conoce a la capa inmediatamente por debajo de ella. `ui` → `service` → `repository` → modelo. Nunca se salta niveles.

---

## Paso 1 — El modelo de datos

Lo primero es definir qué es una transacción. Se necesitan dos clases: un enum para el tipo y la entidad principal.

**`model/TipoTransaccion.java`**

```java
package model;

public enum TipoTransaccion {
    INGRESO,
    GASTO
}
```

Un `enum` es la forma más segura de representar un conjunto cerrado de valores. Así no hay riesgo de escribir `"ingreso"` en minúsculas por error.

**`model/Transaccion.java`**

```java
package model;

import util.MoneyFormatter;
import java.time.LocalDate;

public class Transaccion {
    private int id;
    private String descripcion;
    private double monto;
    private TipoTransaccion tipo;
    private LocalDate fecha;

    public Transaccion(int id, String descripcion, double monto,
                       TipoTransaccion tipo, LocalDate fecha) {
        this.id = id;
        this.descripcion = descripcion;
        this.monto = monto;
        this.tipo = tipo;
        this.fecha = fecha;
    }

    // getters y setters para cada campo...

    @Override
    public String toString() {
        return "[" + id + "] " + fecha + " | " + tipo + " | "
                + descripcion + " | "
                + MoneyFormatter.format(monto);
    }
}
```

`LocalDate` (no `Date` ni `LocalDateTime`) es suficiente porque solo necesitamos día/mes/año. El `toString()` usa `MoneyFormatter` que crearemos en el siguiente paso.

---

## Paso 2 — El formateador de moneda

Esta pequeña utilidad centraliza el formato de dinero en euros. Se escribe una sola vez y se reutiliza en toda la aplicación.

**`util/MoneyFormatter.java`**

```java
package util;

import java.text.NumberFormat;
import java.util.Locale;

public final class MoneyFormatter {

    private static final Locale LOCALE_ES = Locale.forLanguageTag("es-ES");
    private static final NumberFormat CURRENCY_FORMAT =
            NumberFormat.getCurrencyInstance(LOCALE_ES);

    private MoneyFormatter() {}   // evita instanciación

    public static String format(double amount) {
        return CURRENCY_FORMAT.format(amount).replace('\u00A0', ' ');
    }
}
```

El constructor privado convierte la clase en una utilidad estática pura. El `replace` elimina el espacio no separable (`\u00A0`) que Java inserta entre el número y el símbolo `€`, sustituyéndolo por un espacio normal para que se vea bien en la terminal.

---

## Paso 3 — La capa de repositorio (persistencia en CSV)

El repositorio se encarga exclusivamente de **leer y escribir** datos. No sabe nada de reglas de negocio.

**`repository/TransaccionRepository.java`**

```java
package repository;

import model.TipoTransaccion;
import model.Transaccion;
import java.io.*;
import java.time.LocalDate;
import java.util.*;

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
        cargarDesdeArchivo();   // carga al arrancar
    }

    public void guardar(Transaccion transaccion) {
        transaccion.setId(contadorId++);
        transacciones.add(transaccion);
        guardarEnArchivo();     // persiste tras cada cambio
    }

    public List<Transaccion> obtenerTodas() {
        return new ArrayList<>(transacciones);  // copia defensiva
    }

    public Optional<Transaccion> buscarPorId(int id) {
        return transacciones.stream()
                .filter(t -> t.getId() == id)
                .findFirst();
    }

    public boolean eliminar(int id) {
        boolean eliminado = transacciones.removeIf(t -> t.getId() == id);
        if (eliminado) guardarEnArchivo();
        return eliminado;
    }

    private void guardarEnArchivo() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivoPath))) {
            for (Transaccion t : transacciones) {
                String descripcion = t.getDescripcion().replace(",", " ");
                writer.write(t.getId() + "," + descripcion + ","
                        + t.getMonto() + "," + t.getTipo() + "," + t.getFecha());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error al guardar: " + e.getMessage());
        }
    }

    private void cargarDesdeArchivo() {
        File archivo = new File(archivoPath);
        if (!archivo.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(archivoPath))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] partes = linea.split(",");
                if (partes.length < 5) continue;  // línea malformada, la ignora

                int id = Integer.parseInt(partes[0]);
                String descripcion = partes[1];
                double monto = Double.parseDouble(partes[2]);
                TipoTransaccion tipo = TipoTransaccion.valueOf(partes[3]);
                LocalDate fecha = LocalDate.parse(partes[4]);

                transacciones.add(new Transaccion(id, descripcion, monto, tipo, fecha));
                if (id >= contadorId) contadorId = id + 1;
            }
        } catch (IOException | RuntimeException e) {
            System.out.println("Error al cargar: " + e.getMessage());
        }
    }
}
```

Puntos importantes:

- El constructor secundario que acepta una ruta de archivo es clave para los **tests**: permite usar un CSV temporal en lugar del fichero real.
- `obtenerTodas()` devuelve una **copia** de la lista, no la lista interna. Así se evita que el código externo la modifique sin pasar por el repositorio.
- Al leer CSV, se comprueba `partes.length < 5` antes de parsear, para tolerar líneas con columnas extra o malformadas sin explotar.
- Las comas dentro de las descripciones se reemplazan por espacios al escribir, ya que el CSV usa coma como separador.

---

## Paso 4 — La capa de servicio (lógica de negocio)

El servicio recibe el repositorio por constructor (**inyección de dependencias manual**). No sabe cómo se almacenan los datos — simplemente llama al repositorio.

**`service/BudgetService.java`**

```java
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

    public void registrarTransaccion(String descripcion, double monto, TipoTransaccion tipo) {
        Transaccion t = new Transaccion(0, descripcion, monto, tipo, LocalDate.now());
        repositorio.guardar(t);
        System.out.println("Transaccion registrada correctamente.");
    }

    public double calcularSaldo() {
        double ingresos = repositorio.obtenerTodas().stream()
                .filter(t -> t.getTipo() == TipoTransaccion.INGRESO)
                .mapToDouble(Transaccion::getMonto).sum();

        double gastos = repositorio.obtenerTodas().stream()
                .filter(t -> t.getTipo() == TipoTransaccion.GASTO)
                .mapToDouble(Transaccion::getMonto).sum();

        return ingresos - gastos;
    }

    public double calcularSaldoMesActual() {
        int mes = LocalDate.now().getMonthValue();
        int anio = LocalDate.now().getYear();

        double ingresos = repositorio.obtenerTodas().stream()
                .filter(t -> t.getTipo() == TipoTransaccion.INGRESO
                        && t.getFecha().getMonthValue() == mes
                        && t.getFecha().getYear() == anio)
                .mapToDouble(Transaccion::getMonto).sum();

        double gastos = repositorio.obtenerTodas().stream()
                .filter(t -> t.getTipo() == TipoTransaccion.GASTO
                        && t.getFecha().getMonthValue() == mes
                        && t.getFecha().getYear() == anio)
                .mapToDouble(Transaccion::getMonto).sum();

        return ingresos - gastos;
    }

    public void mostrarResumenMensual() {
        List<Transaccion> todas = repositorio.obtenerTodas();
        int mesActual = LocalDate.now().getMonthValue();
        int anioActual = LocalDate.now().getYear();

        double ingresos = 0.0, gastos = 0.0;
        System.out.println("\n--- Resumen del mes " + mesActual + "/" + anioActual + " ---");

        for (Transaccion t : todas) {
            if (t.getFecha().getMonthValue() == mesActual
                    && t.getFecha().getYear() == anioActual) {
                System.out.println(t);
                if (t.getTipo() == TipoTransaccion.INGRESO) ingresos += t.getMonto();
                else gastos += t.getMonto();
            }
        }

        System.out.println("--------------------------------");
        System.out.println("Total ingresos : " + MoneyFormatter.format(ingresos));
        System.out.println("Total gastos   : " + MoneyFormatter.format(gastos));
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
```

---

## Paso 5 — La interfaz de usuario (menú de consola)

El menú recibe el servicio por constructor. Usa `Scanner` para leer la entrada del usuario.

**`ui/ConsolaMenu.java`**

```java
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

    // métodos privados leerEntero(), leerDecimal(), leerTexto()
    // con manejo de NumberFormatException en bucle hasta que el input sea válido
}
```

Los métodos de lectura (`leerEntero`, `leerDecimal`) envuelven el parseo en un `while(true)` con `try/catch`. Si el usuario escribe algo que no es un número, el programa lo indica y vuelve a preguntar sin explotar.

```java
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
```

En `leerDecimal` se añade un `replace(',', '.')` para que el usuario pueda escribir `1,50` o `1.50` indistintamente.

---

## Paso 6 — El punto de entrada

`Main.java` construye las tres capas y arranca el menú. La cadena de dependencias es explícita y visible de un vistazo.

**`Main.java`**

```java
import repository.TransaccionRepository;
import service.BudgetService;
import ui.ConsolaMenu;

public class Main {
    public static void main(String[] args) {
        TransaccionRepository repositorio = new TransaccionRepository();
        BudgetService servicio = new BudgetService(repositorio);
        ConsolaMenu menu = new ConsolaMenu(servicio);
        menu.iniciar();
    }
}
```

El repositorio se crea primero, se inyecta en el servicio, y el servicio se inyecta en el menú. Cada capa recibe lo que necesita: no hay estáticos globales ni singletons.

---

## Paso 7 — Los tests (sin frameworks)

Los tests están escritos con **infraestructura propia** sin JUnit ni ninguna librería externa, para demostrar que se puede testear código limpio con Java puro.

### `TestAssertions.java` — aserciones básicas

```java
package test;

public final class TestAssertions {

    private TestAssertions() {}

    public static void assertTrue(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void assertEquals(int expected, int actual, String message) {
        if (expected != actual)
            throw new AssertionError(message + " | esperado=" + expected + ", actual=" + actual);
    }

    public static void assertEquals(double expected, double actual, double delta, String message) {
        if (Math.abs(expected - actual) > delta)
            throw new AssertionError(message + " | esperado=" + expected + ", actual=" + actual);
    }

    public static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null && actual == null) return;
        if (expected != null && expected.equals(actual)) return;
        throw new AssertionError(message + " | esperado=" + expected + ", actual=" + actual);
    }
}
```

### `TestUtils.java` — utilidades para CSV temporal

```java
package test;

import java.io.File;
import java.util.UUID;

public final class TestUtils {

    private TestUtils() {}

    public static String tempCsvPath() {
        return System.getProperty("java.io.tmpdir") + File.separator
                + "finanzapp-test-" + UUID.randomUUID() + ".csv";
    }

    public static void deleteIfExists(String path) {
        File file = new File(path);
        if (file.exists() && !file.delete())
            throw new RuntimeException("No se pudo eliminar: " + path);
    }
}
```

Usando un UUID en el nombre del archivo temporal se garantiza que dos tests no interfieren entre sí aunque corran en paralelo.

### `BudgetServiceTest.java`

```java
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
        TestUtils.deleteIfExists(csvPath);  // limpia siempre, aunque falle el test
    }
}
```

El bloque `finally` garantiza que el archivo temporal se elimina siempre, tanto si el test pasa como si falla.

### `TransaccionRepositoryTest.java`

Incluye un test de regresión importante: verifica que el repositorio tolera CSV con columnas extra (compatibilidad hacia atrás con ficheros de versiones anteriores).

```java
private static void testCargaSoportaCsvAntiguoConColumnasExtra() {
    String csvPath = TestUtils.tempCsvPath();
    try {
        writeLine(csvPath, "7,Linea antigua,42.5,GASTO,2026-01-15,comida,150.0");
        TransaccionRepository repo = new TransaccionRepository(csvPath);
        TestAssertions.assertEquals(1, repo.obtenerTodas().size(), "Debe cargar una linea valida");
    } finally {
        TestUtils.deleteIfExists(csvPath);
    }
}
```

### `TestRunner.java` — punto de entrada de los tests

```java
public class TestRunner {
    public static void main(String[] args) {
        int passed = 0, failed = 0;

        try { BudgetServiceTest.runAll(); System.out.println("[OK] BudgetServiceTest"); passed++; }
        catch (Throwable t) { System.out.println("[FAIL] BudgetServiceTest: " + t.getMessage()); failed++; }

        try { TransaccionRepositoryTest.runAll(); System.out.println("[OK] TransaccionRepositoryTest"); passed++; }
        catch (Throwable t) { System.out.println("[FAIL] TransaccionRepositoryTest: " + t.getMessage()); failed++; }

        System.out.println("\nResultado tests -> OK: " + passed + ", FAIL: " + failed);
        if (failed > 0) System.exit(1);
    }
}
```

El `System.exit(1)` al final es importante: si se ejecuta este runner en un CI/CD, el código de salida distinto de cero indica que algo ha fallado.

---

## Cómo compilar y ejecutar

### Compilar la aplicación

Desde la raíz del proyecto (donde está `finanz-core/`):

```bash
# Compilar todas las clases de la app
javac -d out -sourcepath finanz-core finanz-core/Main.java

# Ejecutar
java -cp out Main
```

### Compilar y ejecutar los tests

```bash
# Compilar tests (necesitan las clases de la app también)
javac -d out -sourcepath finanz-core finanz-core/test/TestRunner.java

# Ejecutar tests
java -cp out test.TestRunner
```

---

## Formato del CSV

El archivo `transacciones.csv` tiene este formato:

```
1,Sueldo,1500.0,INGRESO,2026-03-15
2,Alquiler,700.0,GASTO,2026-03-15
```

Columnas: `id`, `descripcion`, `monto`, `tipo (INGRESO|GASTO)`, `fecha (YYYY-MM-DD)`.

---

## Decisiones de diseño destacadas

- **Sin frameworks**: cero dependencias externas. Todo con `java.io`, `java.time` y `java.util`.
- **CSV como base de datos**: suficiente para un proyecto personal. Es legible por humanos y fácil de editar manualmente.
- **Inyección de dependencias manual**: el repositorio y el servicio se pasan por constructor. Esto permite sustituirlos en tests sin ningún framework de DI.
- **Copia defensiva en `obtenerTodas()`**: el repositorio nunca expone su lista interna. Cualquier modificación externa no tiene efecto sobre el estado real.
- **Tests sin JUnit**: demuestra que los principios de testing (aislamiento, arrancar/limpiar, aserciones) son independientes del framework.
