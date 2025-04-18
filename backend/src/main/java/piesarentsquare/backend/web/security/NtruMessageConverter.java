package piesarentsquare.backend.web.security;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class NtruMessageConverter extends MappingJackson2MessageConverter {

    @Override
    protected Object convertFromInternal(Message<?> message, Class<?> targetClass, Object conversionHint) {
        try {
            String encrypted = new String((byte[]) message.getPayload(), StandardCharsets.UTF_8);
            String decrypted = Encryption.decrypt(encrypted);

            return getObjectMapper().readValue(decrypted, targetClass);
        } catch (IOException e) {
            throw new MessageConversionException("Failed to decrypt and deserialize message", e);
        }
    }

    @Override
    protected Object convertToInternal(Object payload, MessageHeaders headers, Object conversionHint) {
        try {
            String json = getObjectMapper().writeValueAsString(payload);
            String encrypted = Encryption.encrypt(json);
            return encrypted.getBytes(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new MessageConversionException("Failed to convert message", e);
        }
    }
}
