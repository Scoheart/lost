package com.community.lostandfound.dto.user;

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
    
    @Pattern(regexp = "^(resident|admin|sysadmin)$", message = "角色必须是 resident, admin 或 sysadmin")
    private String role;
    
    private Boolean isEnabled;
} 