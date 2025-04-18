package piesarentsquare.backend.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import piesarentsquare.backend.model.ChatMessage;

import java.security.Principal;

@Controller
public class ChatController {

    private final SimpMessagingTemplate template;

    @Autowired
    public ChatController(SimpMessagingTemplate template) {
        this.template = template;
    }

    @MessageMapping("/send")
    public void send(@Payload ChatMessage.ReceivedMessage received, Principal user) {
        var message = new ChatMessage(received, user);
        template.convertAndSendToUser(message.to(), "/queue/messages", message);
    }
}
