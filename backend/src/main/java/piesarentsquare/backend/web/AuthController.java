package piesarentsquare.backend.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

@RestController
public class AuthController {

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
