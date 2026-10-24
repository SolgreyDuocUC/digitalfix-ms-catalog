package df.digitalfix_ms_catalog.model.repuesto;

import df.digitalfix_ms_catalog.model.common.SanitizadorTexto;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Registro inmutable de un cambio de stock: quién, cuánto, por qué (referencia, ej. "OT-15") y
 * con qué stock quedó el repuesto. Sólo lo crea {@link Repuesto}.
 */
@Entity
@Table(name = "MOVIMIENTO_STOCK", indexes = {
        @Index(name = "IDX_MOV_STOCK_REPUESTO", columnList = "ID_REPUESTO"),
        @Index(name = "IDX_MOV_STOCK_REFERENCIA", columnList = "REFERENCIA")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MovimientoStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_MOVIMIENTO", nullable = false, updatable = false)
    private Long idMovimiento;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_REPUESTO", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "FK_MOV_STOCK_REPUESTO"))
    private Repuesto repuesto;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO", nullable = false, updatable = false, length = 20)
    private TipoMovimiento tipo;

    @NotNull
    @Positive
    @Column(name = "CANTIDAD", nullable = false, updatable = false)
    private Integer cantidad;

    @NotNull
    @Column(name = "STOCK_RESULTANTE", nullable = false, updatable = false)
    private Integer stockResultante;

    @NotBlank(message = "La referencia del movimiento es obligatoria")
    @Size(max = 60, message = "La referencia no puede exceder 60 caracteres")
    @Column(name = "REFERENCIA", nullable = false, updatable = false, length = 60)
    private String referencia;

    @Size(max = 150)
    @Column(name = "USUARIO", updatable = false, length = 150)
    private String usuario;

    @NotNull
    @Column(name = "FECHA_MOVIMIENTO", nullable = false, updatable = false)
    private LocalDateTime fechaMovimiento;

    MovimientoStock(Repuesto repuesto, TipoMovimiento tipo, int cantidad, String referencia, String usuario) {
        this.repuesto = Objects.requireNonNull(repuesto);
        this.tipo = Objects.requireNonNull(tipo);
        this.cantidad = cantidad;
        this.stockResultante = repuesto.getStockDisponible();
        this.referencia = SanitizadorTexto.texto(referencia);
        this.usuario = SanitizadorTexto.texto(usuario);
        this.fechaMovimiento = LocalDateTime.now();
    }
}
