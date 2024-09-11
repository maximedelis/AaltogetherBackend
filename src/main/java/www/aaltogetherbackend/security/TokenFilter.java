package www.aaltogetherbackend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import www.aaltogetherbackend.models.User;
import www.aaltogetherbackend.services.JwtUtils;
import www.aaltogetherbackend.services.UserService;

import java.io.IOException;

public class TokenFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserService userService;

    public TokenFilter(final JwtUtils jwtUtils, UserService userService) {
        this.jwtUtils = jwtUtils;
        this.userService = userService;
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

            if (jwtUtils.isExpired(token)) {
                System.out.println("token is expired");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token is expired");
                return;
            }

            if (token != null && jwtUtils.verifyToken(token)) {
                System.out.println("token is valid");

                String username = jwtUtils.getUsernameFromToken(token);
                User user = userService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

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
