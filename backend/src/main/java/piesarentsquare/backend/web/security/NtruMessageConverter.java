package piesarentsquare.backend.web.security;

import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import piesarentsquare.backend.web.ChatController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Map;

@Component
public class NtruMessageConverter extends MappingJackson2MessageConverter {

    private final EncryptionManager encryptionManager;

    public NtruMessageConverter(EncryptionManager encryptionManager) {
        this.encryptionManager = encryptionManager;
    }

    @Override
    protected Object convertFromInternal(@NonNull Message<?> message, @NonNull Class<?> targetClass, Object conversionHint) {
        var accessor = StompHeaderAccessor.wrap(message);
        // if it's the key exchange, it's not encrypted
        try {
            if (accessor.getDestination().contains("/publicKey"))
                return getObjectMapper().readValue(new String((byte[]) message.getPayload(), StandardCharsets.UTF_8), targetClass);
        } catch (IOException e) {
            throw new MessageConversionException("Failed to decrypt and deserialize message", e);
        }

        Principal user = accessor.getUser();
        if (user == null)
            throw new MessageConversionException("could not extract user from message");
        try {
            String encrypted = new String((byte[]) message.getPayload(), StandardCharsets.UTF_8);
            String decrypted = encryptionManager.decrypt(user.getName(), encrypted);
            return getObjectMapper().readValue(decrypted, targetClass);
        } catch (IOException e) {
            throw new MessageConversionException("Failed to decrypt and deserialize message", e);
        }
    }

    @Override
    protected Object convertToInternal(@NonNull Object payload, MessageHeaders headers, Object conversionHint) {
        SimpMessageHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(headers, SimpMessageHeaderAccessor.class);
        if (accessor == null) {
            throw new MessageConversionException("accessor is null");
        }

        // if it's the key exchange, it's not encrypted
        if (payload instanceof ChatController.KeyRequest keyRequest)
            return keyRequest.key().getBytes(StandardCharsets.UTF_8);

        String username = (String) accessor.getHeader("username");
        if (username == null)
            throw new MessageConversionException("could not extract user from message");
        try {
            String json = getObjectMapper().writeValueAsString(payload);
            String encrypted = encryptionManager.encrypt(username, json);
            return encrypted.getBytes(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new MessageConversionException("Failed to convert message", e);
        }
    }
}
