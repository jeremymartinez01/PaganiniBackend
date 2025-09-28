package espol.integradora.paganinibackend.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
public class HealthController {
  @GetMapping("/")
  public ResponseEntity<Void> ping() {
    return ResponseEntity.ok().build();
  }
  @GetMapping("/healthz")
  public String health() { return "OK"; }
}