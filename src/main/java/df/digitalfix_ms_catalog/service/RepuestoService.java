package df.digitalfix_ms_catalog.service;

import df.digitalfix_ms_catalog.dto.repuesto.ActualizarRepuestoRequest;
import df.digitalfix_ms_catalog.dto.repuesto.RepuestoRequest;
import df.digitalfix_ms_catalog.dto.repuesto.RepuestoResponse;
import df.digitalfix_ms_catalog.model.common.OperacionNoPermitidaException;
import df.digitalfix_ms_catalog.model.repuesto.Repuesto;
import df.digitalfix_ms_catalog.repository.MovimientoStockRepository;
import df.digitalfix_ms_catalog.repository.RepuestoRepository;
import df.digitalfix_ms_catalog.security.UsuarioActual;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

// CRUD del catálogo de repuestos. El stock no se edita aquí: lo mueve StockService.
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RepuestoService {

    private static final String PREFIJO_CODIGO = "REP-";
    private static final String REFERENCIA_STOCK_INICIAL = "Stock inicial";

    private final RepuestoRepository repuestoRepository;
    private final MovimientoStockRepository movimientoStockRepository;

    @Transactional
    public RepuestoResponse crear(RepuestoRequest request) {
        Repuesto repuesto = new Repuesto(generarCodigo(), request.nombre(), request.descripcion(),
                request.unidadMedida(), request.precioUnitario(), request.stockMinimo());
        Repuesto guardado = repuestoRepository.saveAndFlush(repuesto);
        if (request.stockInicial() != null && request.stockInicial() > 0) {
            movimientoStockRepository.save(
                    guardado.registrarEntrada(request.stockInicial(), REFERENCIA_STOCK_INICIAL, UsuarioActual.identificador()));
        }
        return RepuestoResponse.from(guardado);
    }

    /** {@code soloActivos}: lo que se puede agregar a una orden. {@code bajoStock}: lo que hay que reponer. */
    public List<RepuestoResponse> listar(boolean soloActivos, boolean bajoStock) {
        List<Repuesto> repuestos;
        if (bajoStock) {
            repuestos = repuestoRepository.findBajoStockMinimo().stream()
                    .filter(r -> !soloActivos || r.isActivo())
                    .toList();
        } else if (soloActivos) {
            repuestos = repuestoRepository.findByActivoTrueOrderByNombreAsc();
        } else {
            repuestos = repuestoRepository.findAllByOrderByNombreAsc();
        }
        return repuestos.stream().map(RepuestoResponse::from).toList();
    }

    public RepuestoResponse buscarPorId(Long id) {
        return RepuestoResponse.from(obtener(id));
    }

    @Transactional
    public RepuestoResponse actualizar(Long id, ActualizarRepuestoRequest request) {
        Repuesto repuesto = obtener(id);
        repuesto.actualizarDatos(request.nombre(), request.descripcion(), request.unidadMedida(),
                request.precioUnitario(), request.stockMinimo());
        if (request.activo()) {
            repuesto.activar();
        } else {
            repuesto.desactivar();
        }
        return RepuestoResponse.from(repuestoRepository.saveAndFlush(repuesto));
    }

    /** Un repuesto con historial de stock no se borra (perdería la trazabilidad): se desactiva. */
    @Transactional
    public void eliminar(Long id) {
        Repuesto repuesto = obtener(id);
        if (movimientoStockRepository.existsByRepuesto_IdRepuesto(id)) {
            throw new OperacionNoPermitidaException("El repuesto " + repuesto.getCodigo()
                    + " tiene movimientos de stock; desactívelo en vez de eliminarlo");
        }
        repuestoRepository.delete(repuesto);
    }

    Repuesto obtener(Long id) {
        return repuestoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe un repuesto con id " + id));
    }

    /** REP-0001, REP-0002, ...; se salta un código si ya existe (p. ej. uno cargado a mano). */
    private String generarCodigo() {
        String codigo;
        do {
            codigo = PREFIJO_CODIGO + String.format("%04d", repuestoRepository.siguienteCorrelativoCodigo());
        } while (repuestoRepository.existsByCodigo(codigo));
        return codigo;
    }
}
