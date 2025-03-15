package com.community.lostandfound.dto.announcement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 公告分页数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementPageDto {
    
    /**
     * 公告列表
     */
    private List<AnnouncementDto> announcements;
    
    /**
     * 当前页码
     */
    private int currentPage;
    
    /**
     * 每页条数
     */
    private int pageSize;
    
    /**
     * 总页数
     */
    private int totalPages;
    
    /**
     * 总记录数
     */
    private long totalItems;
} 