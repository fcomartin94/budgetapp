package com.budgetapp.budgetapp.controller;

import com.budgetapp.budgetapp.controller.dto.SimpleTransaccionRequest;
import com.budgetapp.budgetapp.model.Transaccion;
import com.budgetapp.budgetapp.service.BudgetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class BudgetController {

    private final BudgetService servicio;

    public BudgetController(BudgetService servicio) {
        this.servicio = servicio;
    }

    @PostMapping("/transacciones")
    public ResponseEntity<Transaccion> registrar(@RequestBody Transaccion transaccion) {
        Transaccion guardada = servicio.registrar(transaccion);
        return ResponseEntity.ok(guardada);
    }

    @PostMapping("/transacciones/simple")
    public ResponseEntity<Transaccion> registrarSimple(@RequestBody SimpleTransaccionRequest request) {
        if (request.getDescripcion() == null || request.getDescripcion().isBlank() || request.getMonto() <= 0 || request.getTipo() == null) {
            return ResponseEntity.badRequest().build();
        }
        Transaccion guardada = servicio.registrarSimple(
                request.getDescripcion().trim(),
                request.getMonto(),
                request.getTipo()
        );
        return ResponseEntity.ok(guardada);
    }

    @GetMapping("/transacciones")
    public ResponseEntity<List<Transaccion>> obtenerTodas() {
        return ResponseEntity.ok(servicio.obtenerTodas());
    }

    @GetMapping("/transacciones/{id}")
    public ResponseEntity<Transaccion> obtenerPorId(@PathVariable Long id) {
        return servicio.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/transacciones/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (servicio.eliminar(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/saldo")
    public ResponseEntity<Map<String, Double>> saldo() {
        return ResponseEntity.ok(Map.of("saldo", servicio.calcularSaldo()));
    }

    @GetMapping("/saldo/mes-actual")
    public ResponseEntity<Map<String, Double>> saldoMesActual() {
        return ResponseEntity.ok(Map.of("saldo", servicio.calcularSaldoMesActual()));
    }

    @GetMapping("/resumen/mes-actual")
    public ResponseEntity<Map<String, Object>> resumenMensual() {
        return ResponseEntity.ok(servicio.obtenerResumenMensual());
    }
}