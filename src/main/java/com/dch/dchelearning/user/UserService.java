package com.dch.dchelearning.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserEntity register(String email, String rawPassword, String role) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException(email);
        }
        String hashedPassword = passwordEncoder.encode(rawPassword);
        UserEntity user = new UserEntity(email, hashedPassword, role);
        return userRepository.save(user);
    }
}
