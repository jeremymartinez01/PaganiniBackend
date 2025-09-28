package espol.integradora.paganinibackend.Dto;

public record AuthResponse(
    String accessToken,
    String idToken,
    String refreshToken,
    Integer expiresIn
) {}