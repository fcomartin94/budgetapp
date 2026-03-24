# 💰 Finanz Core

> Aplicación de gestión de presupuesto personal por consola, construida en **Java puro** sin frameworks externos. Diseñada con una arquitectura en capas real, persistencia en CSV y tests unitarios propios escritos desde cero.

---

## 🎯 ¿Qué hace esta app?

Permite registrar, consultar y eliminar transacciones financieras (ingresos y gastos) directamente desde la terminal. Los datos sobreviven entre sesiones gracias a persistencia en fichero CSV.

**Funcionalidades principales:**
- ✅ Registrar ingresos y gastos con descripción y fecha automática
- ✅ Listar todas las transacciones
- ✅ Ver resumen mensual con totales desglosados
- ✅ Consultar saldo global y saldo del mes en curso
- ✅ Eliminar transacciones por ID
- ✅ Persistencia automática en CSV (carga y guarda sin intervención del usuario)

---

## 🏗️ Arquitectura

La aplicación implementa un **patrón de arquitectura en capas** estricto, donde cada capa solo conoce a la inmediatamente inferior:

```
┌─────────────────────┐
│     UI (Consola)    │  ← ConsolaMenu.java
├─────────────────────┤
│      Service        │  ← BudgetService.java
├─────────────────────┤
│     Repository      │  ← TransaccionRepository.java
├─────────────────────┤
│    Persistencia     │  ← transacciones.csv
└─────────────────────┘
```

| Capa | Responsabilidad |
|------|----------------|
| `ui` | Interacción con el usuario, lectura de input, validación de formato |
| `service` | Lógica de negocio: cálculo de saldos, filtrado por mes, resúmenes |
| `repository` | Gestión del ciclo de vida de las transacciones y lectura/escritura en CSV |
| `model` | Entidades de dominio: `Transaccion`, `TipoTransaccion`, `Categoria` |
| `util` | Formateo de cantidades monetarias con `MoneyFormatter` |

---

## 🧰 Stack técnico

| Tecnología | Detalles |
|------------|----------|
| **Java** | Versión 17+ |
| **Sin frameworks** | Zero dependencias externas |
| **Persistencia** | Fichero CSV con lectura/escritura manual via `BufferedReader` / `BufferedWriter` |
| **Testing** | Framework de tests casero (sin JUnit) con assertions propias |
| **Build** | Compilación directa con `javac` |

---

## 🧪 Tests

Los tests están escritos **sin JUnit ni librerías externas**, lo que demuestra comprensión de cómo funcionan los frameworks de testing por dentro.

```
test/
├── TestRunner.java              # Punto de entrada de los tests
├── TestAssertions.java          # Implementación manual de assertEquals, assertTrue...
├── TestUtils.java               # Helpers: rutas temporales, limpieza de ficheros
├── BudgetServiceTest.java       # Tests de lógica de negocio
└── TransaccionRepositoryTest.java  # Tests de persistencia y carga de CSV
```

**Casos cubiertos:**
- Registro de transacciones y cálculo de saldo total
- Filtrado correcto por mes (el saldo mensual ignora meses anteriores)
- Guardar, buscar por ID y eliminar desde el repositorio
- Carga tolerante de CSV con columnas extra (retrocompatibilidad)

---

## 🚀 Cómo ejecutar

### Compilar

```bash
# Desde la raíz del proyecto (donde está Main.java)
javac -d out $(find . -name "*.java" ! -path "*/test/*")
```

### Ejecutar la app

```bash
java -cp out Main
```

### Ejecutar los tests

```bash
javac -d out $(find . -name "*.java")
java -cp out test.TestRunner
```

---

## 💡 Decisiones de diseño destacadas

- **Inyección de dependencias manual**: el `BudgetService` recibe el `TransaccionRepository` por constructor, no lo crea internamente. Esto facilita el testing y refleja el mismo patrón que usa Spring internamente.
- **Repositorio testeable**: el repositorio acepta una ruta de CSV configurable para poder usar ficheros temporales en los tests sin contaminar datos reales.
- **Separación UI/lógica**: `ConsolaMenu` nunca calcula nada; delega todo al servicio. Cambiar la interfaz a GUI o API no requeriría tocar la lógica de negocio.
- **Tolerancia en la carga de CSV**: el parser ignora columnas extra para mantener compatibilidad con versiones anteriores del formato.

---

## 📁 Estructura del proyecto

```
src/
├── Main.java
├── model/
│   ├── Transaccion.java
│   ├── TipoTransaccion.java
│   └── Categoria.java
├── service/
│   └── BudgetService.java
├── repository/
│   └── TransaccionRepository.java
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

---

> Este proyecto es la base sobre la que se construyó la versión con API REST usando Spring Boot. Ver → [budget-app-api](#)
