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
     * 申请ID
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
     * 申请描述
     */
    private String description;
    
    /**
     * 申请状态：pending, approved, rejected
     */
    private String status;
    
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
     * 申请人用户名
     */
    private String applicantName;
    
    /**
     * 申请人联系方式
     */
    private String applicantContact;
    
    /**
     * 失物招领标题
     */
    private String foundItemTitle;
    
    /**
     * 失物招领所有者ID
     */
    private Long foundItemOwnerId;
    
    /**
     * 失物招领所有者用户名
     */
    private String foundItemOwnerName;
} 