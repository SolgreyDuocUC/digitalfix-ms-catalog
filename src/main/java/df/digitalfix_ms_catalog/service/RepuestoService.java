package df.digitalfix_ms_catalog.service;

import df.digitalfix_ms_catalog.dto.repuesto.ActualizarRepuestoRequest;
import df.digitalfix_ms_catalog.dto.repuesto.RepuestoRequest;
import df.digitalfix_ms_catalog.dto.repuesto.RepuestoResponse;
import df.digitalfix_ms_catalog.model.repuesto.Repuesto;

import java.util.List;

public interface RepuestoService {

    RepuestoResponse crear(RepuestoRequest request);

    List<RepuestoResponse> listar(boolean soloActivos, boolean bajoStock, String search, String sortDirection);

    default List<RepuestoResponse> listar(boolean soloActivos, boolean bajoStock) {
        return listar(soloActivos, bajoStock, null, "asc");
    }

    RepuestoResponse buscarPorId(Long id);

    RepuestoResponse actualizar(Long id, ActualizarRepuestoRequest request);

    void eliminar(Long id);

    Repuesto obtener(Long id);
}
