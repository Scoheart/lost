package com.community.lostandfound.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * DTO for admin to update user profile information
 */
@Data
public class UpdateUserAdminRequest {
    @Email(message = "请提供有效的邮箱地址")
    private String email;
    
    private String phone;
    
    private String realName;
    
    private String avatar;
    
    private String address;
    
    @Pattern(regexp = "^(resident|admin|sysadmin)$", message = "角色必须是 resident, admin 或 sysadmin")
    private String role;
    
    private Boolean isLocked; // 账号是否被锁定
    
    // 为了兼容前端代码，提供isEnabled/isActive转换
    @JsonProperty("isEnabled")
    public void setIsEnabled(Boolean isEnabled) {
        this.isLocked = isEnabled == null ? null : !isEnabled;
    }
    
    @JsonProperty("isActive")
    public void setIsActive(Boolean isActive) {
        this.isLocked = isActive == null ? null : !isActive;
    }
} 