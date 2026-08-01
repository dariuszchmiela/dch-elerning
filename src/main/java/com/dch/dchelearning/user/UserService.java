package com.dch.dchelearning.user;

import com.dch.dchelearning.config.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public UserEntity register(String email, String rawPassword, String role) {
        if (userRepository.findByEmail(email).isPresent()) {
            log.warn("Registration attempt with existing email: {}", email);
            throw new UserAlreadyExistsException(email);
        }
        String hashedPassword = passwordEncoder.encode(rawPassword);
        UserEntity user = new UserEntity(email, hashedPassword, role);
        UserEntity saved = userRepository.save(user);
        log.info("User registered: {}", email);
        return saved;
    }

    public String login(String email, String rawPassword) {
        UserEntity user = userRepository.findByEmail(email)
            .orElseThrow(() -> {
                log.warn("Login attempt with unknown email: {}", email);
                return new InvalidCredentialsException();
            });

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            log.warn("Login attempt with incorrect password for email: {}", email);
            throw new InvalidCredentialsException();
        }

        log.info("User logged in: {}", email);
        return jwtService.generateToken(user.getEmail());
    }

    public UserEntity findByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> {
                log.warn("User not found for email: {}", email);
                return new InvalidCredentialsException();
            });
    }
}
