package com.community.lostandfound.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 评论分页DTO
 * @deprecated 已被拆分，保留此类是为了保持兼容性
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Deprecated
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
    private int totalItems;
    
    /**
     * 总页数
     */
    private int totalPages;
} 