package espol.integradora.paganinibackend.Dto;

import java.math.BigDecimal;

public record RecargaRequest(
        String email,
        Integer metodoPagoId,
        BigDecimal monto
) {}
