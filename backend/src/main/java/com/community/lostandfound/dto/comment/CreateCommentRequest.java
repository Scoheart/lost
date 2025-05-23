package com.community.lostandfound.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建评论请求DTO
 * @deprecated 已被拆分为CreateItemCommentRequest和CreatePostCommentRequest，保留此类是为了保持兼容性
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Deprecated
public class CreateCommentRequest {
    
    /**
     * 评论内容
     */
    @NotBlank(message = "评论内容不能为空")
    private String content;
    
    /**
     * 关联的物品ID
     */
    @NotNull(message = "物品ID不能为空")
    private Long itemId;
    
    /**
     * 物品类型：lost - 寻物启事，found - 失物招领，post - 论坛帖子
     */
    @NotBlank(message = "物品类型不能为空")
    private String itemType; // "lost"(寻物启事), "found"(失物招领), "post"(论坛帖子)
} 