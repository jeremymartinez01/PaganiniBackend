package espol.integradora.paganinibackend.Dto;

import java.util.List;

public record HistorialTransaccionesDto(
        List<EnvioItemDto>   envios,
        List<ReciboItemDto>  recibos,
        List<RecargaItemDto> recargas,
        List<RetiroItemDto>  retiros
) {}