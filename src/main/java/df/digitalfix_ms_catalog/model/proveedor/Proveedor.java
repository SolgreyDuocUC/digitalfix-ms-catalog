package df.digitalfix_ms_catalog.model.proveedor;

import df.digitalfix_ms_catalog.model.common.EntidadAuditable;
import df.digitalfix_ms_catalog.model.common.SanitizadorTexto;
import df.digitalfix_ms_catalog.model.enums.TipoProveedor;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * Entidad de Proveedor (tabla PROVEEDOR).
 * Cumple con 3FN al desacoplar la información del proveedor de los repuestos y servicios,
 * distinguiendo su especialidad (REPUESTOS, SERVICIOS o MIXTO).
 */
@Entity
@Table(name = "PROVEEDOR",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_PROVEEDOR_RUT", columnNames = "RUT"),
                @UniqueConstraint(name = "UK_PROVEEDOR_RAZON_SOCIAL", columnNames = "RAZON_SOCIAL")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Proveedor extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PROVEEDOR", nullable = false, updatable = false)
    private Long idProveedor;

    @NotBlank(message = "El RUT del proveedor es obligatorio")
    @Size(max = 12, message = "El RUT no puede exceder 12 caracteres")
    @Column(name = "RUT", nullable = false, length = 12)
    private String rut;

    @NotBlank(message = "La razón social es obligatoria")
    @Size(max = 120, message = "La razón social no puede exceder 120 caracteres")
    @Column(name = "RAZON_SOCIAL", nullable = false, length = 120)
    private String razonSocial;

    @Size(max = 100, message = "El nombre de fantasía no puede exceder 100 caracteres")
    @Column(name = "NOMBRE_FANTASIA", length = 100)
    private String nombreFantasia;

    @NotNull(message = "El tipo de proveedor es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO_PROVEEDOR", nullable = false, length = 20)
    private TipoProveedor tipoProveedor;

    @Email(message = "El email de contacto debe tener un formato válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    @Column(name = "EMAIL_CONTACTO", length = 100)
    private String emailContacto;

    @Size(max = 30, message = "El teléfono no puede exceder 30 caracteres")
    @Column(name = "TELEFONO", length = 30)
    private String telefono;

    @Size(max = 150, message = "La dirección no puede exceder 150 caracteres")
    @Column(name = "DIRECCION", length = 150)
    private String direccion;

    @Column(name = "ACTIVO", nullable = false)
    private boolean activo = true;

    public Proveedor(String rut, String razonSocial, String nombreFantasia,
                     TipoProveedor tipoProveedor, String emailContacto, String telefono, String direccion) {
        this.rut = Objects.requireNonNull(rut, "El RUT es obligatorio").trim().toUpperCase();
        this.razonSocial = SanitizadorTexto.capitalizarPrimeraLetra(razonSocial);
        this.nombreFantasia = SanitizadorTexto.capitalizarPrimeraLetra(nombreFantasia);
        this.tipoProveedor = Objects.requireNonNull(tipoProveedor, "El tipo de proveedor es obligatorio");
        this.emailContacto = emailContacto != null ? emailContacto.trim().toLowerCase() : null;
        this.telefono = telefono != null ? telefono.trim() : null;
        this.direccion = SanitizadorTexto.textoMultilinea(direccion);
        this.activo = true;
    }

    public void actualizarDatos(String razonSocial, String nombreFantasia, TipoProveedor tipoProveedor,
                                String emailContacto, String telefono, String direccion) {
        this.razonSocial = SanitizadorTexto.capitalizarPrimeraLetra(razonSocial);
        this.nombreFantasia = SanitizadorTexto.capitalizarPrimeraLetra(nombreFantasia);
        this.tipoProveedor = Objects.requireNonNull(tipoProveedor, "El tipo de proveedor es obligatorio");
        this.emailContacto = emailContacto != null ? emailContacto.trim().toLowerCase() : null;
        this.telefono = telefono != null ? telefono.trim() : null;
        this.direccion = SanitizadorTexto.textoMultilinea(direccion);
    }

    public void desactivar() {
        this.activo = false;
    }

    public void activar() {
        this.activo = true;
    }
}

