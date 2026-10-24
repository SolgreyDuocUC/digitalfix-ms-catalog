package df.digitalfix_ms_catalog.model.repuesto;

import df.digitalfix_ms_catalog.model.common.DatoInvalidoException;
import df.digitalfix_ms_catalog.model.common.EntidadAuditable;
import df.digitalfix_ms_catalog.model.common.OperacionNoPermitidaException;
import df.digitalfix_ms_catalog.model.common.SanitizadorTexto;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Repuesto del catálogo con su stock (tabla REPUESTO). ms-workorders lo referencia por
 * ID_REPUESTO en DETALLE_OT_REPUESTO y nunca toca el stock directamente: pide salidas y
 * devoluciones a este servicio, que es el único dueño del inventario.
 * <p>
 * El stock sólo cambia por {@link #registrarEntrada}, {@link #registrarSalida} y
 * {@link #registrarDevolucion}; cada uno devuelve el {@link MovimientoStock} que lo explica.
 * Un repuesto ya movido no se borra: se desactiva y deja de poder consumirse.
 */
@Entity
@Table(name = "REPUESTO",
        uniqueConstraints = @UniqueConstraint(name = "UK_REPUESTO_CODIGO", columnNames = "CODIGO"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Repuesto extends EntidadAuditable {

    static final int STOCK_MAXIMO = 9_999_999;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_REPUESTO", nullable = false, updatable = false)
    private Long idRepuesto;

    /** Código correlativo generado por el sistema (ej. "REP-0001"); no cambia después de creado. */
    @NotBlank
    @Size(max = 30)
    @Column(name = "CODIGO", nullable = false, updatable = false, length = 30)
    private String codigo;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    @Column(name = "NOMBRE", nullable = false, length = 100)
    private String nombre;

    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
    @Column(name = "DESCRIPCION", length = 255)
    private String descripcion;

    @NotNull(message = "La unidad de medida es obligatoria")
    @Enumerated(EnumType.STRING)
    @Column(name = "UNIDAD_MEDIDA", nullable = false, length = 20)
    private UnidadMedida unidadMedida;

    /** Precio de referencia en CLP por unidad de medida. */
    @NotNull(message = "El precio unitario es obligatorio")
    @DecimalMin(value = "0.00", message = "El precio unitario no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "El precio unitario admite hasta 10 enteros y 2 decimales")
    @Column(name = "PRECIO_UNITARIO", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    @NotNull
    @PositiveOrZero(message = "El stock disponible no puede ser negativo")
    @Max(value = STOCK_MAXIMO, message = "El stock excede el máximo permitido")
    @Column(name = "STOCK_DISPONIBLE", nullable = false)
    private Integer stockDisponible;

    /** Bajo este nivel el repuesto aparece como "por reponer" (filtro bajoStock del listado). */
    @NotNull(message = "El stock mínimo es obligatorio")
    @PositiveOrZero(message = "El stock mínimo no puede ser negativo")
    @Max(value = STOCK_MAXIMO, message = "El stock mínimo excede el máximo permitido")
    @Column(name = "STOCK_MINIMO", nullable = false)
    private Integer stockMinimo;

    @Column(name = "ACTIVO", nullable = false)
    private boolean activo = true;

    public Repuesto(String codigo, String nombre, String descripcion, UnidadMedida unidadMedida,
                    BigDecimal precioUnitario, int stockMinimo) {
        this.codigo = Objects.requireNonNull(codigo, "El código es obligatorio");
        this.stockDisponible = 0;
        actualizarDatos(nombre, descripcion, unidadMedida, precioUnitario, stockMinimo);
    }

    public void actualizarDatos(String nombre, String descripcion, UnidadMedida unidadMedida,
                                BigDecimal precioUnitario, int stockMinimo) {
        this.nombre = SanitizadorTexto.capitalizarPrimeraLetra(nombre);
        this.descripcion = SanitizadorTexto.textoMultilinea(descripcion);
        this.unidadMedida = Objects.requireNonNull(unidadMedida, "La unidad de medida es obligatoria");
        this.precioUnitario = Objects.requireNonNull(precioUnitario, "El precio unitario es obligatorio");
        this.stockMinimo = validarCantidad(stockMinimo, 0, "El stock mínimo");
    }

    public void activar() {
        this.activo = true;
    }

    public void desactivar() {
        this.activo = false;
    }

    public boolean bajoStockMinimo() {
        return stockDisponible <= stockMinimo;
    }

    /** Reposición de bodega. */
    public MovimientoStock registrarEntrada(int cantidad, String referencia, String usuario) {
        validarCantidad(cantidad, 1, "La cantidad");
        if ((long) stockDisponible + cantidad > STOCK_MAXIMO) {
            throw new DatoInvalidoException("La entrada deja el stock sobre el máximo permitido (" + STOCK_MAXIMO + ")");
        }
        this.stockDisponible += cantidad;
        return new MovimientoStock(this, TipoMovimiento.ENTRADA, cantidad, referencia, usuario);
    }

    /** Consumo de una orden de trabajo. Regla del caso: el stock disminuye al asignar la orden. */
    public MovimientoStock registrarSalida(int cantidad, String referencia, String usuario) {
        validarCantidad(cantidad, 1, "La cantidad");
        if (!activo) {
            throw new OperacionNoPermitidaException("El repuesto " + codigo + " está desactivado y no se puede consumir");
        }
        if (cantidad > stockDisponible) {
            throw new StockInsuficienteException("Stock insuficiente para " + codigo + " (" + nombre + "): se requieren "
                    + cantidad + " y hay " + stockDisponible + " disponibles");
        }
        this.stockDisponible -= cantidad;
        return new MovimientoStock(this, TipoMovimiento.SALIDA, cantidad, referencia, usuario);
    }

    /** Repuesto que vuelve a bodega; se acepta aunque el repuesto esté desactivado. */
    public MovimientoStock registrarDevolucion(int cantidad, String referencia, String usuario) {
        validarCantidad(cantidad, 1, "La cantidad");
        this.stockDisponible = Math.min(STOCK_MAXIMO, stockDisponible + cantidad);
        return new MovimientoStock(this, TipoMovimiento.DEVOLUCION, cantidad, referencia, usuario);
    }

    private static int validarCantidad(int cantidad, int minimo, String campo) {
        if (cantidad < minimo || cantidad > STOCK_MAXIMO) {
            throw new DatoInvalidoException(campo + " debe estar entre " + minimo + " y " + STOCK_MAXIMO);
        }
        return cantidad;
    }
}
