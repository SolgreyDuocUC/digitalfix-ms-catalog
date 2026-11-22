package df.digitalfix_ms_catalog.service;

import df.digitalfix_ms_catalog.dto.b2c.VerifyAndHoldRequest;
import df.digitalfix_ms_catalog.dto.b2c.VerifyAndHoldResponse;
import df.digitalfix_ms_catalog.dto.stock.EntradaStockRequest;
import df.digitalfix_ms_catalog.dto.stock.LoteStockRequest;
import df.digitalfix_ms_catalog.dto.stock.MovimientoStockResponse;

import java.util.List;

/**
 * Contrato de servicio para operaciones e inventario de stock.
 */
public interface StockService {

    MovimientoStockResponse registrarEntrada(Long idRepuesto, EntradaStockRequest request);

    List<MovimientoStockResponse> registrarSalida(LoteStockRequest lote);

    List<MovimientoStockResponse> registrarDevolucion(LoteStockRequest lote);

    List<MovimientoStockResponse> movimientos(Long idRepuesto);

    /**
     * Valida la existencia y disponibilidad física del stock de repuestos y genera
     * una retención (hold) temporal de inventario para el flujo de compra/checkout B2C.
     */
    VerifyAndHoldResponse verificarYReservar(VerifyAndHoldRequest request);

    /**
     * Libera una retención temporal de stock generada previamente.
     */
    boolean liberarReserva(String tokenReserva);

    /**
     * Retorna la cantidad de unidades actualmente retenidas (en carritos activos) para un repuesto.
     */
    int stockReservado(Long idRepuesto);
}