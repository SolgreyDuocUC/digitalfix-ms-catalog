package df.digitalfix_ms_catalog.model.common;

/** Un dato no cumple el formato o las reglas del dominio (se responde 400). */
public class DatoInvalidoException extends IllegalArgumentException {

    public DatoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
