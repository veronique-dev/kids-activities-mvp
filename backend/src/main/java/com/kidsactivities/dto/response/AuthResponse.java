package com.kidsactivities.dto.response;

import com.kidsactivities.entity.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AuthResponse {
    private String token;
    private UserResponse user;
}
