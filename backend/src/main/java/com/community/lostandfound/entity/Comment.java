package com.community.lostandfound.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 留言实体类
 * 用于寻物启事和失物招领的留言
 * @deprecated 已被拆分为ItemComment和PostComment，保留此类是为了保持兼容性
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Deprecated
public class Comment {
    
    /**
     * 留言ID
     */
    private Long id;
    
    /**
     * 留言内容
     */
    private String content;
    
    /**
     * 关联的物品ID（寻物启事或失物招领的ID）
     */
    private Long itemId;
    
    /**
     * 物品类型：lost - 寻物启事，found - 失物招领, post - 论坛帖子
     */
    private String itemType;
    
    /**
     * 发布留言的用户ID
     */
    private Long userId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 非持久化字段 - 用户名
     */
    private String username;
    
    /**
     * 非持久化字段 - 用户头像
     */
    private String userAvatar;
} 