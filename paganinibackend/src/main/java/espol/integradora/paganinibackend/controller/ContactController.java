package espol.integradora.paganinibackend.controller;

import espol.integradora.paganinibackend.Dto.AddContactRequest;
import espol.integradora.paganinibackend.Dto.ContactDto;
import espol.integradora.paganinibackend.service.ContactService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users/{correoOwner}/contacts")
public class ContactController {

    private final ContactService service;

    public ContactController(ContactService service) {
        this.service = service;
    }

    /**
     * Agregar un contacto al usuario (por correo).
     * POST /users/{correoOwner}/contacts
     * Body: { "correoContact": "amigo@dominio.com" }
     */
    @PostMapping
    public ResponseEntity<?> add(@PathVariable("correoOwner") String correoOwner,
                                 @Valid @RequestBody AddContactRequest req) {
        try {
            ContactDto dto = service.addContact(correoOwner, req);
            return ResponseEntity
                    .created(URI.create("/users/" + correoOwner + "/contacts/" + dto.correo()))
                    .body(dto);
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(Map.of("error", ex.getReason()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error inesperado"));
        }
    }

    @GetMapping
    public ResponseEntity<?> list(@PathVariable("correoOwner") String correoOwner) {
        try {
            List<ContactDto> contactos = service.listContacts(correoOwner);
            return ResponseEntity.ok(contactos);
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(Map.of("error", ex.getReason()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error inesperado"));
        }
    }
    /**
     * Eliminar un contacto del usuario (por correo del contacto).
     * DELETE /users/{correoOwner}/contacts/{correoContact}
     */
    @DeleteMapping("/{correoContact}")
    public ResponseEntity<?> remove(@PathVariable String correoOwner,
                                    @PathVariable String correoContact) {
        try {
            service.removeContact(correoOwner, correoContact);
            return ResponseEntity.ok(Map.of(
                "message", "Contacto eliminado",
                "correoEliminado", correoContact
            ));
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(Map.of("error", ex.getReason()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error inesperado"));
        }
    }
}
