package com.traveler.traveler_backend.dto;

import com.traveler.traveler_backend.entity.User;

import java.time.LocalDateTime;

public record UserResponse(
    Long id,
    String email,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
