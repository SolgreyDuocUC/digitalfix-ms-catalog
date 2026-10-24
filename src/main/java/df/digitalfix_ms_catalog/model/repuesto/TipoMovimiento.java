package df.digitalfix_ms_catalog.model.repuesto;

/**
 * Motivo de un cambio de stock. Cada cambio queda en MOVIMIENTO_STOCK, así el stock actual
 * siempre se puede explicar (y auditar) a partir de su historial.
 */
public enum TipoMovimiento {
    /** Reposición del bodeguero o compra al proveedor: suma stock. */
    ENTRADA,
    /** Consumo de una orden de trabajo al ser asignada: resta stock. */
    SALIDA,
    /** Repuesto que vuelve a bodega (se quitó de una orden ya asignada): suma stock. */
    DEVOLUCION
}
