package com.community.lostandfound.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for returning user profile information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    private Long id;
    private String username;
    private String email;
    private String role;
    private String avatar;
    private String phone;
    private String realName;
    private String address;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 