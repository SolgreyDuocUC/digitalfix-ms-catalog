-- =====================================================================
-- V2 de ms-catalog: REPUESTO.ACTIVO pasa de NUMBER(1) a BOOLEAN
-- =====================================================================
-- La base es Oracle 23ai, que tiene BOOLEAN nativo, e Hibernate 7 mapea los
-- boolean de las entidades a ese tipo (igual que SERVICIO.ACTIVO en ms-workorders).
-- Oracle no convierte NUMBER -> BOOLEAN con MODIFY, así que se copia a una
-- columna nueva y se reemplaza.

ALTER TABLE REPUESTO DROP CONSTRAINT CK_REPUESTO_ACTIVO;

ALTER TABLE REPUESTO ADD (ACTIVO_TMP BOOLEAN DEFAULT TRUE NOT NULL);

UPDATE REPUESTO SET ACTIVO_TMP = CASE WHEN ACTIVO = 1 THEN TRUE ELSE FALSE END;

ALTER TABLE REPUESTO DROP COLUMN ACTIVO;

ALTER TABLE REPUESTO RENAME COLUMN ACTIVO_TMP TO ACTIVO;
