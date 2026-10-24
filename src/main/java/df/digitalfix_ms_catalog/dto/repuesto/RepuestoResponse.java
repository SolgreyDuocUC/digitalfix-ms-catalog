package df.digitalfix_ms_catalog.dto.repuesto;

import df.digitalfix_ms_catalog.model.repuesto.Repuesto;
import df.digitalfix_ms_catalog.model.repuesto.UnidadMedida;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RepuestoResponse(
        Long idRepuesto,
        String codigo,
        String nombre,
        String descripcion,
        UnidadMedida unidadMedida,
        BigDecimal precioUnitario,
        int stockDisponible,
        int stockMinimo,
        boolean bajoStockMinimo,
        boolean activo,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion
) {
    public static RepuestoResponse from(Repuesto r) {
        return new RepuestoResponse(
                r.getIdRepuesto(),
                r.getCodigo(),
                r.getNombre(),
                r.getDescripcion(),
                r.getUnidadMedida(),
                r.getPrecioUnitario(),
                r.getStockDisponible(),
                r.getStockMinimo(),
                r.bajoStockMinimo(),
                r.isActivo(),
                r.getFechaCreacion(),
                r.getFechaActualizacion()
        );
    }
}
