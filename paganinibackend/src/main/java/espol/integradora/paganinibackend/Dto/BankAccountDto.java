package espol.integradora.paganinibackend.Dto;

public record BankAccountDto(
    String nombreBanco,
    String numeroCuenta,
    String tipoCuenta,  
    String titular,
    String identificacion
) {}
