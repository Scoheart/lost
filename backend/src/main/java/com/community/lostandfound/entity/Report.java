package com.community.lostandfound.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 举报实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Report {
    /**
     * 举报ID
     */
    private Long id;
    
    /**
     * 举报类型: 'user', 'lost_item', 'found_item', 'forum_post', 'comment'
     */
    private String reportType;
    
    /**
     * 被举报项目ID
     */
    private Long reportedItemId;
    
    /**
     * 被举报用户ID（当举报类型为'user'时使用）
     */
    private Long reportedUserId;
    
    /**
     * 举报者ID
     */
    private Long reporterUserId;
    
    /**
     * 举报原因
     */
    private String reason;
    
    /**
     * 举报状态: 'pending', 'approved', 'rejected'
     */
    private String status;
    
    /**
     * 管理员备注
     */
    private String adminNote;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 处理时间
     */
    private LocalDateTime processedAt;
    
    // 以下字段为辅助展示字段，不存储在数据库中
    
    /**
     * 举报者用户名
     */
    private String reporterUsername;
    
    /**
     * 被举报者用户名
     */
    private String reportedUsername;
} 