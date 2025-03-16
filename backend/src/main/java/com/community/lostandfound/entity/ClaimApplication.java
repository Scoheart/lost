package com.community.lostandfound.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 认领申请实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimApplication {
    
    /**
     * 认领申请ID
     */
    private Long id;
    
    /**
     * 失物招领ID
     */
    private Long foundItemId;
    
    /**
     * 申请人ID
     */
    private Long applicantId;
    
    /**
     * 申请说明
     */
    private String description;
    
    /**
     * 申请状态: pending(待处理), approved(已同意), rejected(已拒绝)
     */
    private String status;
    
    /**
     * 认领申请的创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 认领申请的更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 处理时间
     */
    private LocalDateTime processedAt;
    
    /**
     * 申请人用户名（非持久化字段）
     */
    private String applicantName;
    
    /**
     * 申请人联系方式（非持久化字段）
     */
    private String applicantContact;
    
    /**
     * 失物招领标题（非持久化字段）
     */
    private String foundItemTitle;
    
    /**
     * 失物招领发布者ID（非持久化字段）
     */
    private Long foundItemOwnerId;
    
    /**
     * 失物招领发布者用户名（非持久化字段）
     */
    private String foundItemOwnerName;
} 