package espol.integradora.paganinibackend.Dto;

import java.math.BigDecimal;

public record SaldoResponse(String correo, BigDecimal saldo) {}