package www.aaltogetherbackend.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import www.aaltogetherbackend.models.User;
import www.aaltogetherbackend.payloads.requests.UpdatePasswordRequest;
import www.aaltogetherbackend.payloads.requests.UpdateUserRequest;
import www.aaltogetherbackend.payloads.responses.ErrorMessageResponse;
import www.aaltogetherbackend.payloads.responses.LoginResponse;
import www.aaltogetherbackend.payloads.responses.MessageResponse;
import www.aaltogetherbackend.repositories.UserRepository;
import www.aaltogetherbackend.services.EmailConfirmationTokenService;
import www.aaltogetherbackend.services.JwtUtils;
import www.aaltogetherbackend.services.MailService;
import www.aaltogetherbackend.services.RefreshTokenService;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final EmailConfirmationTokenService emailConfirmationTokenService;
    private final MailService mailService;
    private final RefreshTokenService refreshTokenService;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils, EmailConfirmationTokenService emailConfirmationTokenService, MailService mailService, RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.emailConfirmationTokenService = emailConfirmationTokenService;
        this.mailService = mailService;
        this.refreshTokenService = refreshTokenService;
    }

    @Value("${FRONTEND_PORT}")
    private String frontPort;

    @Value("${FRONTEND_IP}")
    private String frontIp;

    @GetMapping("/me")
    public ResponseEntity<?> me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok().body(userRepository.findUserInfoByUsername(user.getUsername()));
    }

    @PatchMapping("/update-password")
    public ResponseEntity<?> updatePassword(@Valid @RequestBody UpdatePasswordRequest updatePasswordRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        if (!user.isEmailVerified())
            return ResponseEntity.badRequest().body(new ErrorMessageResponse("Please verify your email first"));

        if (!passwordEncoder.matches(updatePasswordRequest.oldPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body(new ErrorMessageResponse("Old password is incorrect"));
        }

        user.setPassword(passwordEncoder.encode(updatePasswordRequest.newPassword()));
        userRepository.save(user);

        return ResponseEntity.ok().body(new MessageResponse("Password updated successfully"));
    }

    @PatchMapping("/update")
    public ResponseEntity<?> update(@Valid @RequestBody UpdateUserRequest updateUserRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        user.setUsername(updateUserRequest.username());
        if (!user.getEmail().equals(updateUserRequest.email())) {
            user.setEmailVerified(false);
            user.setEmail(updateUserRequest.email());
            String token = emailConfirmationTokenService.generateEmailVerificationToken(user);
            String link = "http://" + frontIp + ":" + frontPort + "/verify-email?token=" + token;
            mailService.SendMail(updateUserRequest.email(), "Email Verification", "<a href=\"" + link + "\">Click here to verify your email</a>");
        }

        userRepository.save(user);

        String jwt = jwtUtils.generateToken(user.getUsername(), user.getId());

        return ResponseEntity.ok().body(new LoginResponse("User updated successfully", jwt, null, userRepository.findUserInfoByUsername(updateUserRequest.username())));
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        refreshTokenService.deleteByUser(user);

        return ResponseEntity.ok().body(new MessageResponse("You've been signed out!"));
    }

}
