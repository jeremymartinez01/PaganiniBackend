package espol.integradora.paganinibackend.service;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

@Service
public class EncryptionService {

    private static final String ALGO = "AES/CBC/PKCS5Padding";
    private static final int IV_LENGTH = 16; // bytes

    private final SecretKeySpec keySpec;
    private final SecureRandom random = new SecureRandom();

    /**
     * @param base64Key clave AES de 128/192/256 bits codificada en Base64
     */
    public EncryptionService(@Value("${encryption.key}") String base64Key) {
        byte[] key = Base64.getDecoder().decode(base64Key);
        this.keySpec = new SecretKeySpec(key, "AES");
    }

    /**
     * Cifra el texto plano y devuelve IV || cipherText
     */
    public byte[] encrypt(String plain) {
        try {
            Cipher cipher = Cipher.getInstance(ALGO);

            // Generar IV aleatorio
            byte[] iv = new byte[IV_LENGTH];
            random.nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] cipherBytes = cipher.doFinal(plain.getBytes("UTF-8"));

            // Prepend IV al ciphertext
            byte[] result = new byte[IV_LENGTH + cipherBytes.length];
            System.arraycopy(iv, 0, result, 0, IV_LENGTH);
            System.arraycopy(cipherBytes, 0, result, IV_LENGTH, cipherBytes.length);
            return result;

        } catch (Exception e) {
            throw new RuntimeException("Error al encriptar", e);
        }
    }

    /**
     * Desencripta los datos formateados como IV || cipherText
     */
    public String decrypt(byte[] ivAndCipher) {
        try {
            Cipher cipher = Cipher.getInstance(ALGO);

            // Extraer IV
            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(ivAndCipher, 0, iv, 0, IV_LENGTH);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            // Extraer ciphertext
            int cipherLen = ivAndCipher.length - IV_LENGTH;
            byte[] cipherBytes = new byte[cipherLen];
            System.arraycopy(ivAndCipher, IV_LENGTH, cipherBytes, 0, cipherLen);

            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] plainBytes = cipher.doFinal(cipherBytes);
            return new String(plainBytes, "UTF-8");

        } catch (Exception e) {
            throw new RuntimeException("Error al desencriptar", e);
        }
    }
}
