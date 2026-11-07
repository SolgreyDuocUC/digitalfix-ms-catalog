package df.digitalfix_ms_catalog.dto.proveedor;

import df.digitalfix_ms_catalog.model.enums.TipoProveedor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProveedorRequest(
        @NotBlank(message = "El RUT del proveedor es obligatorio")
        @Size(max = 12, message = "El RUT no puede exceder 12 caracteres")
        String rut,

        @NotBlank(message = "La razón social es obligatoria")
        @Size(max = 120, message = "La razón social no puede exceder 120 caracteres")
        String razonSocial,

        @Size(max = 100, message = "El nombre de fantasía no puede exceder 100 caracteres")
        String nombreFantasia,

        @NotNull(message = "El tipo de proveedor es obligatorio (REPUESTOS, SERVICIOS, MIXTO)")
        TipoProveedor tipoProveedor,

        @Email(message = "El email debe ser válido")
        @Size(max = 100, message = "El email no puede exceder 100 caracteres")
        String emailContacto,

        @Size(max = 30, message = "El teléfono no puede exceder 30 caracteres")
        String telefono,

        @Size(max = 150, message = "La dirección no puede exceder 150 caracteres")
        String direccion
) {}

