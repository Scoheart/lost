package com.community.lostandfound.dto.report;

import com.community.lostandfound.entity.Report;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequest {
    
    /**
     * 举报类型: LOST_ITEM(寻物启事), FOUND_ITEM(失物招领), COMMENT(留言)
     */
    @NotNull(message = "举报类型不能为空")
    private Report.ReportType reportType;
    
    /**
     * 被举报内容的ID (根据reportType可能是寻物启事ID、失物招领ID或留言ID)
     */
    @NotNull(message = "被举报内容ID不能为空")
    private Long reportedItemId;
    
    /**
     * 被举报者ID (由后端自动从被举报内容获取)
     */
    private Long reportedUserId;
    
    /**
     * 举报原因
     */
    @NotBlank(message = "举报原因不能为空")
    @Size(min = 5, max = 500, message = "举报原因长度必须在5-500个字符之间")
    private String reason;
} 