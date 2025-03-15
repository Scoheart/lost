package com.community.lostandfound.dto.auth;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {
    
    @NotBlank(message = "用户名或邮箱不能为空")
    @Size(min = 3, max = 100, message = "用户名或邮箱长度必须在3到100个字符之间")
    @JsonAlias({"username"})
    private String usernameOrEmail;
    
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度必须在6到100个字符之间")
    private String password;
} 