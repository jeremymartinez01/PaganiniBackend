package espol.integradora.paganinibackend.controller;

import espol.integradora.paganinibackend.Dto.*;
import espol.integradora.paganinibackend.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

  private final NotificationService service;
  public NotificationController(NotificationService service){ this.service = service; }

  @PostMapping("/device")
  public ResponseEntity<RegisterDeviceResponse> register(@RequestBody RegisterDeviceRequest req) {
    return ResponseEntity.ok(new RegisterDeviceResponse(service.registerDevice(req)));
  }
}
