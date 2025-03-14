package com.community.lostandfound.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String username;
    private String email;
    private String password;
    private String role; // 'resident', 'admin', 'sysadmin'
    private String avatar;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isEnabled;
    private Boolean isLocked;
} 