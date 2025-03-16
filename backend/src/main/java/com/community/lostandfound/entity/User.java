package com.community.lostandfound.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 电子邮箱
     */
    private String email;
    
    /**
     * 密码（加密后存储）
     */
    private String password;
    
    /**
     * 用户角色: 'resident'(普通用户), 'admin'(管理员), 'sysadmin'(系统管理员)
     */
    private String role;
    
    /**
     * 头像URL
     */
    private String avatar;
    
    /**
     * 联系电话
     */
    private String phone;
    
    /**
     * 真实姓名
     */
    private String realName;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 账号是否启用
     */
    private Boolean isEnabled;
} 