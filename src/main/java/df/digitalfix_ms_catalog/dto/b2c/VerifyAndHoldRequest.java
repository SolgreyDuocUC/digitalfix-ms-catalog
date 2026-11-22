package df.digitalfix_ms_catalog.dto.b2c;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

/**
 * Solicitud de verificación y retención temporal de stock para el carrito de compras B2C.
 */
public record VerifyAndHoldRequest(
        String referencia,

        @PositiveOrZero(message = "Los minutos de reserva no pueden ser negativos")
        Integer minutosReserva,

        @NotEmpty(message = "La lista de ítems a reservar no puede estar vacía")
        List<@Valid ItemHold> items
) {
    public record ItemHold(
            @NotNull(message = "El idRepuesto es obligatorio")
            Long idRepuesto,

            @NotNull(message = "La cantidad es obligatoria")
            @Positive(message = "La cantidad debe ser mayor a cero")
            Integer cantidad
    ) {}
}