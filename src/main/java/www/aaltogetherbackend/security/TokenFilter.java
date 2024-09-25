package www.aaltogetherbackend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.web.filter.OncePerRequestFilter;
import www.aaltogetherbackend.models.User;
import www.aaltogetherbackend.payloads.responses.ErrorMessageResponse;
import www.aaltogetherbackend.services.JwtUtils;
import www.aaltogetherbackend.services.UserService;

import java.io.IOException;

public class TokenFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserService userService;
    private final RequestAttributeSecurityContextRepository requestAttributeSecurityContextRepository;

    public TokenFilter(final JwtUtils jwtUtils, UserService userService, RequestAttributeSecurityContextRepository requestAttributeSecurityContextRepository) {
        this.jwtUtils = jwtUtils;
        this.userService = userService;
        this.requestAttributeSecurityContextRepository = requestAttributeSecurityContextRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();

        if (path.startsWith("/api/auth") || path.startsWith("/h2-console") || path.startsWith("/favicon.ico") || path.startsWith("/swagger-ui") || path.startsWith("/v3") || path.startsWith("/api/files/play")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = jwtUtils.getJwtFromRequest(request);

            if (token == null) {
                System.out.println("token is null");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                ObjectMapper objectMapper = new ObjectMapper();
                String jsonResponse = objectMapper.writeValueAsString(new ErrorMessageResponse("Unauthorized"));
                response.getWriter().write(jsonResponse);
                return;
            }

            if (jwtUtils.isValid(token)) {
                System.out.println("token is expired");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                ObjectMapper objectMapper = new ObjectMapper();
                String jsonResponse = objectMapper.writeValueAsString(new ErrorMessageResponse("TOKEN_EXPIRED"));
                response.getWriter().write(jsonResponse);
                return;
            }

            if (jwtUtils.verifyToken(token)) {
                System.out.println("token is valid");

                String username = jwtUtils.getUsernameFromToken(token);
                User user = userService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                this.requestAttributeSecurityContextRepository.saveContext(SecurityContextHolder.getContext(), request, response);

                filterChain.doFilter(request, response);
            } else {
                System.out.println("token is invalid");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Unauthorized");
            }

        } catch (UsernameNotFoundException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized");
        } catch (Exception e) {
            logger.error("Cannot set authentication:", e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized");
        }

    }

}
