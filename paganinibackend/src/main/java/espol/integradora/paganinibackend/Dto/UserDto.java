package espol.integradora.paganinibackend.Dto;
import java.math.BigDecimal;

public record UserDto(
    int id,
    String nombre,
    String apellido,
    String correo,
    BigDecimal saldo,
    String telefono,
    String codigoQr
) {}