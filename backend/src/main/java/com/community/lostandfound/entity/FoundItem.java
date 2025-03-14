package com.community.lostandfound.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FoundItem {
    private Long id;
    private String title;
    private String description;
    private LocalDate foundDate;
    private String foundLocation;
    private String category;
    private String images; // Stored as JSON string of image URLs
    private String contactInfo;
    private String status; // 'pending', 'claimed', 'closed'
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Transient fields for responses
    private String username;
} 