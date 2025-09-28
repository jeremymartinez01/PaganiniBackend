package espol.integradora.paganinibackend.util;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import jakarta.persistence.AttributeConverter;
import espol.integradora.paganinibackend.service.EncryptionService;

@Converter(autoApply = false)
public class CryptoConverter implements AttributeConverter<String, byte[]> {

    private EncryptionService encryptionService() {
        return SpringContext.getBean(EncryptionService.class);
    }

    @Override
    public byte[] convertToDatabaseColumn(String plain) {
        if (plain == null) return null;
        return encryptionService().encrypt(plain);
    }

    @Override
    public String convertToEntityAttribute(byte[] dbData) {
        if (dbData == null) return null;
        return encryptionService().decrypt(dbData);
    }
}