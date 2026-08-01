package com.dch.dchelearning.user;

import java.time.Instant;

public record UserResponse(
    Long id,
    String email,
    String role,
    Instant createdAt
) {
    public static UserResponse fromEntity(UserEntity user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getRole(), user.getCreatedAt());
    }
}
