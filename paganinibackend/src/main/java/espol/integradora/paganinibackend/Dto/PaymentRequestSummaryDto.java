package espol.integradora.paganinibackend.Dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentRequestSummaryDto(
        Integer id,
        String requesterEmail,
        BigDecimal amount,
        String status,
        LocalDateTime createdAt,
        String payload,
        String qrBase64
) { }