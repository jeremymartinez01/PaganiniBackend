package espol.integradora.paganinibackend.service;

import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;

import espol.integradora.paganinibackend.Dto.SendVerificationCodeDto;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VerificationService {

    private final GmailService gmailService;

    private final Map<String, VerificationEntry> codes = new ConcurrentHashMap<>();

    public VerificationService(GmailService gmailService) {
        this.gmailService = gmailService;
    }

    public SendVerificationCodeDto sendVerificationCode(String email) {
        if(email.isBlank() || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")){
            return new SendVerificationCodeDto(false);
        }
        try {
            String code = generateCode();

            codes.put(email, new VerificationEntry(code, Instant.now().plusSeconds(300)));

            String subject = "Verificación de pago PAGANINI";
            String text = "Tu código de verificación: " + code + "\n\nEste código expira en 5 minutos.";

            gmailService.sendEmail(email, subject, text);

            return new SendVerificationCodeDto(true);
        } catch (MailException e) {
            throw new RuntimeException("Failed to send verification code");
        }
    }

    public boolean verifyCode(String email, String submittedCode) {
        if (email == null || submittedCode == null || email.isBlank() || submittedCode.isBlank()){
            return false;
        }
        VerificationEntry entry = this.queryCode(email);

        if (entry == null) {
            return false;
        }

        if (Instant.now().isAfter(entry.expiresAt())) {
            codes.remove(email);
            return false;
        }

        boolean isValid = entry.code().equals(submittedCode);

        if (isValid) {
            codes.remove(email);
        }

        return isValid;
    }

    private String generateCode() {
        SecureRandom random = new SecureRandom();
        int number = random.nextInt(900000) + 100000;
        return String.valueOf(number);
    }

    protected VerificationEntry queryCode(String email){
        return codes.get(email);
    }

    protected record VerificationEntry(String code, Instant expiresAt) {
    }
}
