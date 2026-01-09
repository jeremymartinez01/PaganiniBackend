package espol.integradora.paganinibackend.controller;

import espol.integradora.paganinibackend.Dto.SaldoResponse;
import espol.integradora.paganinibackend.Dto.UserDto;
import espol.integradora.paganinibackend.service.UserService;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService svc) {
        this.userService = svc;
    }

    @GetMapping
    public ResponseEntity<UserDto> getUserByCorreo(@RequestParam("correo") String correo) {
        UserDto dto = userService.getByCorreo(correo);
        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable String id,
            @RequestBody UserDto dto) {
        try {
            UserDto updated = userService.patchUser(Integer.parseInt(id), dto);
            return ResponseEntity.ok(updated);
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(Map.of("error", "El ID no es válido"));
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .body(Map.of("error", ex.getReason()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error inesperado"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String id) {
        try {
            UserDto deleted = userService.deleteUser(Integer.parseInt(id));
            return ResponseEntity.ok(deleted);
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(Map.of("error", "El ID no es válido"));
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .body(Map.of("error", ex.getReason()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error inesperado"));
        }
    }

    @GetMapping("/saldo")
    public ResponseEntity<?> getSaldo(@RequestParam("correo") String correo) {
        try {
            SaldoResponse resp = userService.getSaldoByCorreo(correo);
            return ResponseEntity.ok(resp);
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .body(Map.of("error", ex.getReason()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error inesperado"));
        }
    }
}
