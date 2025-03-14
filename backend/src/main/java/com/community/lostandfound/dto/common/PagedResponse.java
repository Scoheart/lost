package com.community.lostandfound.dto.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {
    private List<T> items;
    private int page;
    private int pageSize;
    private int totalPages;
    private long total;
    
    public static <T> PagedResponse<T> of(List<T> items, int page, int pageSize, long total) {
        return new PagedResponse<>(
                items, 
                page, 
                pageSize, 
                calculateTotalPages(total, pageSize), 
                total
        );
    }
    
    private static int calculateTotalPages(long total, int pageSize) {
        return pageSize > 0 ? (int) Math.ceil((double) total / pageSize) : 0;
    }
} 