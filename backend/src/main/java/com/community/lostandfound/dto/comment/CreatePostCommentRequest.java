package com.community.lostandfound.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建帖子评论请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostCommentRequest {
    
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 500, message = "评论内容不能超过500个字符")
    private String content;
    
    @NotNull(message = "帖子ID不能为空")
    private Long postId;
} 