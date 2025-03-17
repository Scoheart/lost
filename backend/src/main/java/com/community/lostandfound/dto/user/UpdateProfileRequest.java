package com.community.lostandfound.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO for updating user profile information
 */
@Data
public class UpdateProfileRequest {
    @Email(message = "请提供有效的邮箱地址")
    private String email;
    
    private String phone;
    
    private String avatar;
} 