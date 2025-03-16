package com.community.lostandfound.dto.claim;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 认领申请DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimApplicationDto {
    /**
     * 认领申请ID
     */
    private Long id;
    
    /**
     * 失物招领ID
     */
    private Long foundItemId;
    
    /**
     * 失物招领标题
     */
    private String foundItemTitle;
    
    /**
     * 失物招领图片
     */
    private String foundItemImage;
    
    /**
     * 申请人ID
     */
    private Long applicantId;
    
    /**
     * 申请人用户名
     */
    private String applicantName;
    
    /**
     * 申请人联系方式
     */
    private String applicantContact;
    
    /**
     * 失物招领发布者ID
     */
    private Long ownerId;
    
    /**
     * 失物招领发布者用户名
     */
    private String ownerName;
    
    /**
     * 申请说明
     */
    private String description;
    
    /**
     * 申请状态: pending(待处理), approved(已同意), rejected(已拒绝)
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
} 