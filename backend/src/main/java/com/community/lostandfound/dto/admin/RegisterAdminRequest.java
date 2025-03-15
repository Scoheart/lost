package com.community.lostandfound.dto.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for registering a new administrator (either community admin or system admin)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterAdminRequest {
    
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3到50个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "用户名只能包含字母、数字、下划线和短横线")
    private String username;
    
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;
    
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度必须在6到100个字符之间")
    private String password;
    
    @Size(max = 100, message = "姓名长度不能超过100个字符")
    private String realName;
    
    @Size(max = 20, message = "手机号长度不能超过20个字符")
    private String phone;
    
    @NotBlank(message = "用户类型不能为空")
    @Pattern(regexp = "^(admin|resident)$", message = "用户类型只能是小区管理员(admin)或居民(resident)")
    private String role; // Either "admin" (community admin) or "resident" (resident user)
} 