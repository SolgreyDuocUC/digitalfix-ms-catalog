package df.digitalfix_ms_catalog.controller;

import df.digitalfix_ms_catalog.dto.stock.LoteStockRequest;
import df.digitalfix_ms_catalog.dto.stock.MovimientoStockResponse;
import df.digitalfix_ms_catalog.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Lotes de stock que pide ms-workorders con el JWT del supervisor que opera la orden.
@RestController
@RequestMapping("/api/catalog/stock")
@RequiredArgsConstructor
@Tag(name = "Stock", description = "Salidas y devoluciones de stock por orden de trabajo (todo o nada)")
public class StockController {

    private final StockService stockService;

    @PostMapping("/salidas")
    @Operation(summary = "Descontar stock de un lote",
            description = "Descuenta todos los repuestos del lote o ninguno. Si alguno no tiene stock suficiente responde 409 con el detalle.")
    public ResponseEntity<List<MovimientoStockResponse>> registrarSalida(@Valid @RequestBody LoteStockRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(stockService.registrarSalida(request));
    }

    @PostMapping("/devoluciones")
    @Operation(summary = "Devolver stock de un lote", description = "Suma de vuelta los repuestos del lote (movimiento DEVOLUCION).")
    public ResponseEntity<List<MovimientoStockResponse>> registrarDevolucion(@Valid @RequestBody LoteStockRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(stockService.registrarDevolucion(request));
    }
}
