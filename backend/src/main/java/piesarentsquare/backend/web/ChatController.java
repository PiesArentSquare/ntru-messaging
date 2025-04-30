package piesarentsquare.backend.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import piesarentsquare.backend.model.ChatMessage;
import piesarentsquare.backend.web.security.EncryptionManager;

import java.security.Principal;

@Controller
public class ChatController {

    private final SimpMessagingTemplate template;
    private final EncryptionManager encryptionManager;

    @Autowired
    public ChatController(SimpMessagingTemplate template, EncryptionManager encryptionManager) {
        this.template = template;
        this.encryptionManager = encryptionManager;
    }

    @MessageMapping("/send")
    public void send(@Payload ChatMessage.ReceivedMessage received, Principal user) {
        var message = new ChatMessage(received, user);
        var accessor = SimpMessageHeaderAccessor.create();
        accessor.setHeader("username", message.to());
        template.convertAndSendToUser(message.to(), "/queue/messages", message, accessor.getMessageHeaders());
    }

    public record KeyRequest(String key) {}

    @MessageMapping("/publicKey")
    @SendToUser("/queue/publicKey")
    public KeyRequest setClientPublicKey(@Payload KeyRequest clientPublicKey, Principal user) {
        encryptionManager.init(user.getName(), clientPublicKey.key);
        return new KeyRequest(encryptionManager.getPublicKey(user.getName()));
    }
}
