package df.digitalfix_ms_catalog.controller;

import df.digitalfix_ms_catalog.dto.b2c.ProductoCatalogoDTO;
import df.digitalfix_ms_catalog.dto.b2c.VerifyAndHoldRequest;
import df.digitalfix_ms_catalog.dto.b2c.VerifyAndHoldResponse;
import df.digitalfix_ms_catalog.model.enums.UnidadMedida;
import df.digitalfix_ms_catalog.model.repuesto.Repuesto;
import df.digitalfix_ms_catalog.repository.RepuestoRepository;
import df.digitalfix_ms_catalog.service.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PublicCatalogControllerTest {

    @Mock
    private RepuestoRepository repuestoRepository;

    @Mock
    private StockService stockService;

    @InjectMocks
    private PublicCatalogController controller;

    private Repuesto repuestoTablero;
    private Repuesto repuestoCable;
    private Repuesto repuestoSinStock;

    @BeforeEach
    void setUp() {
        repuestoTablero = new Repuesto("REP-0001", "Tablero de distribución 12 polos",
                "Sobrepuesto", UnidadMedida.UNIDAD, new BigDecimal("39990.00"), 5);
        ReflectionTestUtils.setField(repuestoTablero, "idRepuesto", 1L);
        repuestoTablero.registrarEntrada(10, "Stock inicial", "admin");

        repuestoCable = new Repuesto("REP-0002", "Cable THHN 2,5 mm²",
                "Conductor de cobre", UnidadMedida.ROLLO, new BigDecimal("64990.00"), 2);
        ReflectionTestUtils.setField(repuestoCable, "idRepuesto", 2L);
        repuestoCable.registrarEntrada(8, "Stock inicial", "admin");

        repuestoSinStock = new Repuesto("REP-0003", "Enchufe doble 10A",
                "Embutido", UnidadMedida.UNIDAD, new BigDecimal("3490.00"), 10);
        ReflectionTestUtils.setField(repuestoSinStock, "idRepuesto", 3L);
    }

    @Test
    void listarProductosB2C_RetornaTodosLosActivos() {
        when(repuestoRepository.findAll()).thenReturn(List.of(repuestoTablero, repuestoCable, repuestoSinStock));
        when(stockService.stockReservado(any())).thenReturn(0);

        ResponseEntity<List<ProductoCatalogoDTO>> response = controller.listarProductosB2C(null, null, false);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).hasSize(3);
    }

    @Test
    void listarProductosB2C_FiltraPorCategoria() {
        when(repuestoRepository.findAll()).thenReturn(List.of(repuestoTablero, repuestoCable, repuestoSinStock));
        when(stockService.stockReservado(any())).thenReturn(0);

        ResponseEntity<List<ProductoCatalogoDTO>> response = controller.listarProductosB2C(null, "Tableros & Fuerza", false);

        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).categoria()).isEqualTo("Tableros & Fuerza");
        assertThat(response.getBody().get(0).nombre()).contains("Tablero");
    }

    @Test
    void listarProductosB2C_FiltraSoloEnStock() {
        when(repuestoRepository.findAll()).thenReturn(List.of(repuestoTablero, repuestoCable, repuestoSinStock));
        when(stockService.stockReservado(any())).thenReturn(0);

        ResponseEntity<List<ProductoCatalogoDTO>> response = controller.listarProductosB2C(null, null, true);

        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody()).allMatch(ProductoCatalogoDTO::disponible);
    }

    @Test
    void listarCategorias_RetornaCategoriasUnicas() {
        when(repuestoRepository.findAll()).thenReturn(List.of(repuestoTablero, repuestoCable, repuestoSinStock));

        ResponseEntity<List<String>> response = controller.listarCategorias();

        assertThat(response.getBody()).isNotEmpty();
        assertThat(response.getBody()).contains("Tableros & Fuerza", "Conductores & Canalización", "Artefactos & Iluminación");
    }

    @Test
    void obtenerProductoB2C_RetornaDetalleDesacopladoConSEC() {
        when(repuestoRepository.findById(1L)).thenReturn(Optional.of(repuestoTablero));
        when(stockService.stockReservado(1L)).thenReturn(0);

        ResponseEntity<ProductoCatalogoDTO> response = controller.obtenerProductoB2C(1L);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        ProductoCatalogoDTO dto = response.getBody();
        assertThat(dto).isNotNull();
        assertThat(dto.certificacionSEC()).isTrue();
        assertThat(dto.codigo()).isEqualTo("REP-0001");
        assertThat(dto.categoria()).isEqualTo("Tableros & Fuerza");
    }

    @Test
    void verifyAndHold_DelegaEnStockService() {
        VerifyAndHoldRequest request = new VerifyAndHoldRequest("CART-1", 15,
                List.of(new VerifyAndHoldRequest.ItemHold(1L, 2)));
        VerifyAndHoldResponse mockResponse = new VerifyAndHoldResponse(true, true, "HOLD-TEST", null, "Ok", List.of());
        when(stockService.verificarYReservar(request)).thenReturn(mockResponse);

        ResponseEntity<VerifyAndHoldResponse> response = controller.verifyAndHold(request);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody().exitoso()).isTrue();
        assertThat(response.getBody().tokenReserva()).isEqualTo("HOLD-TEST");
    }
}