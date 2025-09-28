package espol.integradora.paganinibackend.repository;

import espol.integradora.paganinibackend.model.User;
import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    boolean existsByCorreo(String correo);
    boolean existsByCognitoUsername(String cognitoUsername);
    Optional<User> findByCorreo(String correo);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :id")
    Optional<User> lockById(@Param("id") Integer id);
}
