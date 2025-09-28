package espol.integradora.paganinibackend.Dto;

public record CardDto(
    String numeroTarjeta,
    String titular,
    int mes,
    int year,
    String cvv,
    String tipo,   
    String red     
) {}
