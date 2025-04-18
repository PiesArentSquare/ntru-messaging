package piesarentsquare.backend.model;

import java.security.Principal;

public record ChatMessage(String from, String to, String content) {
    public ChatMessage(ReceivedMessage message, Principal user) {
        this(user.getName(), message.to(), message.content());
    }
    public record ReceivedMessage(String to, String content) {}
}
