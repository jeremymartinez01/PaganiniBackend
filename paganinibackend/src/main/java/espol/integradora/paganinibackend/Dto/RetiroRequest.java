package espol.integradora.paganinibackend.Dto;

import java.math.BigDecimal;

public record RetiroRequest(
        String email,
        Integer metodoPagoId,
        BigDecimal monto
) {}
