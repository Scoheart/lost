package com.community.lostandfound.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 举报实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Report {
    private Long id;
    
    /**
     * 举报类型: LOST_ITEM(寻物启事), FOUND_ITEM(失物招领), COMMENT(留言)
     */
    private ReportType reportType;
    
    /**
     * 被举报内容的ID (根据reportType可能是寻物启事ID、失物招领ID或留言ID)
     */
    private Long reportedItemId;
    
    /**
     * 举报者ID（用户ID）
     */
    private Long reporterId;
    
    /**
     * 被举报者ID（用户ID）
     */
    private Long reportedUserId;
    
    /**
     * 举报原因
     */
    private String reason;
    
    /**
     * 举报状态: PENDING(待处理), RESOLVED(已处理), REJECTED(已驳回)
     */
    private ReportStatus status;
    
    /**
     * 管理员处理结果说明
     */
    private String resolutionNotes;
    
    /**
     * 处理该举报的管理员ID
     */
    private Long resolvedByAdminId;
    
    /**
     * 举报创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 举报处理时间
     */
    private LocalDateTime resolvedAt;
    
    /**
     * 举报类型枚举
     */
    public enum ReportType {
        LOST_ITEM,    // 寻物启事
        FOUND_ITEM,   // 失物招领
        COMMENT       // 留言
    }
    
    /**
     * 举报状态枚举
     */
    public enum ReportStatus {
        PENDING,     // 待处理
        RESOLVED,    // 已处理
        REJECTED     // 已驳回
    }
} 