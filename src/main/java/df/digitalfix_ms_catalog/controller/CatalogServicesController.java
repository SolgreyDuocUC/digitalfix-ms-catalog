package df.digitalfix_ms_catalog.controller;

import df.digitalfix_ms_catalog.dto.service.ActualizarCatalogServiceRequest;
import df.digitalfix_ms_catalog.dto.service.CatalogServiceRequest;
import df.digitalfix_ms_catalog.dto.service.CatalogServiceResponse;
import df.digitalfix_ms_catalog.service.CatalogServicesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/catalog/services")
@RequiredArgsConstructor
@Tag(name = "Servicios de Catálogo", description = "CRUD de servicios técnicos y control de stock de repuestos")
public class CatalogServicesController {

    private final CatalogServicesService catalogServicesService;

    @GetMapping
    @Operation(summary = "Listar servicios paginados",
            description = "Retorna el listado paginado con precio, categoría y stock. Admite filtros opcionales por categoría, búsqueda y solo activos.")
    public ResponseEntity<Page<CatalogServiceResponse>> listar(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "false") boolean soloActivos,
            @PageableDefault(page = 0, size = 10, sort = "nombre", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(catalogServicesService.listar(categoria, search, soloActivos, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar servicio por ID", description = "Obtiene los detalles del servicio/repuesto según su ID.")
    public ResponseEntity<CatalogServiceResponse> buscarPorId(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(catalogServicesService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Crear servicio en catálogo",
            description = "Crea un servicio técnico con nombre, descripción, tarifa y stock inicial.")
    public ResponseEntity<CatalogServiceResponse> crear(@Valid @RequestBody CatalogServiceRequest request) {
        CatalogServiceResponse creado = catalogServicesService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar servicio y/o stock",
            description = "Actualiza tarifa y/o stock del servicio. Si el stock o la tarifa es negativo, responde 422 Unprocessable Entity.")
    public ResponseEntity<CatalogServiceResponse> actualizar(
            @PathVariable @Positive Long id,
            @RequestBody ActualizarCatalogServiceRequest request) {
        CatalogServiceResponse actualizado = catalogServicesService.actualizar(id, request);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Borrado lógico de servicio",
            description = "Realiza el borrado lógico del servicio cambiando su estado a INACTIVO.")
    public ResponseEntity<CatalogServiceResponse> eliminarLogico(@PathVariable @Positive Long id) {
        CatalogServiceResponse eliminado = catalogServicesService.eliminarLogico(id);
        return ResponseEntity.ok(eliminado);
    }
}

