package df.digitalfix_ms_catalog.service;

import df.digitalfix_ms_catalog.dto.service.ActualizarCatalogServiceRequest;
import df.digitalfix_ms_catalog.dto.service.CatalogServiceRequest;
import df.digitalfix_ms_catalog.dto.service.CatalogServiceResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CatalogServicesService {

    Page<CatalogServiceResponse> listar(String categoria, String search, boolean soloActivos, Pageable pageable);

    CatalogServiceResponse buscarPorId(Long id);

    CatalogServiceResponse crear(CatalogServiceRequest request);

    CatalogServiceResponse actualizar(Long id, ActualizarCatalogServiceRequest request);

    CatalogServiceResponse eliminarLogico(Long id);
}

