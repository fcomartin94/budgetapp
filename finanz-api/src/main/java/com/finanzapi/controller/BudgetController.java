package com.finanzapi.controller;

import com.finanzapi.controller.dto.SimpleTransaccionRequest;
import com.finanzapi.model.Transaccion;
import com.finanzapi.service.BudgetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for personal budget management.
 *
 * <p>Base path: {@code /api}. Exposes endpoints for recording transactions,
 * querying history, computing balances, and retrieving monthly summaries.
 * All data is stored in an embedded H2 database via {@link BudgetService}.</p>
 *
 * <h3>Endpoints summary</h3>
 * <ul>
 *   <li>{@code POST /api/transacciones} — record a full {@link Transaccion} object</li>
 *   <li>{@code POST /api/transacciones/simple} — record with minimal validated input</li>
 *   <li>{@code GET  /api/transacciones} — list all transactions</li>
 *   <li>{@code GET  /api/transacciones/{id}} — get a transaction by ID</li>
 *   <li>{@code DELETE /api/transacciones/{id}} — delete a transaction</li>
 *   <li>{@code GET  /api/saldo} — all-time net balance</li>
 *   <li>{@code GET  /api/saldo/mes-actual} — current month balance</li>
 *   <li>{@code GET  /api/resumen/mes-actual} — full monthly summary</li>
 * </ul>
 */
@RestController
@RequestMapping("/api")
public class BudgetController {

    private final BudgetService servicio;

    /**
     * @param servicio the budget service handling all business logic
     */
    public BudgetController(BudgetService servicio) {
        this.servicio = servicio;
    }

    /**
     * Records a full transaction from the request body.
     *
     * <p>{@code POST /api/transacciones}</p>
     *
     * <p>The {@code fecha} field is overwritten to today by the service regardless
     * of what is passed in the body.</p>
     *
     * @param transaccion the transaction payload
     * @return {@code 200 OK} with the persisted transaction (auto-assigned ID and date)
     */
    @PostMapping("/transacciones")
    public ResponseEntity<Transaccion> registrar(@RequestBody Transaccion transaccion) {
        Transaccion guardada = servicio.registrar(transaccion);
        return ResponseEntity.ok(guardada);
    }

    /**
     * Records a transaction from a simplified DTO with server-side validation.
     *
     * <p>{@code POST /api/transacciones/simple}</p>
     *
     * <p>Validates that {@code descripcion} is not blank, {@code monto} is positive,
     * and {@code tipo} is non-null. Returns {@code 400} if any check fails.</p>
     *
     * @param request the simplified payload (descripcion, monto, tipo)
     * @return {@code 200 OK} with the persisted transaction; {@code 400} if input is invalid
     */
    @PostMapping("/transacciones/simple")
    public ResponseEntity<Transaccion> registrarSimple(@RequestBody SimpleTransaccionRequest request) {
        if (request.getDescripcion() == null || request.getDescripcion().isBlank()
                || request.getMonto() <= 0 || request.getTipo() == null) {
            return ResponseEntity.badRequest().build();
        }
        Transaccion guardada = servicio.registrarSimple(
                request.getDescripcion().trim(),
                request.getMonto(),
                request.getTipo()
        );
        return ResponseEntity.ok(guardada);
    }

    /**
     * Returns all recorded transactions.
     *
     * <p>{@code GET /api/transacciones}</p>
     *
     * @return {@code 200 OK} with the complete list of transactions
     */
    @GetMapping("/transacciones")
    public ResponseEntity<List<Transaccion>> obtenerTodas() {
        return ResponseEntity.ok(servicio.obtenerTodas());
    }

    /**
     * Returns a single transaction by its ID.
     *
     * <p>{@code GET /api/transacciones/{id}}</p>
     *
     * @param id the transaction primary key
     * @return {@code 200 OK} with the transaction; {@code 404} if not found
     */
    @GetMapping("/transacciones/{id}")
    public ResponseEntity<Transaccion> obtenerPorId(@PathVariable Long id) {
        return servicio.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Deletes a transaction by its ID.
     *
     * <p>{@code DELETE /api/transacciones/{id}}</p>
     *
     * @param id the transaction primary key
     * @return {@code 204 No Content} if deleted; {@code 404} if not found
     */
    @DeleteMapping("/transacciones/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (servicio.eliminar(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Returns the all-time net balance (total income minus total expenses).
     *
     * <p>{@code GET /api/saldo}</p>
     *
     * @return {@code 200 OK} with JSON body {@code {"saldo": <value>}}
     */
    @GetMapping("/saldo")
    public ResponseEntity<Map<String, Double>> saldo() {
        return ResponseEntity.ok(Map.of("saldo", servicio.calcularSaldo()));
    }

    /**
     * Returns the net balance for the current calendar month.
     *
     * <p>{@code GET /api/saldo/mes-actual}</p>
     *
     * @return {@code 200 OK} with JSON body {@code {"saldo": <value>}}
     */
    @GetMapping("/saldo/mes-actual")
    public ResponseEntity<Map<String, Double>> saldoMesActual() {
        return ResponseEntity.ok(Map.of("saldo", servicio.calcularSaldoMesActual()));
    }

    /**
     * Returns a full monthly summary for the current calendar month.
     *
     * <p>{@code GET /api/resumen/mes-actual}</p>
     *
     * <p>Response keys: {@code mes}, {@code anio}, {@code ingresos}, {@code gastos},
     * {@code saldo}, {@code transacciones}.</p>
     *
     * @return {@code 200 OK} with the summary map
     */
    @GetMapping("/resumen/mes-actual")
    public ResponseEntity<Map<String, Object>> resumenMensual() {
        return ResponseEntity.ok(servicio.obtenerResumenMensual());
    }
}
