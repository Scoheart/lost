package com.community.lostandfound.service;

import com.community.lostandfound.dto.report.ReportDto;
import com.community.lostandfound.dto.report.ReportPageDto;
import com.community.lostandfound.dto.report.ReportRequest;
import com.community.lostandfound.dto.report.ReportResolutionRequest;
import com.community.lostandfound.entity.Report;

import java.util.List;

/**
 * 举报服务接口
 */
public interface ReportService {
    
    /**
     * 创建举报
     */
    ReportDto createReport(ReportRequest request, Long reporterId);
    
    /**
     * 获取举报详情
     */
    ReportDto getReportById(Long reportId);
    
    /**
     * 获取举报列表（分页）
     */
    ReportPageDto getReports(
        Integer page, 
        Integer size, 
        Report.ReportStatus status, 
        Report.ReportType type
    );
    
    /**
     * 处理举报
     */
    ReportDto resolveReport(Long reportId, ReportResolutionRequest resolution, Long adminId);
    
    /**
     * 获取用户发起的举报
     */
    List<ReportDto> getReportsByReporter(Long reporterId);
    
    /**
     * 获取针对特定内容的举报
     */
    List<ReportDto> getReportsByItem(Report.ReportType type, Long itemId);
    
    /**
     * 获取待处理举报数量
     */
    long getPendingReportsCount();
    
    /**
     * 根据用户ID删除所有相关举报
     */
    void deleteReportsByUser(Long userId);
    
    /**
     * 获取管理员可见的所有举报列表（带筛选和分页）
     */
    List<ReportDto> getReportsForAdmin(int page, int size, String status, String type, String startDate, String endDate);
    
    /**
     * 统计符合筛选条件的举报数量
     */
    int countReportsForAdmin(String status, String type, String startDate, String endDate);
    
    /**
     * 统计待处理的举报数量
     */
    int countPendingReports();
} 