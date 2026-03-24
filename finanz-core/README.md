# Finanz Core (`finanz-core/`)

Aplicacion de presupuesto personal en consola construida con Java puro, sin frameworks, con persistencia en CSV y arquitectura en capas.

Si buscas una version corta y orientada a portfolio/demo, revisa `README_PUBLIC.md`.

---

## Indice

- [1. Estado actual](#1-estado-actual)
- [2. Funcionalidades](#2-funcionalidades)
- [3. Arquitectura tecnica](#3-arquitectura-tecnica)
- [4. Modelo de dominio](#4-modelo-de-dominio)
- [5. Persistencia CSV](#5-persistencia-csv)
- [6. Manual de uso](#6-manual-de-uso)
- [7. Compilacion y ejecucion](#7-compilacion-y-ejecucion)
- [8. Consideraciones tecnicas](#8-consideraciones-tecnicas)
- [9. Relacion con Finanz API y Finanz App](#9-relacion-con-finanz-api-y-finanz-app)
- [10. Roadmap recomendado](#10-roadmap-recomendado)

---

## 1. Estado actual

Estado verificado:

- El codigo de `finanz-core` compila correctamente.
- Flujo CLI funcional: alta, consulta, resumen y borrado.
- Persistencia en archivo `transacciones.csv` activa.
- Tests unitarios base disponibles para `service` y `repository`.

Comprobacion:

```bash
javac finanz-core/**/*.java
```

---

## 2. Funcionalidades

- Registrar ingresos.
- Registrar gastos.
- Listar todas las transacciones.
- Mostrar resumen mensual.
- Mostrar saldo del mes actual.
- Eliminar transacciones por ID.

---

## 3. Arquitectura tecnica

```text
src/
├── Main.java
├── model/
│   ├── TipoTransaccion.java
│   └── Transaccion.java
├── repository/
│   └── TransaccionRepository.java
├── service/
│   └── BudgetService.java
├── test/
│   ├── BudgetServiceTest.java
│   ├── TransaccionRepositoryTest.java
│   ├── TestAssertions.java
│   ├── TestUtils.java
│   └── TestRunner.java
├── util/
│   └── MoneyFormatter.java
└── ui/
    └── ConsolaMenu.java
```

Responsabilidades:

- `Main`: composicion de dependencias.
- `ConsolaMenu`: interaccion con usuario (CLI).
- `BudgetService`: reglas de negocio.
- `TransaccionRepository`: persistencia y recuperacion CSV.
- `test`: pruebas unitarias sin dependencias externas (runner propio).
- `MoneyFormatter`: formato monetario comun en toda la app.
- `model`: entidades del dominio.

---

## 4. Modelo de dominio

### `TipoTransaccion`

- `INGRESO`
- `GASTO`

### `Transaccion`

- `id: int`
- `descripcion: String`
- `monto: double`
- `tipo: TipoTransaccion`
- `fecha: LocalDate`

---

## 5. Persistencia CSV

Archivo usado:

- `transacciones.csv` en el directorio de ejecucion.

Comportamiento:

- En cada alta y borrado se reescribe el archivo completo.
- Al iniciar, se cargan lineas existentes a memoria.
- Se mantiene contador incremental de IDs.

Detalle de robustez actual:

- Lineas mal formadas se ignoran.
- Se reemplazan comas en descripcion para no romper el parseo CSV simple.

---

## 6. Manual de uso

### Menu principal

Opciones disponibles:

- `1` Registrar ingreso
- `2` Registrar gasto
- `3` Mostrar todas las transacciones
- `4` Mostrar resumen mensual
- `5` Eliminar transaccion
- `6` Mostrar saldo del mes actual
- `0` Salir

### Flujo recomendado

1. Registrar ingresos.
2. Registrar gastos.
3. Revisar resumen mensual y saldo del mes actual.
4. Eliminar movimientos erroneos por ID.

### Validaciones de entrada

- Enteros y decimales se validan en bucle.
- En decimales se acepta `,` o `.`.

---

## 7. Compilacion y ejecucion

Desde la raiz del repositorio:

```bash
javac finanz-core/**/*.java && java -cp finanz-core Main
```

Si tu shell no expande `**`, compila desde IDE o ajusta el comando segun tu entorno.

### Ejecutar tests unitarios

Compilar y ejecutar:

```bash
javac finanz-core/**/*.java && java -cp finanz-core test.TestRunner
```

---

## 8. Consideraciones tecnicas

- No se usan frameworks ni gestores de build en esta version.
- Los tests unitarios se ejecutan con un runner propio (`test.TestRunner`).
- El parseo CSV es deliberadamente simple (no soporta campos con comas sin transformacion).
- No existe edicion de transacciones (solo alta y borrado).
- Los importes se muestran con formato monetario unificado (`es-ES`, EUR).

---

## 9. Relacion con Finanz API y Finanz App

- `finanz-core` es la base CLI del dominio.
- Finanz API (`finanz-api/`) expone el mismo dominio por HTTP.
- `finanz-app` ofrece UX movil simplificada para alta de movimientos.

Diferencia importante:

- En `finanz-core` y en `finanz-app` la entrada se basa en descripcion + monto + tipo.

---

## 10. Roadmap recomendado

- Migrar CSV a formato mas robusto o libreria dedicada.
- Añadir opcion de edicion de transacciones.
- Añadir filtros por fecha/tipo en listado CLI.
