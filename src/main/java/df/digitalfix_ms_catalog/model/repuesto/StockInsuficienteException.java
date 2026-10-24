package df.digitalfix_ms_catalog.model.repuesto;

import df.digitalfix_ms_catalog.model.common.OperacionNoPermitidaException;

/** No hay stock para cubrir una salida (se responde 409 y no se descuenta nada del lote). */
public class StockInsuficienteException extends OperacionNoPermitidaException {

    public StockInsuficienteException(String mensaje) {
        super(mensaje);
    }
}
