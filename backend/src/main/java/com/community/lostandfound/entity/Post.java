package com.community.lostandfound.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 社区论坛帖子实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    private Long id;
    
    private String title;
    
    private String content;
    
    private Long userId;
    
    private String username;
    
    private String userAvatar;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
} 