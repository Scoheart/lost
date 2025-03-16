package com.community.lostandfound.dto.report;

import com.community.lostandfound.entity.Report;
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
public class ReportResolutionRequest {
    
    /**
     * 举报处理状态：RESOLVED(已处理) 或 REJECTED(已驳回)
     */
    @NotNull(message = "处理状态不能为空")
    private Report.ReportStatus status;
    
    /**
     * 处理结果说明
     */
    @Size(max = 500, message = "处理结果说明不能超过500个字符")
    private String resolutionNotes;
    
    /**
     * 处理方式 - 只有当status为RESOLVED时有效
     * 可能的值：
     * - NONE: 不采取行动
     * - CONTENT_DELETE: 删除内容
     * - USER_WARNING: 警告用户
     * - USER_BAN: 封禁用户账号
     * - USER_LOCK: 锁定用户账号
     */
    private ActionType actionType;
    
    /**
     * 如果选择封禁/锁定用户，指定锁定天数
     * 当actionType为USER_BAN或USER_LOCK时有效
     */
    private Integer actionDays;
    
    /**
     * 处理方式枚举
     */
    public enum ActionType {
        NONE,            // 不采取行动
        CONTENT_DELETE,  // 删除内容
        USER_WARNING,    // 警告用户
        USER_BAN,        // 封禁用户账号
        USER_LOCK        // 锁定用户账号
    }
} 