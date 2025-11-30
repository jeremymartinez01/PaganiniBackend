package espol.integradora.paganinibackend.controller;

import espol.integradora.paganinibackend.Dto.*;
import espol.integradora.paganinibackend.service.VerificationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/verification-code")
public class VerificationController {

    private final VerificationService service;

    @Autowired
    public VerificationController(VerificationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<SendVerificationCodeDto> sendVerificationCode(
            @RequestBody VerificationCodeRequestDto request) {
        try {
            SendVerificationCodeDto dto = service.sendVerificationCode(request.getCorreo());
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(new SendVerificationCodeDto(false));
        }
    }

}
