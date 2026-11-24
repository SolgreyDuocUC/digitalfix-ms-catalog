package df.digitalfix_ms_catalog.dto.service;

import df.digitalfix_ms_catalog.model.enums.UnidadMedida;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Petición para actualizar tarifa y/o stock de un servicio del catálogo.
 * Si se envía stock negativo o tarifa negativa, la lógica responderá 422 Unprocessable Entity.
 */
public record ActualizarCatalogServiceRequest(
        @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
        String nombre,

        @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
        String descripcion,

        BigDecimal tarifa,

        Integer stock,

        Boolean activo,

        String categoria,

        UnidadMedida unidadMedida
) {
}

