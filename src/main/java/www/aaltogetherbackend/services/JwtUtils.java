package www.aaltogetherbackend.services;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtUtils {

    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
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
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parser().verifyWith(jwtSecret).build()
                .parseSignedClaims(token).getPayload().getSubject();
    }

    private final SecretKey jwtSecret = Jwts.SIG.HS256.key().build();
}
