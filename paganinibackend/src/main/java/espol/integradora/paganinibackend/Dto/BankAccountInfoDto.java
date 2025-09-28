package espol.integradora.paganinibackend.Dto;

public record BankAccountInfoDto(
    Integer id,
    String estado,
    String nombreBanco,
    String numeroCuenta,
    String tipoCuenta,
    String titular,
    String identificacion
) {}