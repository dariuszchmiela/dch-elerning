package com.dch.dchelearning.user;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(value = "/{version}/users/register", version = "1.0")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        UserEntity user = userService.register(request.email(), request.password(), request.role());
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.fromEntity(user));
    }

    @PostMapping(value = "/{version}/users/login", version = "1.0")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest request) {
        String token = userService.login(request.email(), request.password());
        return ResponseEntity.ok(Map.of("token", token));
    }

    @GetMapping(value = "/{version}/users/me", version = "1.0")
    public ResponseEntity<UserResponse> me(Authentication authentication) {
        String email = authentication.getName();
        UserEntity user = userService.findByEmail(email);
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }
}
