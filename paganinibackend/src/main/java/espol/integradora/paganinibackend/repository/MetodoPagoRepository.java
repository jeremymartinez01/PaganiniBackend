package espol.integradora.paganinibackend.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import espol.integradora.paganinibackend.model.MetodoPago;

public interface MetodoPagoRepository extends JpaRepository<MetodoPago, Integer> {
    List<MetodoPago> findAllByUserId(Integer userId);
    Optional<MetodoPago> findByIdAndUserId(Integer id, Integer userId);
}
