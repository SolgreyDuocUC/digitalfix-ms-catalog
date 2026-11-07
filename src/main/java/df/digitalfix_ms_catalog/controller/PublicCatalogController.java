package df.digitalfix_ms_catalog.controller;

import df.digitalfix_ms_catalog.dto.b2c.ProductoCatalogoDTO;
import df.digitalfix_ms_catalog.dto.b2c.ValidarStockRequest;
import df.digitalfix_ms_catalog.dto.b2c.ValidarStockResponse;
import df.digitalfix_ms_catalog.dto.repuesto.RepuestoResponse;
import df.digitalfix_ms_catalog.model.repuesto.Repuesto;
import df.digitalfix_ms_catalog.repository.RepuestoRepository;
import df.digitalfix_ms_catalog.service.RepuestoService;
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

    private final RepuestoService repuestoService;
    private final RepuestoRepository repuestoRepository;

    @GetMapping
    @Operation(summary = "Listado de repuestos para el portal cliente",
            description = "Devuelve repuestos activos con stock e información de certificación SEC en formato e-commerce.")
    public ResponseEntity<List<ProductoCatalogoDTO>> listarProductosB2C(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "false") boolean soloEnStock) {

        List<Repuesto> repuestos = repuestoRepository.findAll().stream()
                .filter(Repuesto::isActivo)
                .filter(r -> !soloEnStock || r.getStockDisponible() > 0)
                .filter(r -> search == null || search.isBlank()
                        || r.getNombre().toLowerCase().contains(search.toLowerCase())
                        || (r.getDescripcion() != null && r.getDescripcion().toLowerCase().contains(search.toLowerCase()))
                        || r.getCodigo().toLowerCase().contains(search.toLowerCase()))
                .toList();

        List<ProductoCatalogoDTO> dtos = repuestos.stream()
                .map(ProductoCatalogoDTO::from)
                .toList();

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de producto para el portal cliente")
    public ResponseEntity<ProductoCatalogoDTO> obtenerProductoB2C(@PathVariable Long id) {
        RepuestoResponse repuesto = repuestoService.buscarPorId(id);
        Optional<Repuesto> entidad = repuestoRepository.findById(id);
        return entidad.map(r -> ResponseEntity.ok(ProductoCatalogoDTO.from(r)))
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
                boolean suficiente = rep.isActivo() && rep.getStockDisponible() >= item.cantidad();
                if (!suficiente) {
                    todoDisponible = false;
                }
                detalles.add(new ValidarStockResponse.DetalleDisponibilidad(
                        rep.getIdRepuesto(),
                        rep.getNombre(),
                        item.cantidad(),
                        rep.getStockDisponible(),
                        suficiente
                ));
            }
        }

        String mensaje = todoDisponible
                ? "Stock verificado exitosamente"
                : "Uno o más ítems no cuentan con el stock solicitado";

        return ResponseEntity.ok(new ValidarStockResponse(todoDisponible, mensaje, detalles));
    }
}

