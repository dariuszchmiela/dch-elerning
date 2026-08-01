package com.dch.dchelearning.user;

import com.dch.dchelearning.config.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

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
            throw new UserAlreadyExistsException(email);
        }
        String hashedPassword = passwordEncoder.encode(rawPassword);
        UserEntity user = new UserEntity(email, hashedPassword, role);
        return userRepository.save(user);
    }

    public String login(String email, String rawPassword) {
        UserEntity user = userRepository.findByEmail(email)
            .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        return jwtService.generateToken(user.getEmail());
    }
}
