package com.community.lostandfound.dto.admin;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating the status of an admin user (enable/disable)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAdminStatusRequest {
    @NotNull(message = "启用状态不能为空")
    private Boolean isEnabled;
} 