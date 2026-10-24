package df.digitalfix_ms_catalog.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

// Mismo cuerpo de error que ms-workorders: ms-workorders lee "message" cuando el catálogo
// rechaza una salida de stock y lo devuelve tal cual al frontend.
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private int status;
    private String message;
    private LocalDateTime timestamp;
    private List<String> errors;

}
