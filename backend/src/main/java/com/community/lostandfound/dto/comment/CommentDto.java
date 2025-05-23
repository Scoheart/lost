package com.community.lostandfound.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 留言DTO
 * @deprecated 已被拆分为ItemCommentDto和PostCommentDto，保留此类是为了保持兼容性
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Deprecated
public class CommentDto {
    
    /**
     * 留言ID
     */
    private Long id;
    
    /**
     * 留言内容
     */
    private String content;
    
    /**
     * 关联的物品ID
     */
    private Long itemId;
    
    /**
     * 物品类型：lost - 寻物启事，found - 失物招领
     */
    private String itemType;
    
    /**
     * 发布留言的用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 用户头像
     */
    private String userAvatar;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
} 