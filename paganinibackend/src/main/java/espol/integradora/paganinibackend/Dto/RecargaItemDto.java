package espol.integradora.paganinibackend.Dto;

import java.math.BigDecimal;
import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonFormat;

import espol.integradora.paganinibackend.model.constantes.RedTarjeta;

public record RecargaItemDto(
        Integer mes,          
        Integer anio,         
        RedTarjeta red,       
        String titular,       
        BigDecimal monto,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "America/Guayaquil")
        Timestamp fecha
) {}
