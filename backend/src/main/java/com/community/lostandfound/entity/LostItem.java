package com.community.lostandfound.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LostItem {
    private Long id;
    private String title;
    private String description;
    private LocalDate lostDate;
    private String lostLocation;
    private String category;
    private String images; // Stored as JSON string of image URLs
    private BigDecimal reward;
    private String contactInfo;
    private String status; // 'pending', 'found', 'closed'
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Transient fields for responses
    private String username;
} 