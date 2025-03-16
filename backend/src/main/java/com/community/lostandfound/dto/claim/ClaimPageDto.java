package com.community.lostandfound.dto.claim;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 认领申请分页DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimPageDto {
    /**
     * 认领申请列表
     */
    private List<ClaimApplicationDto> applications;
    
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