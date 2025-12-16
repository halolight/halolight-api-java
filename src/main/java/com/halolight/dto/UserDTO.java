package com.halolight.dto;

import com.halolight.domain.entity.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {

    private String id;
    private String email;
    private String phone;
    private String username;
    private String name;
    private String avatar;
    private UserStatus status;
    private String department;
    private String position;
    private String bio;
    private Instant lastLoginAt;
    private Set<String> roles;
    private Instant createdAt;
    private Instant updatedAt;
}
