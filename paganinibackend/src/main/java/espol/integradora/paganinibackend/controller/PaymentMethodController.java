package espol.integradora.paganinibackend.controller;

import espol.integradora.paganinibackend.Dto.PaymentMethodDto;
import espol.integradora.paganinibackend.Dto.PaymentMethodUpdateDto;
import espol.integradora.paganinibackend.Dto.PaymentMethodsResponse;
import espol.integradora.paganinibackend.model.MetodoPago;
import espol.integradora.paganinibackend.service.PaymentMethodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/payment-methods")
public class PaymentMethodController {

    private final PaymentMethodService service;

    @Autowired
    public PaymentMethodController(PaymentMethodService service) {
        this.service = service;
    }

    /**
     * Crear un nuevo método de pago.
     */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody PaymentMethodDto dto) {
        try {
            MetodoPago created = service.create(dto);
            return ResponseEntity
                    .created(URI.create("/payment-methods/" + created.getId()))
                    .body(created);
        } catch (ResponseStatusException ex) {
            return ResponseEntity
                    .status(ex.getStatusCode())
                    .body(Map.of("error", ex.getReason()));
        } catch (Exception ex) {
                ex.printStackTrace();  // para que lo veas en consola
                Throwable root = org.springframework.core.NestedExceptionUtils.getRootCause(ex);
                String msg = root != null ? root.getMessage() : ex.getMessage();
                return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", msg));
                    }
    }

    /**
     * Activar un método de pago existente.
     */
    @PatchMapping("/{id}/activate")
    public ResponseEntity<?> activate(@PathVariable Integer id) {
        try {
            service.activate(id);
            return ResponseEntity.noContent().build();
        } catch (ResponseStatusException ex) {
            return ResponseEntity
                    .status(ex.getStatusCode())
                    .body(Map.of("error", ex.getReason()));
        } catch (Exception ex) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error inesperado"));
        }
    }

    /**
     * Desactivar un método de pago existente.
     */
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivate(@PathVariable Integer id) {
        try {
            service.deactivate(id);
            return ResponseEntity.noContent().build();
        } catch (ResponseStatusException ex) {
            return ResponseEntity
                    .status(ex.getStatusCode())
                    .body(Map.of("error", ex.getReason()));
        } catch (Exception ex) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error inesperado"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
        @PathVariable Integer id,
        @RequestBody PaymentMethodUpdateDto dto
    ) {
        try {
            MetodoPago updated = service.update(id, dto);
            return ResponseEntity.ok(updated);
        } catch (ResponseStatusException ex) {
            return ResponseEntity
                .status(ex.getStatusCode())
                .body(Map.of("error", ex.getReason()));
        } catch (Exception ex) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error inesperado"));
        }
    }
    /**
     * Eliminar un método de pago.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (ResponseStatusException ex) {
            return ResponseEntity
                    .status(ex.getStatusCode())
                    .body(Map.of("error", ex.getReason()));
        } catch (Exception ex) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error inesperado"));
        }
    }
    /**
     * Retorna todos los metodos de pago del usuario
     * @param correo
     * @return
     */
    @GetMapping("/by-user")
    public ResponseEntity<PaymentMethodsResponse> byUser(@RequestParam String correo) {
        PaymentMethodsResponse resp = service.findByUserCorreo(correo);
        return ResponseEntity.ok(resp);
    }
}
