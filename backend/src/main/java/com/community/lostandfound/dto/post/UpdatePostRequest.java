package com.community.lostandfound.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新论坛帖子请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePostRequest {
    @NotBlank(message = "标题不能为空")
    @Size(min = 2, max = 100, message = "标题长度必须在2-100个字符之间")
    private String title;

    @NotBlank(message = "内容不能为空")
    @Size(min = 5, max = 5000, message = "内容长度必须在5-5000个字符之间")
    private String content;
} 