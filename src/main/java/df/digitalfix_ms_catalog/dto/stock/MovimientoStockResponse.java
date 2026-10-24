package df.digitalfix_ms_catalog.dto.stock;

import df.digitalfix_ms_catalog.model.repuesto.MovimientoStock;
import df.digitalfix_ms_catalog.model.repuesto.TipoMovimiento;

import java.time.LocalDateTime;

public record MovimientoStockResponse(
        Long idMovimiento,
        Long idRepuesto,
        String codigoRepuesto,
        TipoMovimiento tipo,
        int cantidad,
        int stockResultante,
        String referencia,
        String usuario,
        LocalDateTime fechaMovimiento
) {
    public static MovimientoStockResponse from(MovimientoStock m) {
        return new MovimientoStockResponse(
                m.getIdMovimiento(),
                m.getRepuesto().getIdRepuesto(),
                m.getRepuesto().getCodigo(),
                m.getTipo(),
                m.getCantidad(),
                m.getStockResultante(),
                m.getReferencia(),
                m.getUsuario(),
                m.getFechaMovimiento()
        );
    }
}
