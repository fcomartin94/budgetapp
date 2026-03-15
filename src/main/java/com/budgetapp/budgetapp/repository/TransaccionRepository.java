package com.budgetapp.budgetapp.repository;

import com.budgetapp.budgetapp.model.TipoTransaccion;
import com.budgetapp.budgetapp.model.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    List<Transaccion> findByTipo(TipoTransaccion tipo);

    List<Transaccion> findByFechaBetween(LocalDate inicio, LocalDate fin);
}