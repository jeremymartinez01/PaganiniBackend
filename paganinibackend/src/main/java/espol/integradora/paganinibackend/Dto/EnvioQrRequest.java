package espol.integradora.paganinibackend.Dto;

import java.math.BigDecimal;

public record EnvioQrRequest(
            String senderEmail,
 String qrPayload, BigDecimal monto
) { }
