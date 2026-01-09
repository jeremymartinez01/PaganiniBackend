package espol.integradora.paganinibackend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import espol.integradora.paganinibackend.Dto.UserDto;
import espol.integradora.paganinibackend.model.User;
import espol.integradora.paganinibackend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserService userService;

    private User existingUser;

    @BeforeEach
    void setup() {
        existingUser = new User(
                "Jose",
                "Suarez",
                "jose@espol.edu.ec",
                "0987654321",
                "cognitoSub123",
                "QR123");
        existingUser.setId(1);
        existingUser.setSaldo(BigDecimal.valueOf(100));
    }

    @Test
    void getByCorreo_userExists_returnsUserDto() {
        when(userRepository.findByCorreo("jose@espol.edu.ec")).thenReturn(Optional.of(existingUser));

        UserDto result = userService.getByCorreo("jose@espol.edu.ec");

        assertEquals("Jose", result.nombre());
        assertEquals("Suarez", result.apellido());
        assertEquals("jose@espol.edu.ec", result.correo());
        assertEquals(BigDecimal.valueOf(100), result.saldo());
        assertEquals("0987654321", result.telefono());
        assertEquals("QR123", result.codigoQr());
    }

    @Test
    void getByCorreo_userNotExists_returns404() {
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> userService.getByCorreo("dummy@example.com"));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void getSaldoByCorreo_userExists_returnsSaldoResponse() {
        when(userRepository.findByCorreo("jose@espol.edu.ec")).thenReturn(Optional.of(existingUser));
        var saldoResponse = userService.getSaldoByCorreo("jose@espol.edu.ec");
        assertEquals(saldoResponse.correo(), existingUser.getCorreo());
        assertEquals(saldoResponse.saldo(), existingUser.getSaldo());
    }

    @Test
    void getSaldoByCorreo_saldoNull_returnsZero() {
        existingUser.setSaldo(null);
        when(userRepository.findByCorreo("jose@espol.edu.ec"))
                .thenReturn(Optional.of(existingUser));

        var response = userService.getSaldoByCorreo("jose@espol.edu.ec");
        assertEquals(BigDecimal.ZERO, response.saldo());
    }

    @Test
    void getSaldoByCorreo_userNotFound_throws404() {
        when(userRepository.findByCorreo("dummy@example.com")).thenReturn(Optional.empty());
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> userService.getSaldoByCorreo("dummy@example.com"));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void deleteUser_userExists_deletesSuccessfully() {
        when(userRepository.findById(1)).thenReturn(Optional.of(existingUser));
        doNothing().when(userRepository).delete(existingUser);
        assertDoesNotThrow(() -> userService.deleteUser(1));
        verify(userRepository, times(1)).delete(existingUser);
    }

    @Test
    void deleteUser_userNotFound_throws404() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> userService.deleteUser(99));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void patchUser_allFieldsValid_updatesSuccessfully() {
        when(userRepository.findById(1)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        UserDto incoming = new UserDto(
                1,
                "Juan",
                "Perez",
                "juan@espol.edu.ec",
                BigDecimal.valueOf(200),
                "0999999999",
                "QR999");

        UserDto result = userService.patchUser(1, incoming);

        assertEquals("Juan", result.nombre());
        assertEquals("Perez", result.apellido());
        assertEquals("juan@espol.edu.ec", result.correo());
        assertEquals(BigDecimal.valueOf(200), result.saldo());
        assertEquals("0999999999", result.telefono());
        assertEquals("QR999", result.codigoQr());
    }

    @Test
    void patchUser_allFieldsNull_returnsOK() {
        when(userRepository.findById(1)).thenReturn(Optional.of(existingUser));
        assertDoesNotThrow(() -> userService.patchUser(1,
                new UserDto(1, null, null, null, null, null, null)));
    }

    @Test
    void patchUser_userNotFound_throws404() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> userService.patchUser(99, new UserDto(
                        99, "A", "B", "a@b.com", BigDecimal.ONE, "1234567", "QR")));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void patchUser_blankNombre_throws400() {
        when(userRepository.findById(1)).thenReturn(Optional.of(existingUser));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> userService.patchUser(1,
                        new UserDto(1, "   ", null, null, null, null, null)));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void patchUser_blankApellido_throws400() {
        when(userRepository.findById(1)).thenReturn(Optional.of(existingUser));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> userService.patchUser(1,
                        new UserDto(1, null, " ", null, null, null, null)));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void patchUser_invalidCorreo_throws400() {
        when(userRepository.findById(1)).thenReturn(Optional.of(existingUser));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> userService.patchUser(1,
                        new UserDto(1, null, null, "correo-invalido", null, null, null)));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void patchUser_negativeSaldo_throws400() {
        when(userRepository.findById(1)).thenReturn(Optional.of(existingUser));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> userService.patchUser(1,
                        new UserDto(1, null, null, null, BigDecimal.valueOf(-1), null, null)));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void patchUser_invalidTelefono_throws400() {
        when(userRepository.findById(1)).thenReturn(Optional.of(existingUser));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> userService.patchUser(1,
                        new UserDto(1, null, null, null, null, "ABC123", null)));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }


    @Test
    void patchUser_blankCodigoQr_throws400() {
        when(userRepository.findById(1)).thenReturn(Optional.of(existingUser));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> userService.patchUser(1,
                        new UserDto(1, null, null, null, null, null, " ")));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }
}
