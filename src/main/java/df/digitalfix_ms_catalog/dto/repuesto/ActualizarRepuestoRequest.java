package df.digitalfix_ms_catalog.dto.repuesto;

import df.digitalfix_ms_catalog.model.repuesto.UnidadMedida;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

// El código no cambia y el stock no se edita aquí: sólo se mueve con entradas, salidas y
// devoluciones, para que cada cambio quede registrado en MOVIMIENTO_STOCK.
public record ActualizarRepuestoRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
        String nombre,

        @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
        String descripcion,

        @NotNull(message = "La unidad de medida es obligatoria (UNIDAD, METRO, ROLLO, CAJA o KIT)")
        UnidadMedida unidadMedida,

        @NotNull(message = "El precio unitario es obligatorio")
        @DecimalMin(value = "0.00", message = "El precio unitario no puede ser negativo")
        @Digits(integer = 10, fraction = 2, message = "El precio unitario admite hasta 10 enteros y 2 decimales")
        BigDecimal precioUnitario,

        @NotNull(message = "El stock mínimo es obligatorio")
        @PositiveOrZero(message = "El stock mínimo no puede ser negativo")
        @Max(value = 9_999_999, message = "El stock mínimo excede el máximo permitido")
        Integer stockMinimo,

        @NotNull(message = "Indique si el repuesto está activo")
        Boolean activo
) {
}
