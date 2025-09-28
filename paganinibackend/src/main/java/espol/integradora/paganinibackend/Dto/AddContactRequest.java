package espol.integradora.paganinibackend.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AddContactRequest(
    @NotBlank @Email String correoContact
) {}
