package com.community.lostandfound.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Announcement {
    private Long id;
    private String title;
    private String content;
    private Boolean isImportant;
    private Long userId; // Admin who posted
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Transient fields for responses
    private String username;
} 