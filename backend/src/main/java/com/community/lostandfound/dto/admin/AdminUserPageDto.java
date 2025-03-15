package com.community.lostandfound.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for paginated admin user lists
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserPageDto {
    private List<AdminUserDto> items;
    private int currentPage;
    private int pageSize;
    private long totalItems;
    private int totalPages;
} 