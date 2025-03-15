package com.community.lostandfound.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 留言分页数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentPageDto {
    
    /**
     * 留言列表
     */
    private List<CommentDto> comments;
    
    /**
     * 当前页码
     */
    private int currentPage;
    
    /**
     * 每页条数
     */
    private int pageSize;
    
    /**
     * 总条数
     */
    private long totalItems;
    
    /**
     * 总页数
     */
    private int totalPages;
} 