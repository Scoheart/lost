package com.community.lostandfound.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 留言基础实体类 - 为ItemComment和PostComment提供共享属性和方法
 */
@Data
public abstract class BaseComment {
    
    /**
     * 留言ID
     */
    protected Long id;
    
    /**
     * 留言内容
     */
    protected String content;
    
    /**
     * 发布留言的用户ID
     */
    protected Long userId;
    
    /**
     * 创建时间
     */
    protected LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    protected LocalDateTime updatedAt;
    
    /**
     * 非持久化字段 - 用户名
     */
    protected String username;
    
    /**
     * 非持久化字段 - 用户头像
     */
    protected String userAvatar;
} 