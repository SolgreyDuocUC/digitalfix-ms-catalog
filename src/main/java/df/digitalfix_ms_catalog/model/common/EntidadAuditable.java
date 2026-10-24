package df.digitalfix_ms_catalog.model.common;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Campos de auditoría comunes. {@code @Version} activa el bloqueo optimista: si dos transacciones
 * modifican la misma fila, la segunda falla en vez de pisar los cambios de la primera.
 */
@MappedSuperclass
@Getter
public abstract class EntidadAuditable {

    @CreationTimestamp
    @Column(name = "FECHA_CREACION", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "FECHA_ACTUALIZACION", nullable = false)
    private LocalDateTime fechaActualizacion;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Long version;
}
