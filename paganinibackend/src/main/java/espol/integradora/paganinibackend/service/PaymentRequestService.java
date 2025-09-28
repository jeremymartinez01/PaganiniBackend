package espol.integradora.paganinibackend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.zxing.WriterException;
import espol.integradora.paganinibackend.Dto.PaymentRequestCreateDto;
import espol.integradora.paganinibackend.Dto.PaymentRequestQrResponse;
import espol.integradora.paganinibackend.Dto.PaymentRequestSummaryDto;
import espol.integradora.paganinibackend.model.PaymentRequest;
import espol.integradora.paganinibackend.model.User;
import espol.integradora.paganinibackend.repository.PaymentRequestRepository;
import espol.integradora.paganinibackend.repository.UserRepository;
import espol.integradora.paganinibackend.util.QrCodeGenerator;
import espol.integradora.paganinibackend.model.PaymentRequest.Status;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Genera un QR de solicitud de pago SIN firma:
 * payload JSON: {"t":"PAYREQ","c":"correo","m":"12.50","pid":123}
 */
@Service
public class PaymentRequestService {

    private final QrCodeGenerator qrCodeGenerator;
    private final UserRepository userRepo;
    private final PaymentRequestRepository prRepo;
    private final ObjectMapper om;

    public PaymentRequestService(QrCodeGenerator qrCodeGenerator,
                                 UserRepository userRepo,
                                 PaymentRequestRepository prRepo,
                                 ObjectMapper om) {
        this.qrCodeGenerator = qrCodeGenerator;
        this.userRepo = userRepo;
        this.prRepo = prRepo;
        this.om = om; // usa el ObjectMapper de Spring para construir el JSON seguro
    }

@Transactional
public PaymentRequestQrResponse generate(PaymentRequestCreateDto dto) {
    if (dto == null || dto.correoSolicitante() == null || dto.monto() == null)
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Datos incompletos");

    User user = userRepo.findByCorreo(dto.correoSolicitante())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuario no existe"));

    BigDecimal monto = dto.monto();
    if (monto.scale() > 2) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Monto con más de 2 decimales");
    if (monto.compareTo(BigDecimal.ZERO) <= 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Monto debe ser > 0");

    // 1) Crear y GUARDAR para obtener ID
    PaymentRequest pr = new PaymentRequest();
    pr.setRequester(user);
    pr.setAmount(monto);
    pr.setStatus(PaymentRequest.Status.activo);
    pr = prRepo.saveAndFlush(pr);           // <<-- importante para tener pr.getId()

    // 2) Construir payload con PID real
    ObjectNode root = om.createObjectNode();
    root.put("t", "PAYREQ");
    root.put("c", user.getCorreo());
    root.put("m", monto.toPlainString());
    root.put("pid", pr.getId());
    String payload = root.toString();

    // 3) Generar QR
    String qrBase64;
    try {
        qrBase64 = qrCodeGenerator.generateBase64Qr(payload);
    } catch (IOException | WriterException e) {
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo generar el QR", e);
    }

    // 4) Actualizar PR con payload y QR, y guardar
    pr.setPayload(payload);
    pr.setQrBase64(qrBase64);
    prRepo.save(pr);

    return new PaymentRequestQrResponse(qrBase64, payload);
}

    @Transactional(readOnly = true)
    public List<PaymentRequestSummaryDto> listarActivosPorCorreo(String correo) {
        User requester = userRepo.findByCorreo(correo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuario no existe"));

        return prRepo.findByRequesterAndStatusOrderByCreatedAtDesc(requester, Status.activo)
                .stream()
                .map(pr -> new PaymentRequestSummaryDto(
                        pr.getId(),
                        requester.getCorreo(),           // ya lo tenemos
                        pr.getAmount(),
                        pr.getStatus().name(),           // "activo"
                        pr.getCreatedAt(),
                        pr.getPayload(),
                        pr.getQrBase64()
                ))
                .toList();
    }
}
