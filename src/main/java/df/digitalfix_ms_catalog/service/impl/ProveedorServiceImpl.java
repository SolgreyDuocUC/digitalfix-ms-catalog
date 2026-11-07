package df.digitalfix_ms_catalog.service.impl;

import df.digitalfix_ms_catalog.dto.proveedor.ProveedorRequest;
import df.digitalfix_ms_catalog.dto.proveedor.ProveedorResponse;
import df.digitalfix_ms_catalog.model.common.DatoInvalidoException;
import df.digitalfix_ms_catalog.model.enums.TipoProveedor;
import df.digitalfix_ms_catalog.model.proveedor.Proveedor;
import df.digitalfix_ms_catalog.repository.ProveedorRepository;
import df.digitalfix_ms_catalog.service.ProveedorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProveedorServiceImpl implements ProveedorService {

    private final ProveedorRepository proveedorRepository;

    @Override
    @Transactional
    public ProveedorResponse crear(ProveedorRequest request) {
        String rutLimpio = request.rut().trim().toUpperCase();
        if (proveedorRepository.existsByRut(rutLimpio)) {
            throw new DatoInvalidoException("Ya existe un proveedor registrado con el RUT: " + rutLimpio);
        }
        if (proveedorRepository.existsByRazonSocial(request.razonSocial().trim())) {
            throw new DatoInvalidoException("Ya existe un proveedor con la razón social: " + request.razonSocial());
        }

        Proveedor proveedor = new Proveedor(
                rutLimpio,
                request.razonSocial(),
                request.nombreFantasia(),
                request.tipoProveedor(),
                request.emailContacto(),
                request.telefono(),
                request.direccion()
        );

        return ProveedorResponse.from(proveedorRepository.save(proveedor));
    }

    @Override
    public List<ProveedorResponse> listar(TipoProveedor tipo, boolean soloActivos) {
        List<Proveedor> lista;
        if (tipo != null && soloActivos) {
            lista = proveedorRepository.findByTipoProveedorAndActivoTrue(tipo);
        } else if (tipo != null) {
            lista = proveedorRepository.findByTipoProveedor(tipo);
        } else if (soloActivos) {
            lista = proveedorRepository.findByActivoTrue();
        } else {
            lista = proveedorRepository.findAll();
        }
        return lista.stream().map(ProveedorResponse::from).toList();
    }

    @Override
    public ProveedorResponse buscarPorId(Long id) {
        return proveedorRepository.findById(id)
                .map(ProveedorResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proveedor no encontrado con ID: " + id));
    }

    @Override
    @Transactional
    public ProveedorResponse actualizar(Long id, ProveedorRequest request) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proveedor no encontrado con ID: " + id));

        proveedor.actualizarDatos(
                request.razonSocial(),
                request.nombreFantasia(),
                request.tipoProveedor(),
                request.emailContacto(),
                request.telefono(),
                request.direccion()
        );

        return ProveedorResponse.from(proveedor);
    }

    @Override
    @Transactional
    public void cambiarEstado(Long id, boolean activo) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proveedor no encontrado con ID: " + id));
        if (activo) {
            proveedor.activar();
        } else {
            proveedor.desactivar();
        }
    }
}

