package com.community.lostandfound.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建留言请求DTO
 */
@Data
public class CreateCommentRequest {
    
    /**
     * 留言内容
     */
    @NotBlank(message = "留言内容不能为空")
    @Size(max = 500, message = "留言内容不能超过500个字符")
    private String content;
    
    /**
     * 关联的物品ID
     */
    @NotNull(message = "物品ID不能为空")
    private Long itemId;
    
    /**
     * 物品类型：lost - 寻物启事，found - 失物招领
     */
    @NotBlank(message = "物品类型不能为空")
    @Size(min = 4, max = 5, message = "物品类型必须为'lost'或'found'")
    private String itemType;
} 