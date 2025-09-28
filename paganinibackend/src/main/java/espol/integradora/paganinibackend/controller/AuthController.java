package espol.integradora.paganinibackend.controller;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.cognitoidp.model.NotAuthorizedException;
import com.amazonaws.services.cognitoidp.model.UserNotConfirmedException;
import com.amazonaws.services.cognitoidp.model.UsernameExistsException;
import com.google.zxing.WriterException;

import espol.integradora.paganinibackend.service.AwsCognitoService;
import espol.integradora.paganinibackend.service.RegistrationService;
import com.amazonaws.services.cognitoidp.model.NotAuthorizedException;
import com.amazonaws.services.cognitoidp.model.UserNotConfirmedException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import espol.integradora.paganinibackend.Dto.AuthResponse;
import espol.integradora.paganinibackend.Dto.LoginDto;
import espol.integradora.paganinibackend.Dto.RegistrationDto;
import java.io.IOException;
import java.util.Map;
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final RegistrationService registrationService;
    private final AwsCognitoService cognitoService;

    public AuthController(RegistrationService registrationService,
                          AwsCognitoService cognitoService) {
        this.registrationService = registrationService;
        this.cognitoService    = cognitoService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody RegistrationDto dto) {
        try {
            registrationService.registerUser(dto);
            // 201 Created + mensaje en el body
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(Map.of("message", "Usuario registrado exitosamente"));
                    
        } catch (UsernameExistsException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "El usuario ya existe"));
        } catch (DataAccessException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", "No se pudo registrar en la base de datos"));
        } catch (ResponseStatusException e) {
                    return ResponseEntity
                            .status(e.getStatusCode())
                            .body(Map.of("error", e.getReason()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDto dto) {
        try {
            AuthResponse tokens = cognitoService.login(dto.correo(), dto.password());
            return ResponseEntity.ok(tokens);
        } catch (NotAuthorizedException e) {
            // 401 Unauthorized: usuario o contraseña incorrectos
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error","Usuario o contraseña inválidos"));
        } catch (UserNotConfirmedException e) {
            // 403 Forbidden: usuario no confirmado
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(Map.of("error","Usuario no confirmado"));
        } catch (SdkClientException e) {
            // 503 Service Unavailable: error en Cognito
            return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error","Error de comunicación con Cognito"));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String,String> body) {
        String correo = body.get("correo");
        cognitoService.forgotPassword(correo);
        return ResponseEntity.ok(Map.of("message", "Código enviado si el usuario existe"));
    }

    @PostMapping("/confirm-forgot-password")
    public ResponseEntity<?> confirmForgotPassword(@RequestBody Map<String,String> body) {
        
        cognitoService.confirmForgotPassword(
        body.get("correo"), 
        body.get("codigo"), 
        body.get("newPassword")
        );
        return ResponseEntity.ok(Map.of("message", "Contraseña actualizada con éxito"));
    }


}