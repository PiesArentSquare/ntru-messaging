package piesarentsquare.backend.web.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import piesarentsquare.ntru.NTRU;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class NtruLoginFilter extends UsernamePasswordAuthenticationFilter {
    private final ObjectMapper mapper = new ObjectMapper();
    private final EncryptionManager encryptionManager;

    public NtruLoginFilter(AuthenticationManager authenticationManager, String route, EncryptionManager encryptionManager) {
        this.encryptionManager = encryptionManager;
        setAuthenticationManager(authenticationManager);
        setFilterProcessesUrl(route);
        setAuthenticationSuccessHandler((req, res, auth) -> {
            var session = req.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
            res.setStatus(HttpServletResponse.SC_OK);
        });
        setAuthenticationFailureHandler(
                (req, res, e) -> res.setStatus(HttpServletResponse.SC_UNAUTHORIZED)
        );
    }

    record UserDetails(String username, String password) {}

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try (var reader = new BufferedReader(new InputStreamReader(request.getInputStream()))) {
            String encrypted = reader.lines().collect(Collectors.joining());
            String decrypted = encryptionManager.decrypt(request.getSession().getId(), encrypted);
            UserDetails user = mapper.readValue(decrypted, UserDetails.class);
            var authRequest = new UsernamePasswordAuthenticationToken(user.username, user.password);
            setDetails(request, authRequest);
            return getAuthenticationManager().authenticate(authRequest);
        } catch (IOException e) {
            throw new AuthenticationServiceException("could not decrypt login parameters");
        } finally {
            encryptionManager.dispose(request.getSession().getId());
        }
    }
}
