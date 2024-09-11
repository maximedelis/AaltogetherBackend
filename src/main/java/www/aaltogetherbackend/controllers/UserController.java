package www.aaltogetherbackend.controllers;

import jakarta.validation.Valid;
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
import www.aaltogetherbackend.services.JwtUtils;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    @GetMapping("/me")
    public ResponseEntity<?> me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok().body(user);
    }

    @PatchMapping("/update-password")
    public ResponseEntity<?> updatePassword(@Valid @RequestBody UpdatePasswordRequest updatePasswordRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        System.out.println(updatePasswordRequest.oldPassword());

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
        User currentUser = (User) authentication.getPrincipal();

        currentUser.setUsername(updateUserRequest.username());
        currentUser.setEmail(updateUserRequest.email());
        userRepository.save(currentUser);

        String jwt = jwtUtils.generateToken(currentUser.getUsername());

        return ResponseEntity.ok().body(new LoginResponse("User updated successfully", jwt, null));
    }

}
