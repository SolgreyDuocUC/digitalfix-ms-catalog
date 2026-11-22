package df.digitalfix_ms_catalog.dto.b2c;

import java.time.Instant;
import java.util.List;

/**
 * Respuesta de verificación y retención temporal de stock.
 */
public record VerifyAndHoldResponse(
        boolean exitoso,
        boolean disponible,
        String tokenReserva,
        Instant expiraEn,
        String mensaje,
        List<DetalleHold> detalles
) {
    public record DetalleHold(
            Long idRepuesto,
            String nombre,
            Integer cantidadSolicitada,
            Integer stockDisponibleReal,
            boolean disponible
    ) {}
}