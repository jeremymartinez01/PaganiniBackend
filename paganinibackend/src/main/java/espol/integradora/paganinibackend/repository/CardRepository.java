package espol.integradora.paganinibackend.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import espol.integradora.paganinibackend.model.Card;

public interface CardRepository extends JpaRepository<Card, Integer> {}

