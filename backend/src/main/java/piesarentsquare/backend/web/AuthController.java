package piesarentsquare.backend.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import piesarentsquare.backend.web.security.EncryptionManager;

import java.security.Principal;
import java.util.Map;

@RestController
public class AuthController {

    private final EncryptionManager encryptionManager;

    public AuthController(EncryptionManager encryptionManager) {
        this.encryptionManager = encryptionManager;
    }

    @PostMapping("/handshake")
    public String handshake(HttpServletRequest request, @RequestBody String clientPublicKey) {
        String sessionId = request.getSession().getId();
        System.out.println("handshake sessionId " + sessionId);
        System.out.println("client key " + clientPublicKey);
        encryptionManager.init(sessionId, clientPublicKey);
        return encryptionManager.getPublicKey(sessionId);
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> currentUser(Principal user) {
        if (user == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        else
            return ResponseEntity.ok(Map.of("username", user.getName()));
    }

    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken token) {
        return token;
    }
}
