package espol.integradora.paganinibackend.Dto;
import java.util.List;

public record PaymentMethodsResponse(
    List<BankAccountInfoDto> cuentabanco,
    List<CardInfoDto> tarjeta,
    List<EwalletInfoDto> ewallet
) {}