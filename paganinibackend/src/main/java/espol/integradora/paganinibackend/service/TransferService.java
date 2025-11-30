package espol.integradora.paganinibackend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import espol.integradora.paganinibackend.model.MetodoPago;
import espol.integradora.paganinibackend.model.PaymentRequest;
import espol.integradora.paganinibackend.model.PaymentRequest.Status;
import espol.integradora.paganinibackend.model.Transaccion;
import espol.integradora.paganinibackend.model.User;
import espol.integradora.paganinibackend.repository.MetodoPagoRepository;
import espol.integradora.paganinibackend.repository.PaymentRequestRepository;
import espol.integradora.paganinibackend.repository.TransaccionRepository;
import espol.integradora.paganinibackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import espol.integradora.paganinibackend.service.VerificationService;

import espol.integradora.paganinibackend.model.constantes.EstadoPago;
import espol.integradora.paganinibackend.model.constantes.TipoPago;
import java.math.BigDecimal;
import espol.integradora.paganinibackend.Dto.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TransferService {

    private final UserRepository userRepo;
    private final TransaccionRepository txRepo;
    private final PaymentRequestRepository prRepo;
    private final MetodoPagoRepository metodoRepo;
    private final ObjectMapper om;
    private final NotificationService notificationService;
    private final VerificationService verificationService;

    @Autowired
    public TransferService(
            UserRepository userRepo,
            TransaccionRepository txRepo,
            PaymentRequestRepository prRepo,
            ObjectMapper om,
            MetodoPagoRepository metodoRepo,
            NotificationService notificationService,
            VerificationService verificationService) {
        this.userRepo = userRepo;
        this.txRepo = txRepo;
        this.prRepo = prRepo;
        this.om = om;
        this.metodoRepo = metodoRepo;
        this.notificationService = notificationService;
        this.verificationService = verificationService;
    }

    // 1) Envío por correo
    @Transactional
    public TransferResultDto enviarPorCorreo(String senderEmail, String receiverEmail, BigDecimal monto,
            String codigo) {
        User sender = userRepo.findByCorreo(senderEmail)
                .orElseThrow(() -> badRequest("Emisor no existe"));
        User receiver = userRepo.findByCorreo(receiverEmail)
                .orElseThrow(() -> badRequest("Receptor no existe"));
        boolean verification = this.verificationService.verifyCode(senderEmail, codigo);
        if (!verification) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Código inválido o expirado");
        }
        return registrarTransferenciaBloqueada(sender.getId(), receiver.getId(), monto, Transaccion.Origen.correo);
    }

    // 2) Envío por QR (correo en el QR; monto lo ingresa el usuario)
    @Transactional
    public TransferResultDto enviarPorQr(String senderEmail, String qrPayload, BigDecimal monto) {
        String receiverEmail = parseQrCorreo(qrPayload); // {"t":"PAYREQ","c":"correo"}
        User sender = userRepo.findByCorreo(senderEmail)
                .orElseThrow(() -> badRequest("Emisor no existe"));
        User receiver = userRepo.findByCorreo(receiverEmail)
                .orElseThrow(() -> badRequest("Receptor no existe"));

        return registrarTransferenciaBloqueada(sender.getId(), receiver.getId(), monto, Transaccion.Origen.qr);
    }

    // 3) Envío por QR con monto (valida payment_request activo y lo cierra)
    @Transactional
    public TransferResultDto enviarPorQrConMonto(String senderEmail, String qrPayload) {
        ParsedQr pq = parseQrCorreoYMonto(qrPayload); // {"t":"PAYREQ","c":"correo","m":"12.50", "pid":123?}

        User sender = userRepo.findByCorreo(senderEmail)
                .orElseThrow(() -> badRequest("Emisor no existe"));
        User receiver = userRepo.findByCorreo(pq.correo)
                .orElseThrow(() -> badRequest("Receptor no existe"));

        // localizar y BLOQUEAR el payment_request activo
        PaymentRequest pr = localizarPaymentRequestActivo(receiver, pq.monto, pq.pid);

        // ejecutar transferencia (con locks de usuarios)
        TransferResultDto res = registrarTransferenciaBloqueada(sender.getId(), receiver.getId(), pq.monto,
                Transaccion.Origen.qr_monto);

        // cerrar el payment_request
        pr.setStatus(Status.inactivo);
        prRepo.save(pr);

        return res;
    }

    // ======================= RECARGA =======================
    @Transactional
    public OperacionSaldoResult recargar(String email, Integer metodoPagoId, BigDecimal monto) {
        validarMonto(monto);

        var user = userRepo.findByCorreo(email)
                .orElseThrow(() -> badRequest("Usuario no existe"));

        // validar método: pertenece al usuario y es TARJETA
        var mp = metodoRepo.findByIdAndUserId(metodoPagoId, user.getId())
                .orElseThrow(() -> badRequest("Método de pago no pertenece al usuario"));
        if (mp.getTipo() != TipoPago.tarjeta)
            throw badRequest("Método de pago inválido para recarga (se requiere tarjeta)");
        if (mp.getEstado() != EstadoPago.activo)
            throw badRequest("Método de pago inactivo");
        // bloqueo y actualización de saldo
        var locked = userRepo.lockById(user.getId()).orElseThrow(() -> notFound("Usuario no existe"));
        locked.setSaldo(locked.getSaldo().add(monto));
        userRepo.save(locked);

        // registrar transacción (recarga)
        var tx = new Transaccion();
        tx.setSenderId(null);
        tx.setReceiverId(locked.getId());
        tx.setMetodoPagoId(mp.getId());
        tx.setTipo(Transaccion.Tipo.recarga);
        tx.setOrigen(null); // no aplica
        tx.setMonto(monto);
        tx.setEstado(Transaccion.Estado.completado);
        tx = txRepo.save(tx);

        return new OperacionSaldoResult(
                tx.getId(), locked.getId(), mp.getId(),
                "recarga", monto, locked.getSaldo(),
                "Recarga exitosa");
    }

    // ======================= RETIRO =======================
    @Transactional
    public OperacionSaldoResult retirar(String email, Integer metodoPagoId, BigDecimal monto) {
        validarMonto(monto);

        var user = userRepo.findByCorreo(email)
                .orElseThrow(() -> badRequest("Usuario no existe"));

        // validar método: pertenece al usuario y es CUENTA BANCO
        var mp = metodoRepo.findByIdAndUserId(metodoPagoId, user.getId())
                .orElseThrow(() -> badRequest("Método de pago no pertenece al usuario"));
        if (mp.getTipo() != TipoPago.cuentabanco)
            throw badRequest("Método de pago inválido para retiro (se requiere cuenta banco)");
        if (mp.getEstado() != EstadoPago.activo)
            throw badRequest("Método de pago inactivo");
        // bloqueo y validación de saldo
        var locked = userRepo.lockById(user.getId()).orElseThrow(() -> notFound("Usuario no existe"));
        if (locked.getSaldo().compareTo(monto) < 0)
            throw badRequest("Saldo insuficiente");

        locked.setSaldo(locked.getSaldo().subtract(monto));
        userRepo.save(locked);

        // registrar transacción (retiro)
        var tx = new Transaccion();
        tx.setSenderId(locked.getId());
        tx.setReceiverId(null);
        tx.setMetodoPagoId(mp.getId());
        tx.setTipo(Transaccion.Tipo.retiro);
        tx.setOrigen(null); // no aplica
        tx.setMonto(monto);
        tx.setEstado(Transaccion.Estado.completado);
        tx = txRepo.save(tx);

        notificationService.notifyRetiro(locked, monto);
        return new OperacionSaldoResult(
                tx.getId(), locked.getId(), mp.getId(),
                "retiro", monto, locked.getSaldo(),
                "Retiro exitoso");
    }

    @Transactional(readOnly = true)
    public HistorialTransaccionesDto historialPorCorreo(String correo) {
        User user = userRepo.findByCorreo(correo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuario no existe"));

        var envios = txRepo.findEnviosByUser(user.getId());
        var recibos = txRepo.findRecibosByUser(user.getId());
        var recargas = txRepo.findRecargasByUser(user.getId());
        var retiros = txRepo.findRetirosByUser(user.getId());

        return new HistorialTransaccionesDto(envios, recibos, recargas, retiros);
    }
    // ======================= Núcleo común =======================

    private TransferResultDto registrarTransferenciaBloqueada(Integer senderId, Integer receiverId,
            BigDecimal monto, Transaccion.Origen origen) {
        validarMonto(monto);

        // bloquear SIEMPRE en orden por id para evitar deadlocks
        Integer a = Math.min(senderId, receiverId);
        Integer b = Math.max(senderId, receiverId);

        User first = userRepo.lockById(a).orElseThrow(() -> notFound("Usuario " + a + " no existe"));
        User second = userRepo.lockById(b).orElseThrow(() -> notFound("Usuario " + b + " no existe"));

        User sender = first.getId().equals(senderId) ? first : second;
        User receiver = first.getId().equals(receiverId) ? first : second;

        if (sender.getId().equals(receiver.getId()))
            throw badRequest("No puedes enviarte a ti mismo");

        // Validación fuerte de saldo
        if (sender.getSaldo().compareTo(monto) < 0)
            throw badRequest("Saldo insuficiente");

        // Actualiza saldos
        sender.setSaldo(sender.getSaldo().subtract(monto));
        receiver.setSaldo(receiver.getSaldo().add(monto));

        // Inserta ENVÍO
        Transaccion txEnvio = new Transaccion();
        txEnvio.setSenderId(sender.getId());
        txEnvio.setReceiverId(receiver.getId());
        txEnvio.setMetodoPagoId(null);
        txEnvio.setTipo(Transaccion.Tipo.envio);
        txEnvio.setOrigen(origen);
        txEnvio.setMonto(monto);
        txEnvio.setEstado(Transaccion.Estado.completado);
        txEnvio = txRepo.save(txEnvio);

        // Inserta RECIBO
        Transaccion txRecibo = new Transaccion();
        txRecibo.setSenderId(receiver.getId());
        txRecibo.setReceiverId(sender.getId());
        txRecibo.setMetodoPagoId(null);
        txRecibo.setTipo(Transaccion.Tipo.recibo);
        txRecibo.setOrigen(origen);
        txRecibo.setMonto(monto);
        txRecibo.setEstado(Transaccion.Estado.completado);
        txRecibo = txRepo.save(txRecibo);

        // Persiste saldos
        userRepo.save(sender);
        userRepo.save(receiver);
        notificationService.notifyEnvio(sender, receiver, monto);

        return new TransferResultDto(
                txEnvio.getId(),
                txRecibo.getId(),
                sender.getId(),
                receiver.getId(),
                sender.getSaldo(),
                receiver.getSaldo());
    }

    // ======================= Payment Request helpers =======================

    private PaymentRequest localizarPaymentRequestActivo(User receiver, BigDecimal monto, Integer pid) {
        if (pid != null) {
            PaymentRequest pr = prRepo.lockById(pid)
                    .orElseThrow(() -> badRequest("Payment request no existe"));

            // Comparaciones correctas con la entidad y el enum
            if (!pr.getRequester().getId().equals(receiver.getId()))
                throw badRequest("Payment request no pertenece al receptor");
            if (pr.getStatus() != Status.activo)
                throw badRequest("Payment request no está activo");
            if (pr.getAmount().compareTo(monto) != 0)
                throw badRequest("Monto no coincide con el payment request");

            return pr;
        }

        // Fallback: buscar por (requester, monto, activo) el más reciente y luego
        // BLOQUEARLO
        PaymentRequest pr = prRepo.findTopByRequesterAndAmountAndStatusOrderByCreatedAtDesc(
                receiver, monto, Status.activo)
                .orElseThrow(() -> badRequest("No hay payment request activo para ese monto"));

        // Tomamos lock ahora para evitar carrera antes de cerrarlo
        return prRepo.lockById(pr.getId())
                .orElseThrow(() -> notFound("Payment request desapareció"));
    }

    // ======================= Parse & Validaciones =======================

    private void validarMonto(BigDecimal monto) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0 || monto.scale() > 2)
            throw badRequest("Monto inválido");
    }

    private String parseQrCorreo(String payload) {
        try {
            JsonNode n = om.readTree(payload);
            return n.get("c").asText();
        } catch (Exception e) {
            throw badRequest("QR inválido");
        }
    }

    private ParsedQr parseQrCorreoYMonto(String payload) {
        try {
            JsonNode n = om.readTree(payload);
            String correo = n.get("c").asText();
            BigDecimal monto = new BigDecimal(n.get("m").asText());
            Integer pid = (n.has("pid") && !n.get("pid").isNull()) ? n.get("pid").asInt() : null;
            return new ParsedQr(correo, monto, pid);
        } catch (Exception e) {
            throw badRequest("QR inválido");
        }
    }

    private record ParsedQr(String correo, BigDecimal monto, Integer pid) {
    }

    private ResponseStatusException badRequest(String msg) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
    }

    private ResponseStatusException notFound(String msg) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, msg);
    }
}
