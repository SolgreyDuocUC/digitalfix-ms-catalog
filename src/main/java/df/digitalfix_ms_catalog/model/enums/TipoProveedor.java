package df.digitalfix_ms_catalog.model.enums;

/**
 * Distingue el tipo de proveedor en el catálogo de DigitalFix para cumplir con la Tercera Forma Normal (3FN).
 */
public enum TipoProveedor {
    /** Proveedor que suministra materiales, repuestos y componentes eléctricos físicos. */
    REPUESTOS,
    /** Proveedor subcontratista o especializado que presta servicios técnicos y mantención. */
    SERVICIOS,
    /** Proveedor integral que suministra tanto repuestos como servicios en terreno. */
    MIXTO
}

