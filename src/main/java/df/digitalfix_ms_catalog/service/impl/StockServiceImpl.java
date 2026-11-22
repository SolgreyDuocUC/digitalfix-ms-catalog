package df.digitalfix_ms_catalog.service.impl;

import df.digitalfix_ms_catalog.dto.b2c.VerifyAndHoldRequest;
import df.digitalfix_ms_catalog.dto.b2c.VerifyAndHoldResponse;
import df.digitalfix_ms_catalog.dto.stock.EntradaStockRequest;
import df.digitalfix_ms_catalog.dto.stock.LoteStockRequest;
import df.digitalfix_ms_catalog.dto.stock.MovimientoStockResponse;
import df.digitalfix_ms_catalog.model.common.DatoInvalidoException;
import df.digitalfix_ms_catalog.model.enums.TipoMovimiento;
import df.digitalfix_ms_catalog.model.repuesto.MovimientoStock;
import df.digitalfix_ms_catalog.model.repuesto.Repuesto;
import df.digitalfix_ms_catalog.repository.MovimientoStockRepository;
import df.digitalfix_ms_catalog.repository.RepuestoRepository;
import df.digitalfix_ms_catalog.security.UsuarioActual;
import df.digitalfix_ms_catalog.service.RepuestoService;
import df.digitalfix_ms_catalog.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Movimientos de stock y gestión de reservas temporales (Hold) para el canal comercial B2C.
 * Cada operación de salida/entrada es una transacción (todo o nada) y queda en MOVIMIENTO_STOCK.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockServiceImpl implements StockService {

    private final RepuestoRepository repuestoRepository;
    private final MovimientoStockRepository movimientoStockRepository;
    private final RepuestoService repuestoService;

    // Almacén concurrente en memoria de reservas temporales (Hold) con tiempo de vida (TTL)
    private final Map<String, ReservaTemporal> reservasActivas = new ConcurrentHashMap<>();

    public record ReservaTemporal(
            String token,
            String referencia,
            Map<Long, Integer> items,
            Instant fechaCreacion,
            Instant fechaExpiracion
    ) {
        public boolean expirada() {
            return Instant.now().isAfter(fechaExpiracion);
        }
    }

    @Override
    @Transactional
    public MovimientoStockResponse registrarEntrada(Long idRepuesto, EntradaStockRequest request) {
        Repuesto repuesto = bloquear(Map.of(idRepuesto, request.cantidad())).get(idRepuesto);
        MovimientoStock movimiento = repuesto.registrarEntrada(request.cantidad(), request.referencia(), UsuarioActual.identificador());
        return MovimientoStockResponse.from(movimientoStockRepository.save(movimiento));
    }

    /** Consumo de una orden de trabajo (ms-workorders lo pide al asignarla). */
    @Override
    @Transactional
    public List<MovimientoStockResponse> registrarSalida(LoteStockRequest lote) {
        // Si la salida corresponde a un hold previo, lo liberamos para no duplicar descuento
        liberarHoldPorReferencia(lote.referencia());
        return aplicar(lote, TipoMovimiento.SALIDA);
    }

    /** Repuestos que vuelven a bodega (se quitaron de una orden ya asignada). */
    @Override
    @Transactional
    public List<MovimientoStockResponse> registrarDevolucion(LoteStockRequest lote) {
        return aplicar(lote, TipoMovimiento.DEVOLUCION);
    }

    @Override
    public List<MovimientoStockResponse> movimientos(Long idRepuesto) {
        repuestoService.obtener(idRepuesto); // 404 si no existe
        return movimientoStockRepository.findByRepuesto_IdRepuestoOrderByFechaMovimientoDesc(idRepuesto)
                .stream().map(MovimientoStockResponse::from).toList();
    }

    @Override
    public VerifyAndHoldResponse verificarYReservar(VerifyAndHoldRequest request) {
        limpiarReservasExpiradas();

        int minutos = (request.minutosReserva() != null && request.minutosReserva() > 0)
                ? request.minutosReserva()
                : 15; // default 15 minutos estándar para carritos web

        List<VerifyAndHoldResponse.DetalleHold> detalles = new ArrayList<>();
        boolean todoDisponible = true;
        Map<Long, Integer> itemsAReservar = new TreeMap<>();

        for (VerifyAndHoldRequest.ItemHold item : request.items()) {
            itemsAReservar.merge(item.idRepuesto(), item.cantidad(), Math::addExact);
        }

        for (Map.Entry<Long, Integer> entry : itemsAReservar.entrySet()) {
            Long idRepuesto = entry.getKey();
            int cantSolicitada = entry.getValue();

            Optional<Repuesto> opt = repuestoRepository.findById(idRepuesto);
            if (opt.isEmpty()) {
                detalles.add(new VerifyAndHoldResponse.DetalleHold(
                        idRepuesto, "No encontrado", cantSolicitada, 0, false
                ));
                todoDisponible = false;
            } else {
                Repuesto rep = opt.get();
                int reservado = stockReservado(idRepuesto);
                int stockDisponibleReal = Math.max(0, rep.getStockDisponible() - reservado);
                boolean suficiente = rep.isActivo() && stockDisponibleReal >= cantSolicitada;

                if (!suficiente) {
                    todoDisponible = false;
                }

                detalles.add(new VerifyAndHoldResponse.DetalleHold(
                        rep.getIdRepuesto(),
                        rep.getNombre(),
                        cantSolicitada,
                        stockDisponibleReal,
                        suficiente
                ));
            }
        }

        if (!todoDisponible) {
            return new VerifyAndHoldResponse(
                    false,
                    false,
                    null,
                    null,
                    "Stock insuficiente o repuesto inactivo para uno o más ítems solicitados",
                    detalles
            );
        }

        String token = "HOLD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Instant ahora = Instant.now();
        Instant expiraEn = ahora.plus(minutos, ChronoUnit.MINUTES);

        reservasActivas.put(token, new ReservaTemporal(
                token,
                request.referencia() != null ? request.referencia() : token,
                itemsAReservar,
                ahora,
                expiraEn
        ));

        log.info("Stock hold {} generado exitosamente por {} min para {} repuesto(s)",
                token, minutos, itemsAReservar.size());

        return new VerifyAndHoldResponse(
                true,
                true,
                token,
                expiraEn,
                "Stock verificado y reservado temporalmente por " + minutos + " minutos",
                detalles
        );
    }

    @Override
    public boolean liberarReserva(String tokenReserva) {
        if (tokenReserva == null) return false;
        ReservaTemporal eliminada = reservasActivas.remove(tokenReserva);
        if (eliminada != null) {
            log.info("Stock hold {} liberado exitosamente", tokenReserva);
            return true;
        }
        return false;
    }

    @Override
    public int stockReservado(Long idRepuesto) {
        limpiarReservasExpiradas();
        return reservasActivas.values().stream()
                .filter(r -> !r.expirada())
                .mapToInt(r -> r.items().getOrDefault(idRepuesto, 0))
                .sum();
    }

    private void limpiarReservasExpiradas() {
        reservasActivas.entrySet().removeIf(entry -> {
            boolean exp = entry.getValue().expirada();
            if (exp) {
                log.info("Stock hold {} expiró; su inventario queda liberado", entry.getKey());
            }
            return exp;
        });
    }

    private void liberarHoldPorReferencia(String referencia) {
        if (referencia == null) return;
        reservasActivas.remove(referencia);
        reservasActivas.entrySet().removeIf(e -> referencia.equalsIgnoreCase(e.getValue().referencia()));
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