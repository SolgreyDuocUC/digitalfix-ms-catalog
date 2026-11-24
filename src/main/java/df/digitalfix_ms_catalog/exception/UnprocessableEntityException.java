package df.digitalfix_ms_catalog.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción para errores semánticos o de lógica de negocio (HTTP 422 Unprocessable Entity),
 * como intentar registrar un stock negativo o valores que no pasan las reglas de negocio.
 */
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class UnprocessableEntityException extends RuntimeException {

    public UnprocessableEntityException(String message) {
        super(message);
    }
}

