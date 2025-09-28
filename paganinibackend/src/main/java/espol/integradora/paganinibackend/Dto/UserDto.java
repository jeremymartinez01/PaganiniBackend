package espol.integradora.paganinibackend.Dto;
import java.math.BigDecimal;
import java.util.List;

public record UserDto(
    String nombre,
    String apellido,
    String correo,
    BigDecimal saldo,
    String telefono,
    String codigoQr
) {}