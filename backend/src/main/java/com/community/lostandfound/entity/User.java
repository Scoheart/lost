package com.community.lostandfound.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
     * 住址
     */
    private String address;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 账号是否被锁定
     * true表示锁定（不可用），false表示正常（可用）
     */
    private Boolean isLocked;
    
    /**
     * 锁定结束时间
     */
    private LocalDateTime lockEndTime;
    
    /**
     * 锁定原因
     */
    private String lockReason;
    
    /**
     * 判断用户是否被锁定
     * @return 如果用户被锁定且锁定期未结束，返回true；否则返回false（账户可用）
     */
    public boolean isLocked() {
        if (Boolean.TRUE.equals(isLocked) && lockEndTime != null) {
            return lockEndTime.isAfter(LocalDateTime.now());
        }
        return Boolean.TRUE.equals(isLocked);
    }
} 