package espol.integradora.paganinibackend.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import espol.integradora.paganinibackend.model.Ewallet;

public interface EwalletRepository extends JpaRepository<Ewallet, Integer> {}