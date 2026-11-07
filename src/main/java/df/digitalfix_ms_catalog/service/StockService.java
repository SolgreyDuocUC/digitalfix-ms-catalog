package df.digitalfix_ms_catalog.service;

import df.digitalfix_ms_catalog.dto.stock.EntradaStockRequest;
import df.digitalfix_ms_catalog.dto.stock.LoteStockRequest;
import df.digitalfix_ms_catalog.dto.stock.MovimientoStockResponse;

import java.util.List;

public interface StockService {

    MovimientoStockResponse registrarEntrada(Long idRepuesto, EntradaStockRequest request);

    List<MovimientoStockResponse> registrarSalida(LoteStockRequest lote);

    List<MovimientoStockResponse> registrarDevolucion(LoteStockRequest lote);

    List<MovimientoStockResponse> movimientos(Long idRepuesto);
}
