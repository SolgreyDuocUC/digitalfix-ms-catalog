package df.digitalfix_ms_catalog.repository;

import df.digitalfix_ms_catalog.model.repuesto.Repuesto;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface RepuestoRepository extends JpaRepository<Repuesto, Long> {

    boolean existsByCodigo(String codigo);

    // Correlativo del código (secuencia de la migración V1): dos altas simultáneas nunca
    // reciben el mismo código.
    @Query(value = "SELECT SEQ_CODIGO_REPUESTO.NEXTVAL FROM DUAL", nativeQuery = true)
    long siguienteCorrelativoCodigo();

    List<Repuesto> findAllByOrderByNombreAsc();

    List<Repuesto> findByActivoTrueOrderByNombreAsc();

    @Query("select r from Repuesto r where r.stockDisponible <= r.stockMinimo order by r.stockDisponible asc")
    List<Repuesto> findBajoStockMinimo();

    /**
     * Bloqueo pesimista (SELECT ... FOR UPDATE) de los repuestos de un lote de stock: dos órdenes
     * que se asignan a la vez no pueden descontar el mismo stock. Se bloquean siempre en orden de
     * ID para que dos lotes con los mismos repuestos no se bloqueen mutuamente (deadlock).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Repuesto r where r.idRepuesto in :ids order by r.idRepuesto")
    List<Repuesto> findAllByIdConBloqueo(@Param("ids") Collection<Long> ids);
}
