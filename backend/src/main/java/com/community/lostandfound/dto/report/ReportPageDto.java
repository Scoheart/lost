package com.community.lostandfound.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportPageDto {
    
    // 举报列表
    private List<ReportDto> reports;
    
    // 总记录数
    private long totalItems;
    
    // 总页数
    private int totalPages;
    
    // 当前页码
    private int currentPage;
    
    // 每页大小
    private int pageSize;
    
    // 待处理举报数量
    private long pendingReportsCount;
} 