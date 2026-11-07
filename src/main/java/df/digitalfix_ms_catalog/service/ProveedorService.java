package df.digitalfix_ms_catalog.service;

import df.digitalfix_ms_catalog.dto.proveedor.ProveedorRequest;
import df.digitalfix_ms_catalog.dto.proveedor.ProveedorResponse;
import df.digitalfix_ms_catalog.model.enums.TipoProveedor;

import java.util.List;

public interface ProveedorService {

    ProveedorResponse crear(ProveedorRequest request);

    List<ProveedorResponse> listar(TipoProveedor tipo, boolean soloActivos);

    ProveedorResponse buscarPorId(Long id);

    ProveedorResponse actualizar(Long id, ProveedorRequest request);

    void cambiarEstado(Long id, boolean activo);
}
