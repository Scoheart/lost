package com.community.lostandfound.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 帖子评论分页DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostCommentPageDto {
    private List<PostCommentDto> comments;
    private int currentPage;
    private int pageSize;
    private int totalItems;
    private int totalPages;
} 