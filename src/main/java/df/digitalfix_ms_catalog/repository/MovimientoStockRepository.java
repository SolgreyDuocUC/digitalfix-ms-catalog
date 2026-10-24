package df.digitalfix_ms_catalog.repository;

import df.digitalfix_ms_catalog.model.repuesto.MovimientoStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, Long> {

    List<MovimientoStock> findByRepuesto_IdRepuestoOrderByFechaMovimientoDesc(Long idRepuesto);

    boolean existsByRepuesto_IdRepuesto(Long idRepuesto);
}
