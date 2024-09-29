package www.aaltogetherbackend.services;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtUtils {

    public String generateToken(String username, UUID userId) {
        return Jwts.builder()
                .subject(username)
                .claim("id", userId)
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + 3600000))
                .signWith(jwtSecret)
                .compact();
    }

    public boolean verifyToken(String token) {
        try {
            Jwts.parser().verifyWith(jwtSecret).build().parseSignedClaims(token);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    public String getJwtFromRequest(HttpServletRequest request) {
        String jwt = request.getHeader("Authorization");
        if (jwt != null && jwt.startsWith("Bearer ")) {
            return jwt.substring(7);
        }
        return null;
    }

    public boolean isExpired(String token) {
        try {
            Jwts.parser().verifyWith(jwtSecret).build().parseSignedClaims(token);
            return false;
        }
        catch (ExpiredJwtException e) {
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    public UUID getIdFromToken(String token) {
        return UUID.fromString(Jwts.parser().verifyWith(jwtSecret).build()
                .parseSignedClaims(token).getPayload().get("id", String.class));
    }

    private final SecretKey jwtSecret = Jwts.SIG.HS256.key().build();
}
