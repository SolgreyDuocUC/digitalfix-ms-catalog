package df.digitalfix_ms_catalog.exception;

import df.digitalfix_ms_catalog.dto.ErrorResponse;
import df.digitalfix_ms_catalog.model.common.DatoInvalidoException;
import df.digitalfix_ms_catalog.model.common.OperacionNoPermitidaException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.jspecify.annotations.Nullable;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

// Los mensajes que se devuelven son siempre propios (validaciones y reglas del dominio); los errores
// de base de datos o de parseo se responden con un texto genérico para no exponer detalles internos.
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        List<@Nullable String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .toList();
        return construir(HttpStatus.BAD_REQUEST, "Error de validacion de los datos", errors);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleHandlerMethodValidationException(HandlerMethodValidationException ex) {
        List<@Nullable String> errors = ex.getAllErrors()
                .stream()
                .map(MessageSourceResolvable::getDefaultMessage)
                .toList();
        return construir(HttpStatus.BAD_REQUEST, "Error de validacion de los datos", errors);
    }

    // Validaciones Bean Validation de las entidades al hacer flush.
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        List<@Nullable String> errors = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .toList();
        return construir(HttpStatus.BAD_REQUEST, "Error de validacion de los datos", errors);
    }

    @ExceptionHandler(DatoInvalidoException.class)
    public ResponseEntity<ErrorResponse> handleDatoInvalidoException(DatoInvalidoException ex) {
        return construir(HttpStatus.BAD_REQUEST, ex.getMessage(), List.of());
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> handleSolicitudIlegible(Exception ex) {
        return construir(HttpStatus.BAD_REQUEST, "La solicitud tiene un formato o valor no valido", List.of());
    }

    @ExceptionHandler(OperacionNoPermitidaException.class)
    public ResponseEntity<ErrorResponse> handleOperacionNoPermitidaException(OperacionNoPermitidaException ex) {
        return construir(HttpStatus.CONFLICT, ex.getMessage(), List.of());
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLockingFailureException(OptimisticLockingFailureException ex) {
        return construir(HttpStatus.CONFLICT,
                "El registro fue modificado por otra operacion; vuelva a consultarlo e intente de nuevo", List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        return construir(HttpStatus.CONFLICT, "La operacion viola una restriccion de integridad de los datos", List.of());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException ex) {
        return construir(HttpStatus.valueOf(ex.getStatusCode().value()), ex.getReason(), List.of());
    }

    private ResponseEntity<ErrorResponse> construir(HttpStatus status, @Nullable String message,
                                                    List<@Nullable String> errors) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(status.value())
                .message(message)
                .timestamp(LocalDateTime.now())
                .errors(errors)
                .build();
        return ResponseEntity.status(status).body(errorResponse);
    }
}
