package espol.integradora.paganinibackend.Dto;

import java.math.BigDecimal;
import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonFormat;

public record ReciboItemDto(
        BigDecimal monto,
        String emisorNombre,
        String emisorApellido,
        String emisorCorreo,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "America/Guayaquil")
        Timestamp fecha
) {}
