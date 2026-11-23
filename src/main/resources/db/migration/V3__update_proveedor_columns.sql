-- =====================================================================
-- V3 de ms-catalog: agrega ACTIVO, RAZON_SOCIAL y RUT a PROVEEDOR
-- =====================================================================

DECLARE
    v_count NUMBER;
BEGIN
    -- 1. ACTIVO: BOOLEAN nativo con DEFAULT
    SELECT COUNT(*) INTO v_count FROM user_tab_cols WHERE table_name = 'PROVEEDOR' AND column_name = 'ACTIVO';
    IF v_count = 0 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE PROVEEDOR ADD (ACTIVO BOOLEAN DEFAULT TRUE NOT NULL)';
    END IF;

    -- 2. RAZON_SOCIAL: agregar si no existe
    SELECT COUNT(*) INTO v_count FROM user_tab_cols WHERE table_name = 'PROVEEDOR' AND column_name = 'RAZON_SOCIAL';
    IF v_count = 0 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE PROVEEDOR ADD (RAZON_SOCIAL VARCHAR2(120 CHAR))';
    END IF;

    -- 3. RUT: agregar si no existe
    SELECT COUNT(*) INTO v_count FROM user_tab_cols WHERE table_name = 'PROVEEDOR' AND column_name = 'RUT';
    IF v_count = 0 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE PROVEEDOR ADD (RUT VARCHAR2(12 CHAR))';
    END IF;
END;
/

-- 4. Rellena filas existentes usando la PK real ID_PROVEEDOR
UPDATE PROVEEDOR
SET RAZON_SOCIAL = 'PROVEEDOR SIN RAZON SOCIAL ' || ID_PROVEEDOR
WHERE RAZON_SOCIAL IS NULL;

UPDATE PROVEEDOR
SET RUT = '00000000-0'
WHERE RUT IS NULL;

-- 5. Aplica restricción NOT NULL ya con datos poblados
ALTER TABLE PROVEEDOR MODIFY (
    RAZON_SOCIAL VARCHAR2(120 CHAR) NOT NULL,
    RUT VARCHAR2(12 CHAR) NOT NULL
);
