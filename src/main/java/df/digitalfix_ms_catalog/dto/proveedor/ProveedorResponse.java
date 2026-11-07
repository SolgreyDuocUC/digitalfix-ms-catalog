package df.digitalfix_ms_catalog.dto.proveedor;

import df.digitalfix_ms_catalog.model.enums.TipoProveedor;
import df.digitalfix_ms_catalog.model.proveedor.Proveedor;

import java.time.LocalDateTime;

public record ProveedorResponse(
        Long idProveedor,
        String rut,
        String razonSocial,
        String nombreFantasia,
        TipoProveedor tipoProveedor,
        String emailContacto,
        String telefono,
        String direccion,
        boolean activo,
        LocalDateTime fechaCreacion
) {
    public static ProveedorResponse from(Proveedor p) {
        return new ProveedorResponse(
                p.getIdProveedor(),
                p.getRut(),
                p.getRazonSocial(),
                p.getNombreFantasia(),
                p.getTipoProveedor(),
                p.getEmailContacto(),
                p.getTelefono(),
                p.getDireccion(),
                p.isActivo(),
                p.getFechaCreacion()
        );
    }
}

