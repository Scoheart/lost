package com.community.lostandfound.dto.report;

import com.community.lostandfound.entity.Report;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDto {
    
    private Long id;
    
    // 举报类型
    private Report.ReportType reportType;
    
    // 被举报内容ID
    private Long reportedItemId;
    
    // 被举报内容标题或摘要 (根据类型不同而不同)
    private String reportedItemTitle;
    
    // 被举报内容的详细内容（例如：评论的实际内容）
    private String reportedItemContent;
    
    // 举报者ID
    private Long reporterId;
    
    // 举报者用户名
    private String reporterUsername;
    
    // 被举报者ID
    private Long reportedUserId;
    
    // 被举报者用户名
    private String reportedUsername;
    
    // 举报原因
    private String reason;
    
    // 举报状态
    private Report.ReportStatus status;
    
    // 管理员处理结果说明
    private String resolutionNotes;
    
    // 处理该举报的管理员ID
    private Long resolvedByAdminId;
    
    // 处理举报的管理员用户名
    private String resolvedByAdminUsername;
    
    // 举报创建时间
    private LocalDateTime createdAt;
    
    // 举报处理时间
    private LocalDateTime resolvedAt;
} 