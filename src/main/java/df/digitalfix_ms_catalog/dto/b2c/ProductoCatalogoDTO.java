package df.digitalfix_ms_catalog.dto.b2c;

import df.digitalfix_ms_catalog.model.repuesto.Repuesto;

import java.math.BigDecimal;

/**
 * DTO optimizado para el portal cliente / e-commerce B2C.
 * Solo expone datos comerciales y de inventario seguro, sin revelar
 * datos internos del taller o márgenes de costo.
 */
public record ProductoCatalogoDTO(
        Long id,
        String codigo,
        String nombre,
        String descripcion,
        String categoria,
        BigDecimal precio,
        Integer stock,
        boolean disponible,
        String unidadMedida,
        boolean certificacionSEC,
        String marca
) {
    public static ProductoCatalogoDTO from(Repuesto repuesto) {
        return new ProductoCatalogoDTO(
                repuesto.getIdRepuesto(),
                repuesto.getCodigo(),
                repuesto.getNombre(),
                repuesto.getDescripcion(),
                "Repuestos",
                repuesto.getPrecioUnitario(),
                repuesto.getStockDisponible(),
                repuesto.isActivo() && repuesto.getStockDisponible() > 0,
                repuesto.getUnidadMedida().name(),
                true, // Todos los repuestos de DigitalFix son homologados SEC
                "Schneider / Legrand"
        );
    }
}

