package com.community.lostandfound.repository;

import com.community.lostandfound.entity.Report;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 举报数据访问接口
 */
@Mapper
@Repository
public interface ReportRepository {

    /**
     * 插入新举报
     */
    int insert(Report report);
    
    /**
     * 更新举报
     */
    int update(Report report);
    
    /**
     * 根据ID获取举报
     */
    Optional<Report> findById(Long id);
    
    /**
     * 按状态和类型查找举报（分页）
     */
    List<Report> findByStatusAndReportType(
            @Param("status") Report.ReportStatus status, 
            @Param("reportType") Report.ReportType reportType, 
            @Param("offset") int offset, 
            @Param("limit") int limit);
    
    /**
     * 按状态和类型查找举报总数
     */
    long countByStatusAndReportType(
            @Param("status") Report.ReportStatus status, 
            @Param("reportType") Report.ReportType reportType);
    
    /**
     * 按状态查找举报（分页）
     */
    List<Report> findByStatus(
            @Param("status") Report.ReportStatus status, 
            @Param("offset") int offset, 
            @Param("limit") int limit);
    
    /**
     * 按状态查找举报总数
     */
    long countByStatus(@Param("status") Report.ReportStatus status);
    
    /**
     * 按类型查找举报（分页）
     */
    List<Report> findByReportType(
            @Param("reportType") Report.ReportType reportType, 
            @Param("offset") int offset, 
            @Param("limit") int limit);
    
    /**
     * 按类型查找举报总数
     */
    long countByReportType(@Param("reportType") Report.ReportType reportType);
    
    /**
     * 查找所有举报（分页）
     */
    List<Report> findAll(@Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * 查找举报总数
     */
    long count();
    
    /**
     * 查找特定用户的举报（作为举报者）
     */
    List<Report> findByReporterId(
            @Param("reporterId") Long reporterId, 
            @Param("offset") int offset, 
            @Param("limit") int limit);
    
    /**
     * 查找特定用户举报总数（作为举报者）
     */
    long countByReporterId(@Param("reporterId") Long reporterId);
    
    /**
     * 查找针对特定用户的举报（作为被举报者）
     */
    List<Report> findByReportedUserId(
            @Param("reportedUserId") Long reportedUserId, 
            @Param("offset") int offset, 
            @Param("limit") int limit);
    
    /**
     * 查找针对特定用户的举报总数（作为被举报者）
     */
    long countByReportedUserId(@Param("reportedUserId") Long reportedUserId);
    
    /**
     * 查找特定内容的举报
     */
    List<Report> findByReportTypeAndReportedItemId(
            @Param("reportType") Report.ReportType reportType, 
            @Param("reportedItemId") Long reportedItemId);
    
    /**
     * 删除举报
     */
    int deleteById(Long id);
    
    /**
     * 删除用户相关的举报
     */
    int deleteByReporterId(Long reporterId);
    
    /**
     * 删除针对用户的举报
     */
    int deleteByReportedUserId(Long reportedUserId);
    
    /**
     * 通过多个过滤条件查询举报（分页）
     */
    List<Report> findByFilters(
            @Param("status") Report.ReportStatus status,
            @Param("reportType") Report.ReportType reportType,
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate,
            @Param("offset") int offset,
            @Param("limit") int limit);
    
    /**
     * 通过多个过滤条件统计举报数量
     */
    int countByFilters(
            @Param("status") Report.ReportStatus status,
            @Param("reportType") Report.ReportType reportType,
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);
} 