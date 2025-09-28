package espol.integradora.paganinibackend.Dto;

import java.math.BigDecimal;

public record TransferResultDto(
        Integer envioId,
        Integer reciboId,
        Integer senderId,
        Integer receiverId,
        BigDecimal nuevoSaldoSender,
        BigDecimal nuevoSaldoReceiver
) { }
