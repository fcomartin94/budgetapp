package com.finanzapi;

import com.finanzapi.model.TipoTransaccion;
import com.finanzapi.model.Transaccion;
import com.finanzapi.repository.TransaccionRepository;
import com.finanzapi.service.BudgetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class FinanzApiApplicationTests {

	@Autowired
	private BudgetService servicio;

	@Autowired
	private TransaccionRepository repositorio;

	@BeforeEach
	void limpiarBaseDeDatos() {
		repositorio.deleteAll();
	}

	@Test
	void registrarTransaccionGuardaCorrectamente() {
		Transaccion t = new Transaccion(
				"Supermercado", 85.50,
				TipoTransaccion.GASTO,
				null
		);

		Transaccion guardada = servicio.registrar(t);

		assertNotNull(guardada.getId());
		assertEquals("Supermercado", guardada.getDescripcion());
		assertEquals(85.50, guardada.getMonto());
		assertNotNull(guardada.getFecha());
	}

	@Test
	void calcularSaldoConIngresosYGastos() {
		servicio.registrar(new Transaccion(
				"Nómina", 1800.0, TipoTransaccion.INGRESO,
				null
		));
		servicio.registrar(new Transaccion(
				"Alquiler", 750.0, TipoTransaccion.GASTO,
				null
		));

		double saldo = servicio.calcularSaldo();

		assertEquals(1050.0, saldo);
	}

	@Test
	void saldoEsCeroSinTransacciones() {
		double saldo = servicio.calcularSaldo();
		assertEquals(0.0, saldo);
	}

	@Test
	void eliminarTransaccionFunciona() {
		Transaccion guardada = servicio.registrar(new Transaccion(
				"Gym", 50.0, TipoTransaccion.GASTO,
				null
		));

		boolean eliminado = servicio.eliminar(guardada.getId());

		assertTrue(eliminado);
		assertTrue(servicio.obtenerPorId(guardada.getId()).isEmpty());
	}

	@Test
	void eliminarTransaccionInexistenteDevuelveFalse() {
		boolean eliminado = servicio.eliminar(999L);
		assertFalse(eliminado);
	}
}
