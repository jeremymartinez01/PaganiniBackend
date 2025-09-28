package espol.integradora.paganinibackend.Dto;

public record RegistrationDto(
    String nombre,
    String apellido,
    String correo,
    String telefono,
    String password
) {}
