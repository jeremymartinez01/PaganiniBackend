package espol.integradora.paganinibackend.Dto;
import java.math.BigDecimal;

public record PaymentRequestCreateDto (
    String correoSolicitante,
    BigDecimal monto
){}
