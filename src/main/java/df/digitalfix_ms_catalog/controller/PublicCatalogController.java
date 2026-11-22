package df.digitalfix_ms_catalog.controller;

import df.digitalfix_ms_catalog.dto.b2c.ProductoCatalogoDTO;
import df.digitalfix_ms_catalog.dto.b2c.ValidarStockRequest;
import df.digitalfix_ms_catalog.dto.b2c.ValidarStockResponse;
import df.digitalfix_ms_catalog.dto.b2c.VerifyAndHoldRequest;
import df.digitalfix_ms_catalog.dto.b2c.VerifyAndHoldResponse;
import df.digitalfix_ms_catalog.model.repuesto.Repuesto;
import df.digitalfix_ms_catalog.repository.RepuestoRepository;
import df.digitalfix_ms_catalog.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/catalog/products")
@RequiredArgsConstructor
@Tag(name = "Catálogo B2C", description = "Endpoints de consulta de productos y validación de stock para el Portal de Clientes y Vendedores")
public class PublicCatalogController {

    private final RepuestoRepository repuestoRepository;
    private final StockService stockService;

    @GetMapping
    @Operation(summary = "Listado de repuestos para el portal cliente",
            description = "Devuelve repuestos activos con stock e información de certificación SEC en formato e-commerce, filtrables por búsqueda y categoría.")
    public ResponseEntity<List<ProductoCatalogoDTO>> listarProductosB2C(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false, defaultValue = "false") boolean soloEnStock) {

        List<ProductoCatalogoDTO> productos = repuestoRepository.findAll().stream()
                .filter(Repuesto::isActivo)
                .map(r -> ProductoCatalogoDTO.from(r, stockService.stockReservado(r.getIdRepuesto())))
                .filter(p -> !soloEnStock || p.disponible())
                .filter(p -> categoria == null || categoria.isBlank()
                        || p.categoria().equalsIgnoreCase(categoria.trim())
                        || p.categoria().toLowerCase().contains(categoria.trim().toLowerCase()))
                .filter(p -> search == null || search.isBlank()
                        || p.nombre().toLowerCase().contains(search.toLowerCase())
                        || (p.descripcion() != null && p.descripcion().toLowerCase().contains(search.toLowerCase()))
                        || p.codigo().toLowerCase().contains(search.toLowerCase()))
                .toList();

        return ResponseEntity.ok(productos);
    }

    @GetMapping("/categories")
    @Operation(summary = "Listar categorías disponibles",
            description = "Devuelve las categorías de productos eléctricos homologados SEC disponibles para filtros en el portal cliente.")
    public ResponseEntity<List<String>> listarCategorias() {
        List<String> categorias = repuestoRepository.findAll().stream()
                .filter(Repuesto::isActivo)
                .map(ProductoCatalogoDTO::inferirCategoria)
                .distinct()
                .sorted()
                .toList();

        return ResponseEntity.ok(categorias);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de producto para el portal cliente")
    public ResponseEntity<ProductoCatalogoDTO> obtenerProductoB2C(@PathVariable Long id) {
        return repuestoRepository.findById(id)
                .filter(Repuesto::isActivo)
                .map(r -> ResponseEntity.ok(ProductoCatalogoDTO.from(r, stockService.stockReservado(r.getIdRepuesto()))))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/stock/verify")
    @Operation(summary = "Verificar stock disponible antes de checkout",
            description = "Permite al carrito del portal cliente validar si los repuestos tienen existencias suficientes.")
    public ResponseEntity<ValidarStockResponse> verificarStock(@Valid @RequestBody ValidarStockRequest request) {
        List<ValidarStockResponse.DetalleDisponibilidad> detalles = new ArrayList<>();
        boolean todoDisponible = true;

        for (ValidarStockRequest.ItemStock item : request.items()) {
            Optional<Repuesto> opt = repuestoRepository.findById(item.idRepuesto());
            if (opt.isEmpty()) {
                detalles.add(new ValidarStockResponse.DetalleDisponibilidad(
                        item.idRepuesto(), "No encontrado", item.cantidad(), 0, false
                ));
                todoDisponible = false;
            } else {
                Repuesto rep = opt.get();
                int reservado = stockService.stockReservado(rep.getIdRepuesto());
                int disponibleReal = Math.max(0, rep.getStockDisponible() - reservado);
                boolean suficiente = rep.isActivo() && disponibleReal >= item.cantidad();
                if (!suficiente) {
                    todoDisponible = false;
                }
                detalles.add(new ValidarStockResponse.DetalleDisponibilidad(
                        rep.getIdRepuesto(),
                        rep.getNombre(),
                        item.cantidad(),
                        disponibleReal,
                        suficiente
                ));
            }
        }

        String mensaje = todoDisponible
                ? "Stock verificado exitosamente"
                : "Uno o más ítems no cuentan con el stock solicitado";

        return ResponseEntity.ok(new ValidarStockResponse(todoDisponible, mensaje, detalles));
    }

    @PostMapping("/stock/verify-and-hold")
    @Operation(summary = "Verificar y reservar stock temporalmente (B2C)",
            description = "Verifica la disponibilidad física de repuestos y reserva temporalmente el stock para el carrito de compras B2C.")
    public ResponseEntity<VerifyAndHoldResponse> verifyAndHold(@Valid @RequestBody VerifyAndHoldRequest request) {
        return ResponseEntity.ok(stockService.verificarYReservar(request));
    }
}