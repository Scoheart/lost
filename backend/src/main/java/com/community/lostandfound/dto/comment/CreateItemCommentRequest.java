package com.community.lostandfound.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建物品评论请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateItemCommentRequest {
    
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 500, message = "评论内容不能超过500个字符")
    private String content;
    
    @NotNull(message = "物品ID不能为空")
    private Long itemId;
    
    @NotBlank(message = "物品类型不能为空")
    @Size(min = 4, max = 5, message = "物品类型必须为'lost'或'found'")
    private String itemType; // "lost"(寻物启事), "found"(失物招领)
} 