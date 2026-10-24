package df.digitalfix_ms_catalog.dto.stock;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

/**
 * Salida o devolución de varios repuestos en una sola operación atómica: o se aplican todos los
 * ítems o ninguno. La usa ms-workorders al asignar una orden (referencia "OT-15").
 */
public record LoteStockRequest(
        @NotBlank(message = "La referencia es obligatoria (ej. OT-15)")
        @Size(max = 60, message = "La referencia no puede exceder 60 caracteres")
        String referencia,

        @NotEmpty(message = "El lote debe tener al menos un repuesto")
        @Size(max = 100, message = "El lote no puede tener más de 100 repuestos")
        List<@Valid @NotNull(message = "Los ítems del lote no pueden ser nulos") Item> items
) {
    public record Item(
            @NotNull(message = "El repuesto es obligatorio")
            @Positive(message = "El ID del repuesto no es válido")
            Long idRepuesto,

            @NotNull(message = "La cantidad es obligatoria")
            @Positive(message = "La cantidad debe ser mayor a cero")
            @Max(value = 9_999_999, message = "La cantidad excede el máximo permitido")
            Integer cantidad
    ) {
    }
}
