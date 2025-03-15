package com.community.lostandfound.dto.announcement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 公告数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementDto {
    
    /**
     * 公告ID
     */
    private Long id;
    
    /**
     * 公告标题
     */
    private String title;
    
    /**
     * 公告内容
     */
    private String content;
    
    /**
     * 管理员ID
     */
    private Long adminId;
    
    /**
     * 管理员名称
     */
    private String adminName;
    
    /**
     * 是否置顶
     */
    private Boolean isSticky;
    
    /**
     * 公告状态: published(已发布), draft(草稿)
     */
    private String status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
} 