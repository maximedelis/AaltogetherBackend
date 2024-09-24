package www.aaltogetherbackend.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import www.aaltogetherbackend.models.EmailConfirmationToken;
import www.aaltogetherbackend.models.RefreshToken;
import www.aaltogetherbackend.models.User;
import www.aaltogetherbackend.payloads.requests.LoginRequest;
import www.aaltogetherbackend.payloads.requests.RefreshTokenRequest;
import www.aaltogetherbackend.payloads.requests.SignupRequest;
import www.aaltogetherbackend.payloads.responses.ErrorMessageResponse;
import www.aaltogetherbackend.payloads.responses.LoginResponse;
import www.aaltogetherbackend.payloads.responses.MessageResponse;
import www.aaltogetherbackend.payloads.responses.UserInfoResponse;
import www.aaltogetherbackend.repositories.UserRepository;
import www.aaltogetherbackend.services.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder encoder;
    private final RefreshTokenService refreshTokenService;
    private final MailService mailService;
    private final EmailConfirmationTokenService emailConfirmationTokenService;

    public AuthController(UserRepository userRepository, JwtUtils jwtUtils, AuthenticationManager authenticationManager, PasswordEncoder encoder, RefreshTokenService refreshTokenService, MailService mailService, EmailConfirmationTokenService emailConfirmationTokenService) {
        this.userRepository = userRepository;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
        this.encoder = encoder;
        this.refreshTokenService = refreshTokenService;
        this.mailService = mailService;
        this.emailConfirmationTokenService = emailConfirmationTokenService;
    }

    @Value("${FRONTEND_PORT}")
    private String frontPort;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.username(),
                        loginRequest.password()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateToken(loginRequest.username());

        User user = (User) authentication.getPrincipal();

        if (!user.isEmailVerified()) {
            return ResponseEntity.badRequest().body(new ErrorMessageResponse("Email is not verified!"));
        }

        refreshTokenService.deleteByUser(user);
        String refreshToken = refreshTokenService.generateRefreshToken(loginRequest.username());
        return ResponseEntity.ok().body(new LoginResponse("You've been signed in!", jwt, refreshToken, userRepository.findUserInfoByUsername(loginRequest.username())));
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

        String token = emailConfirmationTokenService.generateEmailVerificationToken(user);

        mailService.SendMail(signupRequest.email(), "Email Verification", "Click here to verify your email: http://localhost:" + frontPort + "/api/auth/verify-email?token=" + token);
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
        return ResponseEntity.ok().body(new LoginResponse("Token refreshed!", jwt, refreshTokenRequest.refreshToken(), userRepository.findUserInfoByUsername(username)));
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        refreshTokenService.deleteByUser(user);

        return ResponseEntity.ok().body(new MessageResponse("You've been signed out!"));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@Valid @RequestParam String token) {
        Optional<EmailConfirmationToken> emailConfirmationToken = emailConfirmationTokenService.findByToken(token);

        if (emailConfirmationToken.isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorMessageResponse("Token is invalid!"));
        }

        if (emailConfirmationTokenService.isExpired(token)) {
            return ResponseEntity.badRequest().body(new ErrorMessageResponse("Token is expired!"));
        }

        User user = emailConfirmationToken.get().getUser();
        user.setEmailVerified(true);
        userRepository.save(user);
        return ResponseEntity.ok().body(new MessageResponse("Email verified!"));
    }

    /*
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return ResponseEntity.badRequest().body(new ErrorMessageResponse("User not found!"));
        }
        String token = UUID.randomUUID().toString();
        userRepository.save(user);
        mailService.SendMail("test@example.com", "Password Reset", "Click here to reset your password: http://localhost:8080/api/auth/reset-password?token=" + token);
        return ResponseEntity.ok().body(new MessageResponse("Password reset email sent!"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestParam String token, @Valid @RequestBody String password) {
        User user = userRepository.findByEmailVerificationToken(token);
        user.setPassword(encoder.encode(password));
        userRepository.save(user);
        return ResponseEntity.ok().body(new MessageResponse("Password reset successfully!"));
    }
    */
}
