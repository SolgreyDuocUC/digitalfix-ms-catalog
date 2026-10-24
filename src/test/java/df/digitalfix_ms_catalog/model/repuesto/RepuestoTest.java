package df.digitalfix_ms_catalog.model.repuesto;

import df.digitalfix_ms_catalog.model.common.DatoInvalidoException;
import df.digitalfix_ms_catalog.model.common.OperacionNoPermitidaException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RepuestoTest {

    private Repuesto repuestoConStock(int stock) {
        Repuesto repuesto = new Repuesto("REP-0001", "interruptor 2x16A", null, UnidadMedida.UNIDAD,
                new BigDecimal("8990"), 5);
        if (stock > 0) {
            repuesto.registrarEntrada(stock, "Stock inicial", "test");
        }
        return repuesto;
    }

    @Test
    void salidaDescuentaStockYRegistraElMovimiento() {
        Repuesto repuesto = repuestoConStock(10);

        MovimientoStock movimiento = repuesto.registrarSalida(3, "OT-15", "supervisor@digitalfix.cl");

        assertThat(repuesto.getStockDisponible()).isEqualTo(7);
        assertThat(movimiento.getTipo()).isEqualTo(TipoMovimiento.SALIDA);
        assertThat(movimiento.getCantidad()).isEqualTo(3);
        assertThat(movimiento.getStockResultante()).isEqualTo(7);
        assertThat(movimiento.getReferencia()).isEqualTo("OT-15");
    }

    @Test
    void salidaSinStockSuficienteNoDescuentaNada() {
        Repuesto repuesto = repuestoConStock(2);

        assertThatThrownBy(() -> repuesto.registrarSalida(3, "OT-15", "test"))
                .isInstanceOf(StockInsuficienteException.class)
                .hasMessageContaining("se requieren 3 y hay 2");
        assertThat(repuesto.getStockDisponible()).isEqualTo(2);
    }

    @Test
    void repuestoDesactivadoNoSePuedeConsumirPeroSiDevolver() {
        Repuesto repuesto = repuestoConStock(10);
        repuesto.desactivar();

        assertThatThrownBy(() -> repuesto.registrarSalida(1, "OT-15", "test"))
                .isInstanceOf(OperacionNoPermitidaException.class);

        repuesto.registrarDevolucion(2, "OT-15", "test");
        assertThat(repuesto.getStockDisponible()).isEqualTo(12);
    }

    @Test
    void cantidadesNoPositivasSeRechazan() {
        Repuesto repuesto = repuestoConStock(10);

        assertThatThrownBy(() -> repuesto.registrarSalida(0, "OT-15", "test")).isInstanceOf(DatoInvalidoException.class);
        assertThatThrownBy(() -> repuesto.registrarEntrada(-1, "Guía 123", "test")).isInstanceOf(DatoInvalidoException.class);
    }

    @Test
    void bajoStockMinimoCuandoLlegaAlUmbral() {
        Repuesto repuesto = repuestoConStock(6);
        assertThat(repuesto.bajoStockMinimo()).isFalse();

        repuesto.registrarSalida(1, "OT-15", "test");
        assertThat(repuesto.bajoStockMinimo()).isTrue();
    }

    @Test
    void elNombreSeSanitizaYCapitaliza() {
        Repuesto repuesto = repuestoConStock(0);
        assertThat(repuesto.getNombre()).isEqualTo("Interruptor 2x16A");
        assertThat(repuesto.getStockDisponible()).isZero();
    }
}
