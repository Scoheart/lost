package com.community.lostandfound.controller;

import com.community.lostandfound.dto.common.ApiResponse;
import com.community.lostandfound.dto.report.ReportDto;
import com.community.lostandfound.dto.report.ReportRequest;
import com.community.lostandfound.entity.Report;
import com.community.lostandfound.security.UserDetailsImpl;
import com.community.lostandfound.service.ReportService;
import com.community.lostandfound.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final UserService userService;

    /**
     * 创建举报
     */
    @PostMapping
    @PreAuthorize("hasRole('RESIDENT') or hasRole('ADMIN') or hasRole('SYSADMIN')")
    public ResponseEntity<ApiResponse<ReportDto>> createReport(@Valid @RequestBody ReportRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = getUserIdFromAuthentication(auth);
        
        log.info("用户 {} 创建举报: {}", userId, request);
        ReportDto createdReport = reportService.createReport(request, userId);
        
        return ResponseEntity.ok(ApiResponse.success("举报提交成功", createdReport));
    }

    /**
     * 获取用户自己提交的举报列表
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('RESIDENT') or hasRole('ADMIN') or hasRole('SYSADMIN')")
    public ResponseEntity<ApiResponse<List<ReportDto>>> getMyReports() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = getUserIdFromAuthentication(auth);
        
        List<ReportDto> reports = reportService.getReportsByReporter(userId);
        return ResponseEntity.ok(ApiResponse.success("获取举报记录成功", reports));
    }

    /**
     * 获取针对特定内容的举报（仅管理员可用）
     */
    @GetMapping("/item/{type}/{itemId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYSADMIN')")
    public ResponseEntity<ApiResponse<List<ReportDto>>> getReportsByItem(
            @PathVariable String type,
            @PathVariable Long itemId
    ) {
        Report.ReportType reportType;
        try {
            reportType = Report.ReportType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("无效的举报类型，有效值: LOST_ITEM, FOUND_ITEM, COMMENT", HttpStatus.BAD_REQUEST)
            );
        }
        
        List<ReportDto> reports = reportService.getReportsByItem(reportType, itemId);
        return ResponseEntity.ok(ApiResponse.success("获取内容举报记录成功", reports));
    }
    
    /**
     * 从Authentication对象中获取用户ID
     */
    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            return userDetails.getId();
        }
        return null;
    }
} 