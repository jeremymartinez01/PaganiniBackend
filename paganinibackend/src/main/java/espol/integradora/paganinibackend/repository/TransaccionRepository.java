package espol.integradora.paganinibackend.repository;

import espol.integradora.paganinibackend.Dto.*;
import espol.integradora.paganinibackend.model.Transaccion;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransaccionRepository extends JpaRepository<Transaccion, Integer> {

    // ENVÍOS: yo envié (tipo=envio). La contraparte (receptor) está en receiverId.
    @Query("""
        select new espol.integradora.paganinibackend.Dto.EnvioItemDto(
            t.monto, u.nombre, u.apellido, u.correo, t.createdAt
        )
        from Transaccion t
        join User u on u.id = t.receiverId
        where t.tipo = espol.integradora.paganinibackend.model.Transaccion$Tipo.envio
          and t.senderId = :userId
        order by t.createdAt desc
        """)
    List<EnvioItemDto> findEnviosByUser(@Param("userId") Integer userId);

    // RECIBOS: yo recibí (tipo=recibo). El emisor real está en receiverId (por cómo guardas el espejo).
    @Query("""
        select new espol.integradora.paganinibackend.Dto.ReciboItemDto(
            t.monto, u.nombre, u.apellido, u.correo, t.createdAt
        )
        from Transaccion t
        join User u on u.id = t.receiverId
        where t.tipo = espol.integradora.paganinibackend.model.Transaccion$Tipo.recibo
          and t.senderId = :userId
        order by t.createdAt desc
        """)
    List<ReciboItemDto> findRecibosByUser(@Param("userId") Integer userId);

    // RECARGAS: tipo=recarga. Unimos MetodoPago por id y luego su Card para mes/año/red/titular.
    @Query("""
        select new espol.integradora.paganinibackend.Dto.RecargaItemDto(
            c.mes, c.year, c.red, c.titular, t.monto, t.createdAt
        )
        from Transaccion t
        join MetodoPago mp on mp.id = t.metodoPagoId
        join mp.card c
        where t.tipo = espol.integradora.paganinibackend.model.Transaccion$Tipo.recarga
          and t.receiverId = :userId
        order by t.createdAt desc
        """)
    List<RecargaItemDto> findRecargasByUser(@Param("userId") Integer userId);

    // RETIROS: tipo=retiro. Unimos MetodoPago por id y luego su BankAccount.
    @Query("""
        select new espol.integradora.paganinibackend.Dto.RetiroItemDto(
            t.monto,b.nombreBanco, b.tipoCuenta, b.titular, t.createdAt
        )
        from Transaccion t
        join MetodoPago mp on mp.id = t.metodoPagoId
        join mp.bankAccount b
        where t.tipo = espol.integradora.paganinibackend.model.Transaccion$Tipo.retiro
          and t.senderId = :userId
        order by t.createdAt desc
        """)
    List<RetiroItemDto> findRetirosByUser(@Param("userId") Integer userId);
}
