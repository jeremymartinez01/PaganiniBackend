package espol.integradora.paganinibackend.service;

import espol.integradora.paganinibackend.Dto.SaldoResponse;
import espol.integradora.paganinibackend.Dto.UserDto;
import espol.integradora.paganinibackend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import espol.integradora.paganinibackend.model.User;
import java.math.BigDecimal;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.http.HttpStatus;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository repo) {
        this.userRepository = repo;
    }

    public UserDto getByCorreo(String correo) {
        return userRepository.findByCorreo(correo)
            .map(u -> new UserDto(
                u.getNombre(),
                u.getApellido(),
                u.getCorreo(),
                u.getSaldo(),
                u.getTelefono(),
                u.getCodigoQr()
            ))
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Usuario no encontrado con correo: " + correo
            ));
    }

    @Transactional(readOnly = true)
    public SaldoResponse getSaldoByCorreo(String correo) {
        User u = userRepository.findByCorreo(correo)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        BigDecimal saldo = u.getSaldo() != null ? u.getSaldo() : BigDecimal.ZERO;
        return new SaldoResponse(u.getCorreo(), saldo);
    }
}
