package espol.integradora.paganinibackend.controller;

import espol.integradora.paganinibackend.Dto.*;
import espol.integradora.paganinibackend.service.TransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transacciones")
public class TransferController {

    private final TransferService service;

    @Autowired
    public TransferController(TransferService service) {
        this.service = service;
    }

    @PostMapping("/enviar/correo")
    public ResponseEntity<TransferResultDto> enviarPorCorreo(@RequestBody EnvioCorreoRequest req) {
        var res = service.enviarPorCorreo(req.senderEmail(), req.receiverEmail(), req.monto(), req.codigo());
        return ResponseEntity.ok(res);
    }

    @PostMapping("/enviar/qr")
    public ResponseEntity<TransferResultDto> enviarPorQr(@RequestBody EnvioQrRequest req) {
        var res = service.enviarPorQr(req.senderEmail(), req.qrPayload(), req.monto());
        return ResponseEntity.ok(res);
    }

    @PostMapping("/enviar/qr-monto")
    public ResponseEntity<TransferResultDto> enviarPorQrConMonto(@RequestBody EnvioQrMontoRequest req) {
        var res = service.enviarPorQrConMonto(req.senderEmail(), req.qrPayload());
        return ResponseEntity.ok(res);
    }

    @PostMapping("/recarga")
    public ResponseEntity<OperacionSaldoResult> recargar(@RequestBody RecargaRequest req) {
        var res = service.recargar(req.email(), req.metodoPagoId(), req.monto());
        return ResponseEntity.ok(res);
    }

    @PostMapping("/retiro")
    public ResponseEntity<OperacionSaldoResult> retirar(@RequestBody RetiroRequest req) {
        var res = service.retirar(req.email(), req.metodoPagoId(), req.monto());
        return ResponseEntity.ok(res);
    }

    @GetMapping("/historial")
    public ResponseEntity<HistorialTransaccionesDto> historial(@RequestParam("correo") String correo) {
        return ResponseEntity.ok(service.historialPorCorreo(correo));
    }
}
