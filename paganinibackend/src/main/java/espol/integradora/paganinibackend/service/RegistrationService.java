package espol.integradora.paganinibackend.service;

import com.google.zxing.WriterException;
import espol.integradora.paganinibackend.model.User;
import espol.integradora.paganinibackend.repository.UserRepository;
import espol.integradora.paganinibackend.util.QrCodeGenerator;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import espol.integradora.paganinibackend.Dto.RegistrationDto;
import java.io.IOException;

@Service
public class RegistrationService {

    private final AwsCognitoService cognitoService;
    private final UserRepository userRepository;
    private final QrCodeGenerator qrCodeGenerator;

    public RegistrationService(AwsCognitoService cognitoService,
                               UserRepository userRepository,
                               QrCodeGenerator qrCodeGenerator) {
        this.cognitoService = cognitoService;
        this.userRepository = userRepository;
        this.qrCodeGenerator = qrCodeGenerator;
    }

    @Transactional
    public void registerUser(RegistrationDto dto) {
        // 1) Registrar en Cognito y obtener el 'sub'
        String cognitoSub = cognitoService.signUp(
            dto.correo(),    // username = email
            dto.password(),
            dto.correo()
        );

        // 2) Generar QR con el correo
        String qrBase64;
        try {
            qrBase64 = qrCodeGenerator.generateBase64Qr(dto.correo());
        } catch (IOException | WriterException e) {
            // 500 Internal Server Error
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "No se pudo generar el código QR",
                e
            );
        }
        // 3) Persistir en Aurora usando el 'sub' como cognitoUsername
        User u = new User(
            dto.nombre(),
            dto.apellido(),
            dto.correo(),
            dto.telefono(),
            cognitoSub,    // aquí va el sub, no el email
            qrBase64
        );
        try {
            userRepository.saveAndFlush(u);
        } catch (DataAccessException e) {
            // 502 Bad Gateway
            throw new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "Error al guardar el usuario en la base de datos",
                e
            );
        }
    }
}
