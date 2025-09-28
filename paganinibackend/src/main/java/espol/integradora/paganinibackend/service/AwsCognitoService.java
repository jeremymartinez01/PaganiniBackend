package espol.integradora.paganinibackend.service;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.AdminConfirmSignUpRequest;
import com.amazonaws.services.cognitoidp.model.AdminGetUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminGetUserResult;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.amazonaws.services.cognitoidp.model.AuthFlowType;
import com.amazonaws.services.cognitoidp.model.CodeMismatchException;
import com.amazonaws.services.cognitoidp.model.ConfirmForgotPasswordRequest;
import com.amazonaws.services.cognitoidp.model.ExpiredCodeException;
import com.amazonaws.services.cognitoidp.model.ForgotPasswordRequest;
import com.amazonaws.services.cognitoidp.model.InitiateAuthRequest;
import com.amazonaws.services.cognitoidp.model.InitiateAuthResult;
import com.amazonaws.services.cognitoidp.model.InvalidPasswordException;
import com.amazonaws.services.cognitoidp.model.LimitExceededException;
import com.amazonaws.services.cognitoidp.model.SignUpRequest;
import com.amazonaws.services.cognitoidp.model.SignUpResult;
import com.amazonaws.services.cognitoidp.model.UserNotFoundException;
import com.amazonaws.services.cognitoidp.model.UsernameExistsException;

import espol.integradora.paganinibackend.Dto.AuthResponse;

import org.springframework.http.ResponseEntity;

import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AwsCognitoService {

    private final AWSCognitoIdentityProvider cognitoClient;

    @Value("${aws.cognito.userPoolId}")
    private String userPoolId;

    @Value("${aws.cognito.clientId}")
    private String clientId;

    public AwsCognitoService(AWSCognitoIdentityProvider cognitoClient) {
        this.cognitoClient = cognitoClient;
    }

    /**
     * Registra al usuario y lo auto‑confirma, y devuelve el 'sub' que Cognito asigna.
     * @param username
     * @param password
     * @param email
     */
    public String signUp(String username, String password, String email) {
        try {
            SignUpResult result = cognitoClient.signUp(new SignUpRequest()
                .withClientId(clientId)
                .withUsername(username)
                .withPassword(password)
                .withUserAttributes(
                    new AttributeType()
                    .withName("email")
                    .withValue(email)
                ));


            return result.getUserSub();

        } catch (UsernameExistsException e) {
            // 409 Conflict
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "El usuario ya existe"
            );
        } catch (com.amazonaws.services.cognitoidp.model.InvalidPasswordException e) {
            // 400 Bad Request – contraseña no cumple política
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Contraseña inválida: debe tener minimo 8 caracteres conteniendo 1 numero, 1 caracter especial, 1 letra mayuscula y 1 letra minuscula " 
                + e.getErrorMessage()
            );
        } catch (com.amazonaws.services.cognitoidp.model.InvalidParameterException e) {
            // 400 Bad Request – parámetro mal formado
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Parámetro inválido: " + e.getErrorMessage()
            );
        } catch (com.amazonaws.services.cognitoidp.model.LimitExceededException e) {
            // 429 Too Many Requests
            throw new ResponseStatusException(
                HttpStatus.TOO_MANY_REQUESTS,
                "Demasiadas solicitudes: inténtalo de nuevo más tarde"
            );
        } catch (SdkClientException e) {
                System.err.println(">>> COGNITO ERROR CAUSE: " + e.getMessage());
                 e.printStackTrace();
            // 503 Service Unavailable – problemas de red/credenciales AWS
            throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Error de comunicación con AWS Cognito en servicio",
                e
            );
        }
    }

    /**
     * Inicia sesion con credenciales válidas
     * @param username
     * @param password
     * @return
     */
    public AuthResponse login(String username, String password) {
        InitiateAuthRequest req = new InitiateAuthRequest()
            .withAuthFlow(AuthFlowType.USER_PASSWORD_AUTH)
            .withClientId(clientId)
            .addAuthParametersEntry("USERNAME", username)
            .addAuthParametersEntry("PASSWORD", password);
        InitiateAuthResult result = cognitoClient.initiateAuth(req);
        var auth = result.getAuthenticationResult();
        return new AuthResponse(
            auth.getAccessToken(),
            auth.getIdToken(),
            auth.getRefreshToken(),
            auth.getExpiresIn()
        );
    }

    /*
     * Consulta si existe el correo
     */
    public void forgotPassword(String username) {
        try {
            cognitoClient.forgotPassword(new ForgotPasswordRequest()
                .withClientId(clientId)
                .withUsername(username));
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "Error al solicitar código "+ e.getMessage());
        }
    }

    /**
     * Actualiza la contraseña
     * @param username
     * @param confirmationCode
     * @param newPassword
     */
    public void confirmForgotPassword(String username, String confirmationCode, String newPassword) {
        if (confirmationCode == null || !confirmationCode.matches("\\d{6}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El código esta incorrecto");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña no puede estar vacía");
        }
        try {
            cognitoClient.confirmForgotPassword(new ConfirmForgotPasswordRequest()
                .withClientId(clientId)
                .withUsername(username)
                .withConfirmationCode(confirmationCode)
                .withPassword(newPassword));
        } catch (CodeMismatchException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código inválido");
        } catch (ExpiredCodeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código expirado");
        } catch (InvalidPasswordException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "Contraseña inválida: debe tener minimo 8 caracteres conteniendo 1 numero, 1 caracter especial, 1 letra mayuscula y 1 letra minuscula " , e);
        } catch (LimitExceededException e) {
            throw new ResponseStatusException(
                HttpStatus.TOO_MANY_REQUESTS,
                "Has excedido el número de intentos. Por favor solicita de nuevo un código."
            );
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "No fue posible restablecer contraseña", e);
        }
    }

}
