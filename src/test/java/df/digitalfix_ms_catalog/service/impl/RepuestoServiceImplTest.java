package df.digitalfix_ms_catalog.service.impl;

import df.digitalfix_ms_catalog.dto.repuesto.RepuestoResponse;
import df.digitalfix_ms_catalog.model.enums.UnidadMedida;
import df.digitalfix_ms_catalog.model.repuesto.Repuesto;
import df.digitalfix_ms_catalog.repository.MovimientoStockRepository;
import df.digitalfix_ms_catalog.repository.RepuestoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepuestoServiceImplTest {

    @Mock
    private RepuestoRepository repuestoRepository;

    @Mock
    private MovimientoStockRepository movimientoStockRepository;

    @InjectMocks
    private RepuestoServiceImpl repuestoService;

    @Test
    void listarNormalizaSearchBlankYAplicaOrdenAscPorDefecto() {
        Repuesto repuesto = new Repuesto("REP-0001", "Pantalla OLED", null,
                UnidadMedida.UNIDAD, new BigDecimal("25000"), 2);
        Sort expectedSort = Sort.by(Sort.Direction.ASC, "nombre");
        when(repuestoRepository.buscarConFiltros(false, false, null, expectedSort))
                .thenReturn(List.of(repuesto));

        List<RepuestoResponse> resultado = repuestoService.listar(false, false, "   ", null);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().nombre()).isEqualTo("Pantalla OLED");
        verify(repuestoRepository).buscarConFiltros(false, false, null, expectedSort);
    }

    @Test
    void listarAplicaSearchRecortadoYOrdenDesc() {
        Sort expectedSort = Sort.by(Sort.Direction.DESC, "nombre");
        when(repuestoRepository.buscarConFiltros(true, true, "Pantalla", expectedSort))
                .thenReturn(List.of());

        List<RepuestoResponse> resultado = repuestoService.listar(true, true, "  Pantalla  ", "desc");

        assertThat(resultado).isEmpty();
        verify(repuestoRepository).buscarConFiltros(true, true, "Pantalla", expectedSort);
    }

    @Test
    void listarConSortInvalidoUsaAscPorDefecto() {
        Sort expectedSort = Sort.by(Sort.Direction.ASC, "nombre");
        when(repuestoRepository.buscarConFiltros(false, false, "Cable", expectedSort))
                .thenReturn(List.of());

        repuestoService.listar(false, false, "Cable", "invalido");

        verify(repuestoRepository).buscarConFiltros(false, false, "Cable", expectedSort);
    }
}
