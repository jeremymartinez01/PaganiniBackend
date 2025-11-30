package espol.integradora.paganinibackend.Dto;

import java.math.BigDecimal;

public record EnvioCorreoRequest(
          String senderEmail,
          String receiverEmail,
          BigDecimal monto,
          String codigo) {
}
