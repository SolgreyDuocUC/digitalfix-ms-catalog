package df.digitalfix_ms_catalog.model.enums;

/**
 * Tipo de proveedor. Debe coincidir con el CHECK CK_PROVEEDOR_TIPO de la tabla PROVEEDOR
 * (compartida con ms-workorders): solo admite 'SERVICIOS' y 'PRODUCTOS'.
 */
public enum TipoProveedor {
    /** Proveedor subcontratista o especializado que presta servicios técnicos y mantención. */
    SERVICIOS,
    /** Proveedor que suministra materiales, repuestos y componentes eléctricos físicos. */
    PRODUCTOS
}
