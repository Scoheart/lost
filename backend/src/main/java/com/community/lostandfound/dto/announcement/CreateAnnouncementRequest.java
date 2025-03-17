package com.community.lostandfound.dto.announcement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 创建公告请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAnnouncementRequest {
    
    /**
     * 公告标题
     */
    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题长度不能超过100个字符")
    private String title;
    
    /**
     * 公告内容
     */
    @NotBlank(message = "内容不能为空")
    @Size(max = 10000, message = "内容长度不能超过10000个字符")
    private String content;
    
    /**
     * 公告状态: published(已发布), draft(草稿)
     */
    private String status = "published";
} 