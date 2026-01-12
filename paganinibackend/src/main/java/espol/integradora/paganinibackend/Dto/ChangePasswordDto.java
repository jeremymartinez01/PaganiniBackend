package espol.integradora.paganinibackend.Dto;

public record ChangePasswordDto(
    String accessToken,
    String currentPassword,
    String newPassword
) {}