package com.community.lostandfound.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Claim {
    private Long id;
    private Long itemId;
    private String itemType; // 'lost' or 'found'
    private Long claimantUserId;
    private Long itemOwnerUserId;
    private String description;
    private String status; // 'pending', 'approved', 'rejected', 'completed'
    private String adminNote;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime processedAt;
    
    // Transient fields for responses
    private String claimantUsername;
    private String itemOwnerUsername;
    private String itemTitle;
} 