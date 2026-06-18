package com.finanzapi.repository;

import com.finanzapi.model.TipoTransaccion;
import com.finanzapi.model.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * JPA repository for {@link com.finanzapi.model.Transaccion} entities.
 *
 * <p>Backed by the H2 {@code transacciones} table. Inherits standard CRUD and
 * pagination operations from {@link JpaRepository}. The two custom methods are
 * resolved by Spring Data as derived queries — no manual SQL required.</p>
 */
@Repository
public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    /**
     * Returns all transactions of the given type.
     *
     * <p>Used by {@link com.finanzapi.service.BudgetService#calcularSaldo} to
     * sum income and expenses separately without loading all rows.</p>
     *
     * @param tipo {@code INGRESO} or {@code GASTO}
     * @return transactions matching the requested type
     */
    List<Transaccion> findByTipo(TipoTransaccion tipo);

    /**
     * Returns all transactions whose date falls within the given range (inclusive).
     *
     * <p>Used for current-month balance and summary calculations in
     * {@link com.finanzapi.service.BudgetService}.</p>
     *
     * @param inicio start date (inclusive)
     * @param fin    end date (inclusive)
     * @return transactions within the date range
     */
    List<Transaccion> findByFechaBetween(LocalDate inicio, LocalDate fin);
}
