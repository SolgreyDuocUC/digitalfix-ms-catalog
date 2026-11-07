package df.digitalfix_ms_catalog.repository;

import df.digitalfix_ms_catalog.model.enums.TipoProveedor;
import df.digitalfix_ms_catalog.model.proveedor.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

    Optional<Proveedor> findByRut(String rut);

    boolean existsByRut(String rut);

    boolean existsByRazonSocial(String razonSocial);

    List<Proveedor> findByTipoProveedor(TipoProveedor tipoProveedor);

    List<Proveedor> findByActivoTrue();

    List<Proveedor> findByTipoProveedorAndActivoTrue(TipoProveedor tipoProveedor);
}

