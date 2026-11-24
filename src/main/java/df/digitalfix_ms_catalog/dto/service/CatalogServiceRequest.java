package df.digitalfix_ms_catalog.dto.service;

import df.digitalfix_ms_catalog.model.enums.UnidadMedida;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Petición para crear un servicio técnico / ítem de catálogo con stock inicial.
 */
public record CatalogServiceRequest(
        @NotBlank(message = "El nombre del servicio es obligatorio")
        @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
        String nombre,

        @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
        String descripcion,

        @NotNull(message = "La tarifa o precio es obligatoria")
        @DecimalMin(value = "0.00", message = "La tarifa no puede ser negativa")
        BigDecimal tarifa,

        Integer stockInicial,

        String categoria,

        UnidadMedida unidadMedida
) {
    public CatalogServiceRequest {
        if (unidadMedida == null) {
            unidadMedida = UnidadMedida.UNIDAD;
        }
        if (stockInicial == null) {
            stockInicial = 0;
        }
    }
}

