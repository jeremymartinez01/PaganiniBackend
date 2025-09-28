package espol.integradora.paganinibackend.Dto;


public record PaymentMethodDto(
    String correo,
    String tipo,
    CardDto card,
    BankAccountDto bankAccount,
    EwalletDto ewallet
) {}
