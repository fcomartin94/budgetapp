package com.finanzapi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Health-check and discovery endpoint for the Finanz API.
 *
 * <p>Returns a JSON payload at {@code GET /} confirming the service is running
 * and listing the main entry points, consistent with the other modules in the
 * FinanzApp ecosystem.</p>
 */
@RestController
public class RootController {

    /**
     * Returns API metadata: service name, status, and available endpoint groups.
     *
     * @return a map serialized to JSON by Spring MVC
     */
    @GetMapping("/")
    public Map<String, Object> root() {
        return Map.of(
                "service", "Finanz API",
                "status", "ok",
                "endpoints", Map.of(
                        "transactions", "/api/transacciones",
                        "balance", "/api/saldo",
                        "currentMonthBalance", "/api/saldo/mes-actual",
                        "monthlySummary", "/api/resumen/mes-actual"
                ),
                "docs", "https://github.com/fcomartin94/finanz-core"
        );
    }
}
