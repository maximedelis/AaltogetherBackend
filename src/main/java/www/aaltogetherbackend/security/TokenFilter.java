package www.aaltogetherbackend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import www.aaltogetherbackend.services.JwtUtils;

import java.io.IOException;

public class TokenFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    public TokenFilter(final JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();

        if (path.startsWith("/api/auth") || path.startsWith("/h2-console")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = jwtUtils.getJwtFromRequest(request);

            if (token != null && jwtUtils.verifyToken(token)) {
                System.out.println("token is valid");
                filterChain.doFilter(request, response);
            } else {
                System.out.println("token is invalid");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Unauthorized");
            }

        } catch (Exception e) {
            logger.error("Cannot set authentication: {}", e);
        }

    }

}
