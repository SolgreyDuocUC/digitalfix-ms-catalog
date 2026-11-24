package df.digitalfix_ms_catalog.service.impl;

import df.digitalfix_ms_catalog.dto.b2c.ProductoCatalogoDTO;
import df.digitalfix_ms_catalog.dto.service.ActualizarCatalogServiceRequest;
import df.digitalfix_ms_catalog.dto.service.CatalogServiceRequest;
import df.digitalfix_ms_catalog.dto.service.CatalogServiceResponse;
import df.digitalfix_ms_catalog.exception.UnprocessableEntityException;
import df.digitalfix_ms_catalog.model.enums.UnidadMedida;
import df.digitalfix_ms_catalog.model.repuesto.Repuesto;
import df.digitalfix_ms_catalog.repository.MovimientoStockRepository;
import df.digitalfix_ms_catalog.repository.RepuestoRepository;
import df.digitalfix_ms_catalog.security.UsuarioActual;
import df.digitalfix_ms_catalog.service.CatalogServicesService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogServicesServiceImpl implements CatalogServicesService {

    private static final String PREFIJO_CODIGO = "SRV-";
    private static final String REFERENCIA_STOCK_INICIAL = "Stock inicial de catálogo";

    private final RepuestoRepository repuestoRepository;
    private final MovimientoStockRepository movimientoStockRepository;

    @Override
    public Page<CatalogServiceResponse> listar(String categoria, String search, boolean soloActivos, Pageable pageable) {
        List<Repuesto> todos = repuestoRepository.findAll();

        List<CatalogServiceResponse> filtrados = todos.stream()
                .filter(r -> !soloActivos || r.isActivo())
                .map(CatalogServiceResponse::from)
                .filter(s -> categoria == null || categoria.isBlank()
                        || s.categoria().equalsIgnoreCase(categoria.trim())
                        || s.categoria().toLowerCase().contains(categoria.trim().toLowerCase()))
                .filter(s -> search == null || search.isBlank()
                        || s.nombre().toLowerCase().contains(search.toLowerCase())
                        || (s.descripcion() != null && s.descripcion().toLowerCase().contains(search.toLowerCase()))
                        || s.codigo().toLowerCase().contains(search.toLowerCase()))
                .toList();

        int start = (int) pageable.getOffset();
        if (start >= filtrados.size()) {
            return new PageImpl<>(List.of(), pageable, filtrados.size());
        }
        int end = Math.min(start + pageable.getPageSize(), filtrados.size());
        return new PageImpl<>(filtrados.subList(start, end), pageable, filtrados.size());
    }

    @Override
    public CatalogServiceResponse buscarPorId(Long id) {
        return CatalogServiceResponse.from(obtenerRepuesto(id));
    }

    @Override
    @Transactional
    public CatalogServiceResponse crear(CatalogServiceRequest request) {
        if (request.tarifa() != null && request.tarifa().compareTo(BigDecimal.ZERO) < 0) {
            throw new UnprocessableEntityException("La tarifa no puede ser negativa");
        }
        if (request.stockInicial() != null && request.stockInicial() < 0) {
            throw new UnprocessableEntityException("El stock inicial no puede ser negativo");
        }

        UnidadMedida unidad = request.unidadMedida() != null ? request.unidadMedida() : UnidadMedida.UNIDAD;
        int stockMinimo = 0;

        Repuesto repuesto = new Repuesto(
                generarCodigo(),
                request.nombre(),
                request.descripcion(),
                unidad,
                request.tarifa(),
                stockMinimo
        );

        Repuesto guardado = repuestoRepository.saveAndFlush(repuesto);

        if (request.stockInicial() != null && request.stockInicial() > 0) {
            movimientoStockRepository.save(
                    guardado.registrarEntrada(request.stockInicial(), REFERENCIA_STOCK_INICIAL, UsuarioActual.identificador())
            );
            guardado = repuestoRepository.saveAndFlush(guardado);
        }

        String categoria = (request.categoria() != null && !request.categoria().isBlank())
                ? request.categoria()
                : ProductoCatalogoDTO.inferirCategoria(guardado);

        return CatalogServiceResponse.from(guardado, categoria);
    }

    @Override
    @Transactional
    public CatalogServiceResponse actualizar(Long id, ActualizarCatalogServiceRequest request) {
        Repuesto repuesto = obtenerRepuesto(id);

        // Validación estricta de regla de negocio: HTTP 422 si stock o tarifa es negativo
        if (request.stock() != null && request.stock() < 0) {
            throw new UnprocessableEntityException("El stock no puede ser negativo (valor ingresado: " + request.stock() + ")");
        }
        if (request.tarifa() != null && request.tarifa().compareTo(BigDecimal.ZERO) < 0) {
            throw new UnprocessableEntityException("La tarifa no puede ser negativa (valor ingresado: " + request.tarifa() + ")");
        }

        String nombre = request.nombre() != null ? request.nombre() : repuesto.getNombre();
        String descripcion = request.descripcion() != null ? request.descripcion() : repuesto.getDescripcion();
        UnidadMedida unidad = request.unidadMedida() != null ? request.unidadMedida() : repuesto.getUnidadMedida();
        BigDecimal tarifa = request.tarifa() != null ? request.tarifa() : repuesto.getPrecioUnitario();
        int stockMinimo = repuesto.getStockMinimo();

        repuesto.actualizarDatos(nombre, descripcion, unidad, tarifa, stockMinimo);

        // Ajuste de stock si se especifica
        if (request.stock() != null) {
            int stockActual = repuesto.getStockDisponible();
            int nuevoStock = request.stock();
            if (nuevoStock > stockActual) {
                int diff = nuevoStock - stockActual;
                movimientoStockRepository.save(
                        repuesto.registrarEntrada(diff, "Ajuste de inventario (+)", UsuarioActual.identificador())
                );
            } else if (nuevoStock < stockActual) {
                int diff = stockActual - nuevoStock;
                movimientoStockRepository.save(
                        repuesto.registrarSalida(diff, "Ajuste de inventario (-)", UsuarioActual.identificador())
                );
            }
        }

        if (request.activo() != null) {
            if (request.activo()) {
                repuesto.activar();
            } else {
                repuesto.desactivar();
            }
        }

        Repuesto actualizado = repuestoRepository.saveAndFlush(repuesto);
        String categoria = (request.categoria() != null && !request.categoria().isBlank())
                ? request.categoria()
                : ProductoCatalogoDTO.inferirCategoria(actualizado);

        return CatalogServiceResponse.from(actualizado, categoria);
    }

    @Override
    @Transactional
    public CatalogServiceResponse eliminarLogico(Long id) {
        Repuesto repuesto = obtenerRepuesto(id);
        // Borrado lógico: se marca estado = INACTIVO (activo = false)
        repuesto.desactivar();
        Repuesto guardado = repuestoRepository.saveAndFlush(repuesto);
        return CatalogServiceResponse.from(guardado);
    }

    private Repuesto obtenerRepuesto(Long id) {
        return repuestoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el servicio con ID " + id));
    }

    private String generarCodigo() {
        String codigo;
        do {
            codigo = PREFIJO_CODIGO + String.format("%04d", repuestoRepository.siguienteCorrelativoCodigo());
        } while (repuestoRepository.existsByCodigo(codigo));
        return codigo;
    }
}

