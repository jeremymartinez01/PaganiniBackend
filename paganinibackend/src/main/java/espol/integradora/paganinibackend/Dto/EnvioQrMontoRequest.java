package espol.integradora.paganinibackend.Dto;

public record EnvioQrMontoRequest(
    String senderEmail, String qrPayload
) { }
