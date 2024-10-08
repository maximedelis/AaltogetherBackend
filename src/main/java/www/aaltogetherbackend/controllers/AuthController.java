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
import www.aaltogetherbackend.models.PasswordResetToken;
import www.aaltogetherbackend.models.RefreshToken;
import www.aaltogetherbackend.models.User;
import www.aaltogetherbackend.payloads.requests.*;
import www.aaltogetherbackend.payloads.responses.ErrorMessageResponse;
import www.aaltogetherbackend.payloads.responses.LoginResponse;
import www.aaltogetherbackend.payloads.responses.MessageResponse;
import www.aaltogetherbackend.repositories.UserRepository;
import www.aaltogetherbackend.services.*;

import java.util.Optional;
import java.util.UUID;

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
    private final PasswordResetTokenService passwordResetTokenService;

    public AuthController(UserRepository userRepository, JwtUtils jwtUtils, AuthenticationManager authenticationManager, PasswordEncoder encoder, RefreshTokenService refreshTokenService, MailService mailService, EmailConfirmationTokenService emailConfirmationTokenService, PasswordResetTokenService passwordResetTokenService) {
        this.userRepository = userRepository;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
        this.encoder = encoder;
        this.refreshTokenService = refreshTokenService;
        this.mailService = mailService;
        this.emailConfirmationTokenService = emailConfirmationTokenService;
        this.passwordResetTokenService = passwordResetTokenService;
    }

    @Value("${FRONTEND_PORT}")
    private String frontPort;

    @Value("${FRONTEND_IP")
    private String frontIp;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.username(),
                        loginRequest.password()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = (User) authentication.getPrincipal();
        UUID userId = user.getId();

        String jwt = jwtUtils.generateToken(loginRequest.username(), userId);

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
        user.setEnabled(false);

        userRepository.save(user);

        String token = emailConfirmationTokenService.generateEmailVerificationToken(user);

        String link = "http://" + frontIp + ":" + frontPort + "/verify-email?token=" + token;
        mailService.SendMail(signupRequest.email(), "Email Verification", "<a href=\"" + link + "\">Click here to verify your email</a>");
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
        UUID userId = refreshToken.get().getUser().getId();

        String jwt = jwtUtils.generateToken(username, userId);
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

        User user = emailConfirmationToken.get().getUser();
        user.setEnabled(true);
        user.setEmailVerified(true);
        userRepository.save(user);
        emailConfirmationTokenService.delete(token);
        return ResponseEntity.ok().body(new MessageResponse("Email verified!"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        User user = userRepository.findUserByEmail(forgotPasswordRequest.email());
        if (user == null) {
            return ResponseEntity.badRequest().body(new ErrorMessageResponse("User not found!"));
        }
        passwordResetTokenService.deleteByUser(user);
        String token = passwordResetTokenService.generatePasswordResetToken(user);

        String link = "http://" + frontIp + ":" + frontPort + "/reset-password?token=" + token;
        mailService.SendMail(user.getEmail(), "Password Reset", "<a href=\"" + link + "\">Click here to reset your password</a>");
        return ResponseEntity.ok().body(new MessageResponse("Password reset email sent! Link is valid for 15 minutes."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        String token = resetPasswordRequest.token();
        String newPassword = resetPasswordRequest.password();

        Optional<PasswordResetToken> passwordResetToken = passwordResetTokenService.findByToken(token);
        if (passwordResetToken.isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorMessageResponse("Invalid token!"));
        }
        if (passwordResetTokenService.isExpired(token)) {
            return ResponseEntity.badRequest().body(new ErrorMessageResponse("Token has expired!"));
        }

        User user = passwordResetToken.get().getUser();
        user.setPassword(encoder.encode(newPassword));
        userRepository.save(user);
        passwordResetTokenService.delete(token);
        return ResponseEntity.ok().body(new MessageResponse("Password has been reset successfully!"));
    }
}
