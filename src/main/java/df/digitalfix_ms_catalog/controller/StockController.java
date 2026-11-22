package df.digitalfix_ms_catalog.controller;

import df.digitalfix_ms_catalog.dto.b2c.VerifyAndHoldRequest;
import df.digitalfix_ms_catalog.dto.b2c.VerifyAndHoldResponse;
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

// Lotes de stock que pide ms-workorders con el JWT del supervisor que opera la orden,
// y reservas temporales de stock para checkout B2C (Portal Cliente).
@RestController
@RequestMapping("/api/catalog/stock")
@RequiredArgsConstructor
@Tag(name = "Stock", description = "Salidas y devoluciones de stock por orden de trabajo, y retención temporal (Hold) para compras B2C")
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

    @PostMapping("/verify-and-hold")
    @Operation(summary = "Verificar y reservar stock temporalmente",
            description = "Verifica la disponibilidad física de repuestos y reserva temporalmente el stock para el carrito de compras B2C.")
    public ResponseEntity<VerifyAndHoldResponse> verifyAndHold(@Valid @RequestBody VerifyAndHoldRequest request) {
        return ResponseEntity.ok(stockService.verificarYReservar(request));
    }

    @PostMapping("/release-hold")
    @Operation(summary = "Liberar reserva temporal de stock",
            description = "Libera una reserva temporal previamente generada (ej: al vaciar el carrito o expirar la sesión).")
    public ResponseEntity<Void> releaseHold(@RequestParam String tokenReserva) {
        stockService.liberarReserva(tokenReserva);
        return ResponseEntity.noContent().build();
    }
}