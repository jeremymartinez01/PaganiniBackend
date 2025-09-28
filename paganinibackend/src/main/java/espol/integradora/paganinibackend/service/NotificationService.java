// espol.integradora.paganinibackend.service.NotificationService.java
package espol.integradora.paganinibackend.service;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import espol.integradora.paganinibackend.model.*;
import espol.integradora.paganinibackend.model.constantes.*;
import espol.integradora.paganinibackend.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.*;

@Service
public class NotificationService {

  private final AmazonSNS sns;
  private final ObjectMapper om;
  private final UserRepository userRepo;
  private final DeviceTokenRepository deviceRepo;

  @Value("${aws.sns.platformApplicationArnAndroid}")
  private String androidPlatformArn;

  public NotificationService(AmazonSNS sns, ObjectMapper om,
                             UserRepository userRepo, DeviceTokenRepository deviceRepo) {
    this.sns = sns; this.om = om; this.userRepo = userRepo; this.deviceRepo = deviceRepo;
  }

  // ===== Registro de dispositivos =====
  public String registerDevice(espol.integradora.paganinibackend.Dto.RegisterDeviceRequest req) {
    if (req == null || req.correo() == null || req.token() == null || req.plataforma() == null)
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Datos incompletos");

    User user = userRepo.findByCorreo(req.correo())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuario no existe"));

    // upsert por token
    DeviceToken dt = deviceRepo.findByToken(req.token()).orElseGet(DeviceToken::new);
    dt.setUserId(user.getId());
    dt.setPlataforma(req.plataforma());
    dt.setToken(req.token());
    dt.setEstado(EstadoRegistro.activo);

    String endpointArn = dt.getEndpointArn();
    if (endpointArn == null || endpointArn.isBlank()) {
      endpointArn = createOrGetEndpoint(req.plataforma(), req.token(), String.valueOf(user.getId()));
      dt.setEndpointArn(endpointArn);
    } else {
      // Asegura atributos correctos en el endpoint existente
      setEndpointAttrs(endpointArn, req.token(), true);
    }

    deviceRepo.save(dt);
    return endpointArn;
  }

  private String createOrGetEndpoint(Plataforma p, String token, String userData) {
    String appArn = switch (p) {
      case ANDROID -> androidPlatformArn;
      case IOS     -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "iOS no configurado");
    };
    try {
      var r = new CreatePlatformEndpointRequest()
          .withPlatformApplicationArn(appArn)
          .withToken(token)
          .withCustomUserData(userData);
      var resp = sns.createPlatformEndpoint(r);
      // Habilita endpoint
      setEndpointAttrs(resp.getEndpointArn(), token, true);
      return resp.getEndpointArn();

    } catch (InvalidParameterException e) {
      // Caso típico: el endpoint ya existe para este token → ARN viene en el mensaje
      String msg = e.getErrorMessage();
      Pattern pat = Pattern.compile(".*Endpoint (arn:aws:sns:[^ ]+) already exists.*");
      Matcher m = pat.matcher(msg != null ? msg : "");
      if (m.matches()) {
        String arn = m.group(1);
        setEndpointAttrs(arn, token, true);
        return arn;
      }
      throw e;
    }
  }

  private void setEndpointAttrs(String endpointArn, String token, boolean enabled) {
    Map<String, String> attrs = new HashMap<>();
    attrs.put("Token", token);
    attrs.put("Enabled", enabled ? "true" : "false");
    sns.setEndpointAttributes(new SetEndpointAttributesRequest()
        .withEndpointArn(endpointArn)
        .withAttributes(attrs));
  }

  // ===== Envíos =====
  public void notifyEnvio(User sender, User receiver, BigDecimal monto) {
    String title = "Paganini";
    String body  = sender.getNombre() + " " + sender.getApellido()
        + " te ha enviado $" + monto.toPlainString();

    publishToUser(receiver.getId(), title, body);
  }

  // ===== Retiros =====
  public void notifyRetiro(User user, BigDecimal monto) {
    String title = "Paganini";
    String body  = "Tu retiro de $" + monto.toPlainString() + " ha sido acreditado a tu cuenta bancaria";
    publishToUser(user.getId(), title, body);
  }

  private void publishToUser(Integer userId, String title, String body) {
    var activos = deviceRepo.findByUserIdAndEstado(userId, EstadoRegistro.activo);
    for (DeviceToken dt : activos) {
      if (dt.getEndpointArn() == null || dt.getEndpointArn().isBlank()) continue;
      publishToEndpoint(dt.getEndpointArn(), title, body);
    }
  }

  private void publishToEndpoint(String endpointArn, String title, String body) {
    try {
      Map<String, Object> fcm = new HashMap<>();
      Map<String, String> notif = new HashMap<>();
      notif.put("title", title);
      notif.put("body", body);
      fcm.put("notification", notif);

      String gcm = om.writeValueAsString(fcm);
      Map<String, String> snsMsg = new HashMap<>();
      snsMsg.put("default", body);
      snsMsg.put("GCM", gcm);

      String message = om.writeValueAsString(snsMsg);

      PublishRequest req = new PublishRequest()
          .withTargetArn(endpointArn)
          .withMessageStructure("json")
          .withMessage(message);

      sns.publish(req);
    } catch (Exception ex) {
      // puedes loguear y, si quieres, marcar el device como inactivo si SNS dice "EndpointDisabled"
    }
  }
}
