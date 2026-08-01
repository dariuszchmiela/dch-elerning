package com.dch.dchelearning.user;

import com.dch.dchelearning.config.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void registerShouldSaveUserWithHashedPassword() {
        String email = "test@example.com";
        String rawPassword = "plainPassword123";
        String hashedPassword = "hashedPasswordValue";
        String role = "STUDENT";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(rawPassword)).thenReturn(hashedPassword);
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserEntity result = userService.register(email, rawPassword, role);

        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getPassword()).isEqualTo(hashedPassword);
        assertThat(result.getRole()).isEqualTo(role);
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void registerShouldThrowWhenEmailAlreadyExists() {
        String email = "existing@example.com";
        UserEntity existingUser = new UserEntity(email, "someHash", "STUDENT");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> userService.register(email, "anyPassword", "STUDENT"))
            .isInstanceOf(UserAlreadyExistsException.class)
            .hasMessageContaining(email);
    }
}
