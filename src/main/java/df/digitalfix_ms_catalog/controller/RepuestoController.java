package df.digitalfix_ms_catalog.controller;

import df.digitalfix_ms_catalog.dto.repuesto.ActualizarRepuestoRequest;
import df.digitalfix_ms_catalog.dto.repuesto.RepuestoRequest;
import df.digitalfix_ms_catalog.dto.repuesto.RepuestoResponse;
import df.digitalfix_ms_catalog.dto.stock.EntradaStockRequest;
import df.digitalfix_ms_catalog.dto.stock.MovimientoStockResponse;
import df.digitalfix_ms_catalog.service.RepuestoService;
import df.digitalfix_ms_catalog.service.StockService;
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
@RequestMapping("/api/catalog/repuestos")
@RequiredArgsConstructor
@Tag(name = "Repuestos", description = "Catálogo de repuestos, stock disponible e historial de movimientos")
public class RepuestoController {

    private final RepuestoService repuestoService;
    private final StockService stockService;

    @PostMapping
    @Operation(summary = "Crear un repuesto",
            description = "Registra un repuesto; el código (REP-0001...) lo genera el sistema. El stock inicial queda como movimiento ENTRADA.")
    public ResponseEntity<RepuestoResponse> crear(@Valid @RequestBody RepuestoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(repuestoService.crear(request));
    }

    @GetMapping
    @Operation(summary = "Listar repuestos",
            description = "Ordenados por nombre. soloActivos=true: los que se pueden agregar a una orden. bajoStock=true: los que están en o bajo su stock mínimo.")
    public ResponseEntity<List<RepuestoResponse>> listar(
            @RequestParam(defaultValue = "false") boolean soloActivos,
            @RequestParam(defaultValue = "false") boolean bajoStock) {
        return ResponseEntity.ok(repuestoService.listar(soloActivos, bajoStock));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar repuesto por ID", description = "Retorna el repuesto con su stock disponible.")
    public ResponseEntity<RepuestoResponse> buscarPorId(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(repuestoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar repuesto",
            description = "Modifica nombre, descripción, unidad, precio, stock mínimo o si está activo. El código y el stock no se editan aquí.")
    public ResponseEntity<RepuestoResponse> actualizar(
            @PathVariable @Positive Long id,
            @Valid @RequestBody ActualizarRepuestoRequest request) {
        return ResponseEntity.ok(repuestoService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar repuesto", description = "Elimina un repuesto sin movimientos de stock; si ya tiene, responde 409.")
    public ResponseEntity<Void> eliminar(@PathVariable @Positive Long id) {
        repuestoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/entradas")
    @Operation(summary = "Reponer stock", description = "Suma stock al repuesto (movimiento ENTRADA) con la referencia de la guía o factura.")
    public ResponseEntity<MovimientoStockResponse> registrarEntrada(
            @PathVariable @Positive Long id,
            @Valid @RequestBody EntradaStockRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(stockService.registrarEntrada(id, request));
    }

    @GetMapping("/{id}/movimientos")
    @Operation(summary = "Historial de stock", description = "Entradas, salidas y devoluciones del repuesto, de la más reciente a la más antigua.")
    public ResponseEntity<List<MovimientoStockResponse>> movimientos(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(stockService.movimientos(id));
    }
}
