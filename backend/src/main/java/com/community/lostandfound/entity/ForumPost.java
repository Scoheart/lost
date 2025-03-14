package com.community.lostandfound.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForumPost {
    private Long id;
    private String title;
    private String content;
    private String images; // Stored as JSON string of image URLs
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Transient fields for responses
    private String username;
    private Integer commentCount;
} 