package espol.integradora.paganinibackend.Dto;

public record EwalletInfoDto(
    Integer id,
    String estado,
    String direccion,
    String criptocoinAbreviacion
) {}