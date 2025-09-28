package espol.integradora.paganinibackend.Dto;

import java.math.BigDecimal;
import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonFormat;

import espol.integradora.paganinibackend.model.constantes.TipoCuenta;

public record RetiroItemDto(
        BigDecimal monto,
        String nombreBanco,
        TipoCuenta tipoCuenta,
        String titular,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "America/Guayaquil")
        Timestamp fecha
) {}
