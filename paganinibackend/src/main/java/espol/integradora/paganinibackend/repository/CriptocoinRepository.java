package espol.integradora.paganinibackend.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import espol.integradora.paganinibackend.model.Criptocoin;
import java.util.Optional;

public interface CriptocoinRepository extends JpaRepository<Criptocoin, Integer> {
    Optional<Criptocoin> findByAbreviacion(String abreviacion);

}