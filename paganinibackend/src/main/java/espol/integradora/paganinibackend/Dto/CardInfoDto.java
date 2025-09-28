package espol.integradora.paganinibackend.Dto;

public record CardInfoDto(
    Integer id,
    String estado,
    String numeroTarjeta,
    String titular,
    Integer mes,
    Integer year
) {}