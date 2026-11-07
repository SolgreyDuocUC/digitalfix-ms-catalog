package df.digitalfix_ms_catalog.dto.b2c;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record ValidarStockRequest(
        @NotEmpty(message = "La lista de ítems a verificar no puede estar vacía")
        List<ItemStock> items
) {
    public record ItemStock(
            @NotNull(message = "El idRepuesto es obligatorio")
            Long idRepuesto,

            @NotNull(message = "La cantidad es obligatoria")
            @Positive(message = "La cantidad debe ser mayor a cero")
            Integer cantidad
    ) {}
}

