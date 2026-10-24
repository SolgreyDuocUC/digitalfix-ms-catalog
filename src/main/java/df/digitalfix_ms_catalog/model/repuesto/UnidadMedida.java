package df.digitalfix_ms_catalog.model.repuesto;

// Unidad en que se cuenta el stock de un repuesto. El stock siempre es un entero de esta unidad
// (DETALLE_OT_REPUESTO.CANTIDAD_UTILIZADA en ms-workorders también es entero).
public enum UnidadMedida {
    UNIDAD,
    METRO,
    ROLLO,
    CAJA,
    KIT
}
