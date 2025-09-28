package espol.integradora.paganinibackend.Dto;

public record PaymentRequestQrResponse(
    String qrBase64,
    String payload
) {}