package espol.integradora.paganinibackend.Dto;

import java.math.BigDecimal;

public record OperacionSaldoResult(
        Integer transaccionId,
        Integer userId,
        Integer metodoPagoId,
        String  tipo,
        BigDecimal monto,
        BigDecimal nuevoSaldo,
        String  mensaje
) {}
