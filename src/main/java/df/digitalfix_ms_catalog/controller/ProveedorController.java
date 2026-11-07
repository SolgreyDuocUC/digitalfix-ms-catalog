package df.digitalfix_ms_catalog.controller;

import df.digitalfix_ms_catalog.dto.proveedor.ProveedorRequest;
import df.digitalfix_ms_catalog.dto.proveedor.ProveedorResponse;
import df.digitalfix_ms_catalog.model.enums.TipoProveedor;
import df.digitalfix_ms_catalog.service.ProveedorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog/proveedores")
@RequiredArgsConstructor
@Tag(name = "Proveedores", description = "Gestión de proveedores distinguiendo Repuestos, Servicios y Mixto (3FN)")
public class ProveedorController {

    private final ProveedorService proveedorService;

    @PostMapping
    @Operation(summary = "Registrar un proveedor",
            description = "Crea un proveedor identificando su tipo (REPUESTOS, SERVICIOS o MIXTO) y datos de contacto.")
    public ResponseEntity<ProveedorResponse> crear(@Valid @RequestBody ProveedorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(proveedorService.crear(request));
    }

    @GetMapping
    @Operation(summary = "Listar proveedores",
            description = "Permite filtrar por tipo de proveedor (REPUESTOS o SERVICIOS) y estado de actividad.")
    public ResponseEntity<List<ProveedorResponse>> listar(
            @RequestParam(required = false) TipoProveedor tipo,
            @RequestParam(defaultValue = "false") boolean soloActivos) {
        return ResponseEntity.ok(proveedorService.listar(tipo, soloActivos));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar proveedor por ID")
    public ResponseEntity<ProveedorResponse> buscarPorId(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(proveedorService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar datos de un proveedor")
    public ResponseEntity<ProveedorResponse> actualizar(
            @PathVariable @Positive Long id,
            @Valid @RequestBody ProveedorRequest request) {
        return ResponseEntity.ok(proveedorService.actualizar(id, request));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Activar o desactivar proveedor")
    public ResponseEntity<Void> cambiarEstado(
            @PathVariable @Positive Long id,
            @RequestParam boolean activo) {
        proveedorService.cambiarEstado(id, activo);
        return ResponseEntity.noContent().build();
    }
}

