package df.digitalfix_ms_catalog.dto.b2c;

import df.digitalfix_ms_catalog.model.enums.UnidadMedida;
import df.digitalfix_ms_catalog.model.repuesto.Repuesto;

import java.math.BigDecimal;

/**
 * DTO optimizado para el portal cliente / e-commerce B2C.
 * Solo expone datos comerciales y de inventario seguro, desacoplando los
 * datos internos del taller (márgenes, costos, stock mínimo interno).
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
        return from(repuesto, 0);
    }

    public static ProductoCatalogoDTO from(Repuesto repuesto, int stockReservado) {
        int stockEfectivo = Math.max(0, repuesto.getStockDisponible() - stockReservado);
        return new ProductoCatalogoDTO(
                repuesto.getIdRepuesto(),
                repuesto.getCodigo(),
                repuesto.getNombre(),
                repuesto.getDescripcion(),
                inferirCategoria(repuesto),
                repuesto.getPrecioUnitario(),
                stockEfectivo,
                repuesto.isActivo() && stockEfectivo > 0,
                repuesto.getUnidadMedida().name(),
                true, // Todos los repuestos de DigitalFix son homologados bajo normativa SEC oficial
                determinarMarca(repuesto)
        );
    }

    public static String inferirCategoria(Repuesto repuesto) {
        if (repuesto == null || repuesto.getNombre() == null) {
            return "Repuestos en Stock";
        }
        String nombre = repuesto.getNombre().toLowerCase();
        String desc = repuesto.getDescripcion() != null ? repuesto.getDescripcion().toLowerCase() : "";

        if (nombre.contains("tablero") || desc.contains("tablero") || desc.contains("polos")) {
            return "Tableros & Fuerza";
        }
        if (nombre.contains("termomagnético") || nombre.contains("termomagnetico")
                || nombre.contains("diferencial") || nombre.contains("automático")
                || nombre.contains("automatico") || nombre.contains("disyuntor")
                || desc.contains("curva c") || desc.contains("protección") || desc.contains("proteccion")) {
            return "Protecciones Eléctricas";
        }
        if (nombre.contains("cable") || nombre.contains("alambre") || nombre.contains("conduit")
                || nombre.contains("canaleta") || nombre.contains("tubería") || nombre.contains("tuberia")
                || repuesto.getUnidadMedida() == UnidadMedida.ROLLO || repuesto.getUnidadMedida() == UnidadMedida.METRO) {
            return "Conductores & Canalización";
        }
        if (nombre.contains("enchufe") || nombre.contains("interruptor de luz") || nombre.contains("placa")
                || nombre.contains("foco") || nombre.contains("luminaria") || nombre.contains("ampolleta")
                || nombre.contains("socket")) {
            return "Artefactos & Iluminación";
        }
        return "Repuestos en Stock";
    }

    private static String determinarMarca(Repuesto repuesto) {
        if (repuesto.getProveedor() != null && repuesto.getProveedor().getNombreFantasia() != null) {
            return repuesto.getProveedor().getNombreFantasia();
        }
        return "Schneider / Legrand";
    }
}