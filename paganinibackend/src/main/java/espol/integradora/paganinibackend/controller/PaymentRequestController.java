package espol.integradora.paganinibackend.controller;

import espol.integradora.paganinibackend.Dto.PaymentRequestCreateDto;
import espol.integradora.paganinibackend.Dto.PaymentRequestQrResponse;
import espol.integradora.paganinibackend.Dto.PaymentRequestSummaryDto;
import espol.integradora.paganinibackend.service.PaymentRequestService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/transactions/payment-requests")
public class PaymentRequestController {

    private final PaymentRequestService service;

    public PaymentRequestController(PaymentRequestService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody PaymentRequestCreateDto dto) {
        try {
            PaymentRequestQrResponse resp = service.generate(dto);
            return ResponseEntity.ok(resp);
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(Map.of("error", ex.getReason()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error inesperado"));
        }
    }
    
    @GetMapping("/activos")
    public ResponseEntity<List<PaymentRequestSummaryDto>> listarActivos(@RequestParam("correo") String correo) {
        return ResponseEntity.ok(service.listarActivosPorCorreo(correo));
    }
}
