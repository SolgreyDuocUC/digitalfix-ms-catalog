package df.digitalfix_ms_catalog.dto.service;

import df.digitalfix_ms_catalog.dto.b2c.ProductoCatalogoDTO;
import df.digitalfix_ms_catalog.model.repuesto.Repuesto;

import java.math.BigDecimal;

/**
 * Respuesta estándar del catálogo de servicios técnicos / repuestos.
 */
public record CatalogServiceResponse(
        Long id,
        String codigo,
        String nombre,
        String descripcion,
        String categoria,
        BigDecimal tarifa,
        Integer stock,
        String estado,
        boolean activo,
        String unidadMedida
) {
    public static CatalogServiceResponse from(Repuesto repuesto) {
        return from(repuesto, ProductoCatalogoDTO.inferirCategoria(repuesto));
    }

    public static CatalogServiceResponse from(Repuesto repuesto, String categoria) {
        if (repuesto == null) {
            return null;
        }
        String cat = (categoria != null && !categoria.isBlank())
                ? categoria
                : ProductoCatalogoDTO.inferirCategoria(repuesto);

        return new CatalogServiceResponse(
                repuesto.getIdRepuesto(),
                repuesto.getCodigo(),
                repuesto.getNombre(),
                repuesto.getDescripcion(),
                cat,
                repuesto.getPrecioUnitario(),
                repuesto.getStockDisponible(),
                repuesto.isActivo() ? "ACTIVO" : "INACTIVO",
                repuesto.isActivo(),
                repuesto.getUnidadMedida() != null ? repuesto.getUnidadMedida().name() : "UNIDAD"
        );
    }
}

