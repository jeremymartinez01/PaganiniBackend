package espol.integradora.paganinibackend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import espol.integradora.paganinibackend.Dto.SendVerificationCodeDto;

@ExtendWith(MockitoExtension.class)
public class VerificationServiceTest {
    @Mock
    GmailService gmailService;

    @Spy
    @InjectMocks
    VerificationService verificationService;

    @Test
    void sendVerificationCode_emailIsValid() {
        assertEquals(new SendVerificationCodeDto(true),
                verificationService.sendVerificationCode("jojusuar@espol.edu.ec"));
    }

    @Test
    void sendVerificationCode_emailIsInvalid() {
        assertEquals(new SendVerificationCodeDto(false),
                verificationService.sendVerificationCode("sfsd"));
    }

    @Test
    void sendVerificationCode_emailIsBlank() {
        assertEquals(new SendVerificationCodeDto(false),
                verificationService.sendVerificationCode(""));
    }

    @Test
    void sendVerificationCode_sendingFailed() {
        doThrow(new MailSendException("")).when(gmailService).sendEmail(anyString(), anyString(), anyString());
        assertThrows(RuntimeException.class,
                () -> verificationService.sendVerificationCode("jojusuar@espol.edu.ec"));
    }

    @Test
    void verifyCode_validEmail_CodeExists_CodeNotExpired() {
        when(verificationService.queryCode("jojusuar@espol.edu.ec"))
                .thenReturn(new VerificationService.VerificationEntry("999999",
                        Instant.now().plusSeconds(300)));
        Boolean result = verificationService.verifyCode("jojusuar@espol.edu.ec", "999999");
        assertEquals(true, result);
    }

    @Test
    void verifyCode_emailDoesNotExist() {
        when(verificationService.queryCode("jojusuar@espol.edu.ec"))
                .thenReturn(null);
        assertFalse(verificationService.verifyCode("jojusuar@espol.edu.ec", "123456"));
    }

    @Test
    void verifyCode_codeDoesNotMatch() throws InterruptedException {
        when(verificationService.queryCode("jojusuar@espol.edu.ec"))
                .thenReturn(new VerificationService.VerificationEntry("999999",
                        Instant.now().plusSeconds(300)));
        assertFalse(verificationService.verifyCode("jojusuar@espol.edu.ec", "123456"));
    }

    @Test
    void verifyCode_codeExpired() throws InterruptedException {
        when(verificationService.queryCode("jojusuar@espol.edu.ec"))
                .thenReturn(new VerificationService.VerificationEntry("999999",
                        Instant.now().minusSeconds(300)));
        assertFalse(verificationService.verifyCode("jojusuar@espol.edu.ec", "999999"));
    }

    @Test
    void verifyCode_emailIsNull() {
        assertFalse(verificationService.verifyCode(null, "123456"));
    }

    @Test
    void verifyCode_codeIsNull() {
        String email = "jojusuar@espol.edu.ec";
        assertFalse(verificationService.verifyCode(email, null));
    }

    @Test
    void verifyCode_emailIsBlank() {
        assertFalse(verificationService.verifyCode("", "123456"));
    }

    @Test
    void verifyCode_codeIsBlank() {
        String email = "jojusuar@espol.edu.ec";
        assertFalse(verificationService.verifyCode(email, ""));
    }

}
