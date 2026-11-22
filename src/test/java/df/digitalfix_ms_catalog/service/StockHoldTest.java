package df.digitalfix_ms_catalog.service;

import df.digitalfix_ms_catalog.dto.b2c.VerifyAndHoldRequest;
import df.digitalfix_ms_catalog.dto.b2c.VerifyAndHoldResponse;
import df.digitalfix_ms_catalog.model.enums.UnidadMedida;
import df.digitalfix_ms_catalog.model.repuesto.Repuesto;
import df.digitalfix_ms_catalog.repository.MovimientoStockRepository;
import df.digitalfix_ms_catalog.repository.RepuestoRepository;
import df.digitalfix_ms_catalog.service.impl.StockServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StockHoldTest {

    @Mock
    private RepuestoRepository repuestoRepository;

    @Mock
    private MovimientoStockRepository movimientoStockRepository;

    @Mock
    private RepuestoService repuestoService;

    @InjectMocks
    private StockServiceImpl stockService;

    private Repuesto repuesto1;
    private Repuesto repuesto2;

    @BeforeEach
    void setUp() {
        repuesto1 = new Repuesto("REP-0001", "Interruptor termomagnético 2x16A",
                "Curva C", UnidadMedida.UNIDAD, new BigDecimal("8990.00"), 5);
        ReflectionTestUtils.setField(repuesto1, "idRepuesto", 1L);
        repuesto1.registrarEntrada(10, "Stock inicial", "admin");

        repuesto2 = new Repuesto("REP-0002", "Protector diferencial 2x25A 30mA",
                "Clase AC", UnidadMedida.UNIDAD, new BigDecimal("24990.00"), 2);
        ReflectionTestUtils.setField(repuesto2, "idRepuesto", 2L);
        repuesto2.registrarEntrada(3, "Stock inicial", "admin");

        when(repuestoRepository.findById(1L)).thenReturn(Optional.of(repuesto1));
        when(repuestoRepository.findById(2L)).thenReturn(Optional.of(repuesto2));
    }

    @Test
    void verificarYReservar_StockSuficiente_GeneraHold() {
        VerifyAndHoldRequest request = new VerifyAndHoldRequest("CART-100", 15,
                List.of(new VerifyAndHoldRequest.ItemHold(1L, 4), new VerifyAndHoldRequest.ItemHold(2L, 2)));

        VerifyAndHoldResponse response = stockService.verificarYReservar(request);

        assertThat(response.exitoso()).isTrue();
        assertThat(response.disponible()).isTrue();
        assertThat(response.tokenReserva()).startsWith("HOLD-");
        assertThat(response.expiraEn()).isNotNull();

        // El stock reservado ahora debe reflejarse
        assertThat(stockService.stockReservado(1L)).isEqualTo(4);
        assertThat(stockService.stockReservado(2L)).isEqualTo(2);
    }

    @Test
    void verificarYReservar_StockInsuficiente_NoGeneraHold() {
        // repuesto2 solo tiene 3 disponibles; solicitamos 5
        VerifyAndHoldRequest request = new VerifyAndHoldRequest("CART-101", 15,
                List.of(new VerifyAndHoldRequest.ItemHold(2L, 5)));

        VerifyAndHoldResponse response = stockService.verificarYReservar(request);

        assertThat(response.exitoso()).isFalse();
        assertThat(response.disponible()).isFalse();
        assertThat(response.tokenReserva()).isNull();
        assertThat(stockService.stockReservado(2L)).isEqualTo(0);
    }

    @Test
    void verificarYReservar_SegundoHoldSobrepasandoStock_Falla() {
        // Primer hold toma 2 de 3 disponibles
        VerifyAndHoldRequest hold1 = new VerifyAndHoldRequest("CART-1", 15,
                List.of(new VerifyAndHoldRequest.ItemHold(2L, 2)));
        VerifyAndHoldResponse resp1 = stockService.verificarYReservar(hold1);
        assertThat(resp1.exitoso()).isTrue();

        // Segundo hold solicita 2 más (quedaba solo 1 libre)
        VerifyAndHoldRequest hold2 = new VerifyAndHoldRequest("CART-2", 15,
                List.of(new VerifyAndHoldRequest.ItemHold(2L, 2)));
        VerifyAndHoldResponse resp2 = stockService.verificarYReservar(hold2);
        assertThat(resp2.exitoso()).isFalse();
    }

    @Test
    void liberarReserva_LiberaInventario() {
        VerifyAndHoldRequest hold = new VerifyAndHoldRequest("CART-1", 15,
                List.of(new VerifyAndHoldRequest.ItemHold(1L, 5)));
        VerifyAndHoldResponse resp = stockService.verificarYReservar(hold);
        assertThat(stockService.stockReservado(1L)).isEqualTo(5);

        boolean liberado = stockService.liberarReserva(resp.tokenReserva());
        assertThat(liberado).isTrue();
        assertThat(stockService.stockReservado(1L)).isEqualTo(0);
    }
}