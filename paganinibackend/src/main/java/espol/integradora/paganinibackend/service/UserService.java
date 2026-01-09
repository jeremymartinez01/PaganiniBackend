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
                        u.getId(),
                        u.getNombre(),
                        u.getApellido(),
                        u.getCorreo(),
                        u.getSaldo(),
                        u.getTelefono(),
                        u.getCodigoQr()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado con correo: " + correo));
    }

    @Transactional(readOnly = true)
    public SaldoResponse getSaldoByCorreo(String correo) {
        User u = userRepository.findByCorreo(correo)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        BigDecimal saldo = u.getSaldo() != null ? u.getSaldo() : BigDecimal.ZERO;
        return new SaldoResponse(u.getCorreo(), saldo);
    }

    @Transactional
    public UserDto patchUser(int id, UserDto incoming) {

        User existing = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado con ID: " + id));

        if (incoming.nombre() != null) {
            if (incoming.nombre().isBlank())
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "El nombre no puede estar vacío");
            existing.setNombre(incoming.nombre());
        }

        if (incoming.apellido() != null) {
            if (incoming.apellido().isBlank())
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "El apellido no puede estar vacío");
            existing.setApellido(incoming.apellido());
        }

        if (incoming.correo() != null) {
            if (!incoming.correo().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Correo inválido");
            existing.setCorreo(incoming.correo());
        }

        if (incoming.saldo() != null) {
            if (incoming.saldo().compareTo(BigDecimal.ZERO) < 0)
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "El saldo no puede ser negativo");
            existing.setSaldo(incoming.saldo());
        }

        if (incoming.telefono() != null) {
            if (!incoming.telefono().matches("^\\d{7,15}$"))
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Teléfono inválido");
            existing.setTelefono(incoming.telefono());
        }

        if (incoming.codigoQr() != null) {
            if (incoming.codigoQr().isBlank())
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "El código QR no puede estar vacío");
            existing.setCodigoQr(incoming.codigoQr());
        }

        userRepository.save(existing);

        return new UserDto(
                existing.getId(),
                existing.getNombre(),
                existing.getApellido(),
                existing.getCorreo(),
                existing.getSaldo(),
                existing.getTelefono(),
                existing.getCodigoQr());
    }

    @Transactional
    public UserDto deleteUser(int id) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado con ID: " + id));
        userRepository.delete(existing);
        return new UserDto(
                existing.getId(),
                existing.getNombre(),
                existing.getApellido(),
                existing.getCorreo(),
                existing.getSaldo(),
                existing.getTelefono(),
                existing.getCodigoQr());
    }

}
