package df.digitalfix_ms_catalog.service;

import df.digitalfix_ms_catalog.dto.stock.EntradaStockRequest;
import df.digitalfix_ms_catalog.dto.stock.LoteStockRequest;
import df.digitalfix_ms_catalog.dto.stock.MovimientoStockResponse;
import df.digitalfix_ms_catalog.model.common.DatoInvalidoException;
import df.digitalfix_ms_catalog.model.repuesto.MovimientoStock;
import df.digitalfix_ms_catalog.model.repuesto.Repuesto;
import df.digitalfix_ms_catalog.model.repuesto.TipoMovimiento;
import df.digitalfix_ms_catalog.repository.MovimientoStockRepository;
import df.digitalfix_ms_catalog.repository.RepuestoRepository;
import df.digitalfix_ms_catalog.security.UsuarioActual;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Movimientos de stock. Cada operación es una transacción: un lote de salida con un solo repuesto
 * sin stock no descuenta ninguno (todo o nada), y cada cambio queda en MOVIMIENTO_STOCK.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockService {

    private final RepuestoRepository repuestoRepository;
    private final MovimientoStockRepository movimientoStockRepository;
    private final RepuestoService repuestoService;

    @Transactional
    public MovimientoStockResponse registrarEntrada(Long idRepuesto, EntradaStockRequest request) {
        Repuesto repuesto = bloquear(Map.of(idRepuesto, request.cantidad())).get(idRepuesto);
        MovimientoStock movimiento = repuesto.registrarEntrada(request.cantidad(), request.referencia(), UsuarioActual.identificador());
        return MovimientoStockResponse.from(movimientoStockRepository.save(movimiento));
    }

    /** Consumo de una orden de trabajo (ms-workorders lo pide al asignarla). */
    @Transactional
    public List<MovimientoStockResponse> registrarSalida(LoteStockRequest lote) {
        return aplicar(lote, TipoMovimiento.SALIDA);
    }

    /** Repuestos que vuelven a bodega (se quitaron de una orden ya asignada). */
    @Transactional
    public List<MovimientoStockResponse> registrarDevolucion(LoteStockRequest lote) {
        return aplicar(lote, TipoMovimiento.DEVOLUCION);
    }

    public List<MovimientoStockResponse> movimientos(Long idRepuesto) {
        repuestoService.obtener(idRepuesto); // 404 si no existe
        return movimientoStockRepository.findByRepuesto_IdRepuestoOrderByFechaMovimientoDesc(idRepuesto)
                .stream().map(MovimientoStockResponse::from).toList();
    }

    private List<MovimientoStockResponse> aplicar(LoteStockRequest lote, TipoMovimiento tipo) {
        Map<Long, Integer> cantidades = agrupar(lote.items());
        Map<Long, Repuesto> repuestos = bloquear(cantidades);
        String usuario = UsuarioActual.identificador();

        List<MovimientoStock> movimientos = new ArrayList<>();
        cantidades.forEach((id, cantidad) -> {
            Repuesto repuesto = repuestos.get(id);
            movimientos.add(tipo == TipoMovimiento.SALIDA
                    ? repuesto.registrarSalida(cantidad, lote.referencia(), usuario)
                    : repuesto.registrarDevolucion(cantidad, lote.referencia(), usuario));
        });
        List<MovimientoStock> guardados = movimientoStockRepository.saveAll(movimientos);
        log.info("Stock: {} de {} repuesto(s) para {} aplicada", tipo, guardados.size(), lote.referencia());
        return guardados.stream().map(MovimientoStockResponse::from).toList();
    }

    // El mismo repuesto puede venir dos veces (de dos proveedores en la orden): se suma. TreeMap
    // mantiene el orden por ID, el mismo en que se bloquean las filas.
    private Map<Long, Integer> agrupar(List<LoteStockRequest.Item> items) {
        Map<Long, Integer> cantidades = new TreeMap<>();
        items.forEach(item -> cantidades.merge(item.idRepuesto(), item.cantidad(), Math::addExact));
        return cantidades;
    }

    private Map<Long, Repuesto> bloquear(Map<Long, Integer> cantidades) {
        Map<Long, Repuesto> repuestos = new TreeMap<>();
        repuestoRepository.findAllByIdConBloqueo(cantidades.keySet())
                .forEach(r -> repuestos.put(r.getIdRepuesto(), r));
        List<Long> inexistentes = cantidades.keySet().stream().filter(id -> !repuestos.containsKey(id)).toList();
        if (!inexistentes.isEmpty()) {
            throw new DatoInvalidoException("No existen repuestos con id " + inexistentes);
        }
        return repuestos;
    }
}
