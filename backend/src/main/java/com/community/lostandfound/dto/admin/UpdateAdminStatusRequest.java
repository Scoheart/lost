package com.community.lostandfound.dto.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating the status of an admin user
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAdminStatusRequest {
    @NotNull(message = "账号状态不能为空")
    private Boolean isLocked;
    
    // 添加getter和setter以支持前端发送的isEnabled字段
    @JsonProperty("isEnabled")
    public void setIsEnabled(Boolean isEnabled) {
        this.isLocked = isEnabled == null ? null : !isEnabled;
    }
    
    // 添加getter和setter以兼容isActive字段（如果有使用）
    @JsonProperty("isActive")
    public void setIsActive(Boolean isActive) {
        this.isLocked = isActive == null ? null : !isActive;
    }
} 