package com.community.lostandfound.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
    private String token;
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