package espol.integradora.paganinibackend.service;

import espol.integradora.paganinibackend.Dto.AddContactRequest;
import espol.integradora.paganinibackend.Dto.ContactDto;
import espol.integradora.paganinibackend.model.User;
import espol.integradora.paganinibackend.model.UserContact;
import espol.integradora.paganinibackend.repository.UserContactRepository;
import espol.integradora.paganinibackend.repository.UserRepository;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ContactService {

    private final UserRepository userRepo;
    private final UserContactRepository ucRepo;

    public ContactService(UserRepository userRepo, UserContactRepository ucRepo) {
        this.userRepo = userRepo;
        this.ucRepo = ucRepo;
    }

    @Transactional
    public ContactDto addContact(String correoOwner, AddContactRequest req) {
        User owner = userRepo.findByCorreo(correoOwner)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner no encontrado"));

        User contact = userRepo.findByCorreo(req.correoContact())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Contacto no existe"));

        if (owner.getId().equals(contact.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes agregarte a ti mismo");
        }

        boolean exists = ucRepo.existsByOwner_IdAndContact_Id(owner.getId(), contact.getId());
        if (exists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe en contactos");
        }

        UserContact uc = new UserContact();
        uc.setOwner(owner);
        uc.setContact(contact);
        uc.setStatus(UserContact.Status.ACCEPTED);
        ucRepo.save(uc);

        return new ContactDto(
                contact.getNombre(),
                contact.getApellido(),
                contact.getCorreo(),
                contact.getTelefono()
        );
    }

    @Transactional(readOnly = true)
    public List<ContactDto> listContacts(String correoOwner) {
        User owner = userRepo.findByCorreo(correoOwner)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuario no encontrado: " + correoOwner));

        // Devuelve directamente la proyección a DTO desde el repo
        return ucRepo.findContactDtosByOwnerId(owner.getId());
    }
    
    @Transactional
    public void removeContact(String correoOwner, String correoContact) {
        User owner = userRepo.findByCorreo(correoOwner)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner no encontrado"));

        User contact = userRepo.findByCorreo(correoContact)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Contacto no existe"));

        int deleted = ucRepo.deleteByOwnerIdAndContactId(owner.getId(), contact.getId());
        if (deleted == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El contacto no está asociado a este usuario");
        }

    }
}
