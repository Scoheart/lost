package com.community.lostandfound.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    private Long id;
    private String content;
    private Long userId;
    private Long itemId;
    private String itemType; // 'lost' or 'found'
    private LocalDateTime createdAt;
    
    // Transient fields for responses
    private String username;
} 