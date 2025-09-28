package espol.integradora.paganinibackend.Dto;

public record PaymentMethodUpdateDto(
    String tipo,             
    BankAccountDto bankAccount,
    CardDto card,
    EwalletDto ewallet
) {}