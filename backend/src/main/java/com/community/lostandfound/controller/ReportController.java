package com.community.lostandfound.controller;

import com.community.lostandfound.dto.common.ApiResponse;
import com.community.lostandfound.dto.report.ReportDto;
import com.community.lostandfound.dto.report.ReportRequest;
import com.community.lostandfound.dto.report.ReportResolutionRequest;
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
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

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
    public ResponseEntity<ApiResponse<ReportDto>> createReport(@RequestBody Map<String, Object> requestMap) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = getUserIdFromAuthentication(auth);
        
        log.info("用户 {} 创建举报: {}", userId, requestMap);
        
        // 处理两种不同格式的请求
        ReportRequest reportRequest = convertRequestMap(requestMap);
        
        // 验证请求
        if (reportRequest.getReportType() == null) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("举报类型不能为空", HttpStatus.BAD_REQUEST)
            );
        }
        
        if (reportRequest.getReportedItemId() == null) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("被举报内容ID不能为空", HttpStatus.BAD_REQUEST)
            );
        }
        
        if (reportRequest.getReason() == null || reportRequest.getReason().trim().length() < 5) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("举报原因长度必须在5-500个字符之间", HttpStatus.BAD_REQUEST)
            );
        }
        
        ReportDto createdReport = reportService.createReport(reportRequest, userId);
        
        return ResponseEntity.ok(ApiResponse.success("举报提交成功", createdReport));
    }
    
    /**
     * 将请求体转换为ReportRequest对象
     * 支持两种格式：
     * 1. 新版：{ reportedItemId, reportType, reason }
     * 2. 旧版：{ itemId, itemType, reason, description }
     */
    private ReportRequest convertRequestMap(Map<String, Object> requestMap) {
        ReportRequest reportRequest = new ReportRequest();
        
        // 处理旧版格式
        if (requestMap.containsKey("itemId") && requestMap.containsKey("itemType")) {
            // 设置被举报内容ID
            reportRequest.setReportedItemId(
                    requestMap.get("itemId") instanceof Number 
                            ? ((Number) requestMap.get("itemId")).longValue() 
                            : null
            );
            
            // 设置举报类型
            String itemType = (String) requestMap.get("itemType");
            if (itemType != null) {
                try {
                    reportRequest.setReportType(Report.ReportType.valueOf(itemType.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    log.warn("无效的举报类型: {}", itemType);
                    // 默认为COMMENT类型
                    reportRequest.setReportType(Report.ReportType.COMMENT);
                }
            }
            
            // 合并原因和描述
            String reason = (String) requestMap.get("reason");
            String description = (String) requestMap.get("description");
            if (reason != null && description != null) {
                reportRequest.setReason(reason + ": " + description);
            } else if (reason != null) {
                reportRequest.setReason(reason);
            } else if (description != null) {
                reportRequest.setReason(description);
            }
        } 
        // 处理新版格式
        else if (requestMap.containsKey("reportedItemId") && requestMap.containsKey("reportType")) {
            // 设置被举报内容ID
            reportRequest.setReportedItemId(
                    requestMap.get("reportedItemId") instanceof Number 
                            ? ((Number) requestMap.get("reportedItemId")).longValue() 
                            : null
            );
            
            // 设置举报类型
            String reportType = (String) requestMap.get("reportType");
            if (reportType != null) {
                try {
                    reportRequest.setReportType(Report.ReportType.valueOf(reportType.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    log.warn("无效的举报类型: {}", reportType);
                    // 默认为COMMENT类型
                    reportRequest.setReportType(Report.ReportType.COMMENT);
                }
            }
            
            // 设置举报原因
            String reason = (String) requestMap.get("reason");
            if (reason != null) {
                reportRequest.setReason(reason);
            }
        }
        
        return reportRequest;
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
                    ApiResponse.error("无效的举报类型，有效值: LOST_ITEM, FOUND_ITEM, COMMENT, POST", HttpStatus.BAD_REQUEST)
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

    /**
     * 处理举报
     */
    @PutMapping("/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYSADMIN')")
    public ResponseEntity<ApiResponse<ReportDto>> resolveReport(
            @PathVariable Long id,
            @Valid @RequestBody ReportResolutionRequest request) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long adminId = getUserIdFromAuthentication(auth);
        
        if (adminId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("未授权操作，请重新登录"));
        }
        
        log.info("管理员 {} 处理举报 {}: {}", adminId, id, request);
        
        // 验证处理方式
        if (request.getStatus() == Report.ReportStatus.RESOLVED) {
            if (request.getActionType() == null) {
                return ResponseEntity.badRequest().body(
                    ApiResponse.fail("确认违规时必须选择处理方式")
                );
            }
            
            // 只允许 NONE 和 USER_LOCK 两种处理方式
            if (!"NONE".equals(request.getActionType()) && !"USER_LOCK".equals(request.getActionType())) {
                return ResponseEntity.badRequest().body(
                    ApiResponse.fail("无效的处理方式，只允许不采取行动或锁定用户")
                );
            }
            
            // 如果选择锁定用户，必须指定天数
            if ("USER_LOCK".equals(request.getActionType()) && 
                (request.getActionDays() == null || request.getActionDays() < 1)) {
                return ResponseEntity.badRequest().body(
                    ApiResponse.fail("锁定用户时必须指定天数（1-365天）")
                );
            }
        }
        
        ReportDto resolvedReport = reportService.resolveReport(id, request, adminId);
        
        return ResponseEntity.ok(ApiResponse.success("处理举报成功", resolvedReport));
    }
    
    /**
     * 获取待处理的举报列表（仅管理员可用）
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYSADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAdminReports(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long adminId = getUserIdFromAuthentication(auth);
        
        if (adminId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("未授权操作，请重新登录"));
        }
        
        List<ReportDto> reports = reportService.getReportsForAdmin(page, size, status, type, startDate, endDate);
        
        // 服务层已经通过enrichWithReportContent完成了内容的填充，不需要在控制器中重复处理
        
        int totalCount = reportService.countReportsForAdmin(status, type, startDate, endDate);
        int pendingCount = reportService.countPendingReports();
        
        Map<String, Object> result = new HashMap<>();
        result.put("reports", reports);
        result.put("totalItems", totalCount);
        result.put("currentPage", page);
        result.put("pageSize", size);
        result.put("pendingReportsCount", pendingCount);
        
        return ResponseEntity.ok(ApiResponse.success("获取举报列表成功", result));
    }
} 