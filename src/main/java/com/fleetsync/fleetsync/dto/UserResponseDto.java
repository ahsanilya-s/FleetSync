package com.fleetsync.fleetsync.dto;

import com.fleetsync.fleetsync.enums.Role;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class UserResponseDto {
    private Long id;
    private String username;
    private String email;
    private Role role;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
