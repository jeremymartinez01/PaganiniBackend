package espol.integradora.paganinibackend.repository;

import espol.integradora.paganinibackend.model.DeviceToken;
import espol.integradora.paganinibackend.model.constantes.EstadoRegistro;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Integer> {
  Optional<DeviceToken> findByToken(String token);
  List<DeviceToken> findByUserIdAndEstado(Integer userId, EstadoRegistro estado);
}
