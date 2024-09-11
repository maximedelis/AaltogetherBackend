package www.aaltogetherbackend.controllers;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import www.aaltogetherbackend.models.RefreshToken;
import www.aaltogetherbackend.models.User;
import www.aaltogetherbackend.payloads.requests.LoginRequest;
import www.aaltogetherbackend.payloads.requests.RefreshTokenRequest;
import www.aaltogetherbackend.payloads.requests.SignupRequest;
import www.aaltogetherbackend.payloads.responses.ErrorMessageResponse;
import www.aaltogetherbackend.payloads.responses.LoginResponse;
import www.aaltogetherbackend.payloads.responses.MessageResponse;
import www.aaltogetherbackend.repositories.UserRepository;
import www.aaltogetherbackend.services.JwtUtils;
import www.aaltogetherbackend.services.RefreshTokenService;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder encoder;
    private final RefreshTokenService refreshTokenService;

    public AuthController(UserRepository userRepository, JwtUtils jwtUtils, AuthenticationManager authenticationManager, PasswordEncoder encoder, RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
        this.encoder = encoder;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.username(),
                        loginRequest.password()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateToken(loginRequest.username());

        User user = (User) authentication.getPrincipal();
        refreshTokenService.deleteByUser(user);
        String refreshToken = refreshTokenService.generateRefreshToken(loginRequest.username());
        return ResponseEntity.ok().body(new LoginResponse("You've been signed in!", jwt, refreshToken));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        if (userRepository.existsByUsername(signupRequest.username())) {
            return ResponseEntity.badRequest().body(new ErrorMessageResponse("Username is already taken!"));
        }
        if (userRepository.existsByEmail(signupRequest.email())) {
            return ResponseEntity.badRequest().body(new ErrorMessageResponse("Email is already in use!"));
        }
        User user = new User();
        user.setUsername(signupRequest.username());
        user.setPassword(encoder.encode(signupRequest.password()));
        user.setEmail(signupRequest.email());
        userRepository.save(user);
        return ResponseEntity.ok().body(new MessageResponse("User registered successfully!"));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        Optional<RefreshToken> refreshToken = refreshTokenService.findByToken(refreshTokenRequest.refreshToken());

        if (refreshToken.isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorMessageResponse("Refresh token is invalid!"));
        }
        if (refreshTokenService.isExpired(refreshTokenRequest.refreshToken())) {
            return ResponseEntity.badRequest().body(new ErrorMessageResponse("Refresh token is expired!"));
        }
        String username = refreshToken.get().getUser().getUsername();

        String jwt = jwtUtils.generateToken(username);
        return ResponseEntity.ok().body(new LoginResponse("Token refreshed!", jwt, refreshTokenRequest.refreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        refreshTokenService.deleteByUser(user);

        return ResponseEntity.ok().body(new MessageResponse("You've been signed out!"));
    }

}
