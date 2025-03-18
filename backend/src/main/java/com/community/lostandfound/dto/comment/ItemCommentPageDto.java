package com.community.lostandfound.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 物品评论分页DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemCommentPageDto {
    private List<ItemCommentDto> comments;
    private int currentPage;
    private int pageSize;
    private int totalItems;
    private int totalPages;
} 