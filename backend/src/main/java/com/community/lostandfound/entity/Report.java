package com.community.lostandfound.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Report {
    private Long id;
    private String reportType; // 'user', 'lost_item', 'found_item', 'forum_post', 'comment'
    private Long reportedItemId;
    private Long reportedUserId;
    private Long reporterUserId;
    private String reason;
    private String status; // 'pending', 'approved', 'rejected'
    private String adminNote;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime processedAt;
    
    // Transient fields for responses
    private String reporterUsername;
    private String reportedUsername;
} 