package espol.integradora.paganinibackend.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import espol.integradora.paganinibackend.model.BankAccount;

public interface BankAccountRepository extends JpaRepository<BankAccount, Integer> {}
