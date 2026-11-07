package df.digitalfix_ms_catalog.dto.b2c;

import java.util.List;

public record ValidarStockResponse(
        boolean disponible,
        String mensaje,
        List<DetalleDisponibilidad> detalles
) {
    public record DetalleDisponibilidad(
            Long idRepuesto,
            String nombre,
            Integer cantidadSolicitada,
            Integer stockActual,
            boolean suficiente
    ) {}
}

