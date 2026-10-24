package df.digitalfix_ms_catalog.dto.stock;

import jakarta.validation.constraints.*;

// Reposición manual de bodega para un repuesto.
public record EntradaStockRequest(
        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor a cero")
        @Max(value = 9_999_999, message = "La cantidad excede el máximo permitido")
        Integer cantidad,

        @NotBlank(message = "La referencia es obligatoria (ej. factura o guía del proveedor)")
        @Size(max = 60, message = "La referencia no puede exceder 60 caracteres")
        String referencia
) {
}
