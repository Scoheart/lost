package com.community.lostandfound.controller;

import com.community.lostandfound.dto.announcement.AnnouncementDto;
import com.community.lostandfound.dto.announcement.AnnouncementPageDto;
import com.community.lostandfound.dto.announcement.CreateAnnouncementRequest;
import com.community.lostandfound.dto.announcement.UpdateAnnouncementRequest;
import com.community.lostandfound.dto.common.ApiResponse;
import com.community.lostandfound.exception.ResourceNotFoundException;
import com.community.lostandfound.security.CurrentUser;
import com.community.lostandfound.security.UserPrincipal;
import com.community.lostandfound.service.AnnouncementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 公告控制器
 */
@Slf4j
@RestController
@RequestMapping("/announcements")
@RequiredArgsConstructor
public class AnnouncementController {
    
    private final AnnouncementService announcementService;

    /**
     * 获取所有公告（分页）
     *
     * @param page     页码（从1开始）
     * @param pageSize 每页条数
     * @return 分页公告列表
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYSADMIN')")
    public ResponseEntity<AnnouncementPageDto> getAllAnnouncements(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        log.info("获取所有公告列表, 页码: {}, 每页条数: {}", page, pageSize);
        AnnouncementPageDto result = announcementService.getAllAnnouncements(page, pageSize);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取已发布公告（分页）
     *
     * @param page     页码（从1开始）
     * @param pageSize 每页条数
     * @return 分页公告列表
     */
    @GetMapping
    public ResponseEntity<AnnouncementPageDto> getPublishedAnnouncements(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        log.info("获取已发布公告列表, 页码: {}, 每页条数: {}", page, pageSize);
        AnnouncementPageDto result = announcementService.getPublishedAnnouncements(page, pageSize);
        return ResponseEntity.ok(result);
    }

    /**
     * 根据ID获取公告
     *
     * @param id 公告ID
     * @return 公告详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<AnnouncementDto> getAnnouncementById(@PathVariable Long id) {
        log.info("获取公告详情, ID: {}", id);
        
        try {
            AnnouncementDto announcement = announcementService.getAnnouncementById(id);
            return ResponseEntity.ok(announcement);
        } catch (ResourceNotFoundException e) {
            log.warn("公告不存在, ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 创建公告
     *
     * @param request 创建公告请求
     * @param currentUser 当前用户
     * @return 创建的公告
     */
    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYSADMIN')")
    public ResponseEntity<AnnouncementDto> createAnnouncement(
            @Valid @RequestBody CreateAnnouncementRequest request,
            @CurrentUser UserPrincipal currentUser) {
        
        log.info("创建公告: {}, 用户ID: {}", request.getTitle(), currentUser.getId());
        AnnouncementDto createdAnnouncement = announcementService.createAnnouncement(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAnnouncement);
    }

    /**
     * 更新公告
     *
     * @param id 公告ID
     * @param request 更新公告请求
     * @param currentUser 当前用户
     * @return 更新后的公告
     */
    @PutMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYSADMIN')")
    public ResponseEntity<AnnouncementDto> updateAnnouncement(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAnnouncementRequest request,
            @CurrentUser UserPrincipal currentUser) {
        
        log.info("更新公告, ID: {}, 用户ID: {}", id, currentUser.getId());
        
        try {
            AnnouncementDto updatedAnnouncement = announcementService.updateAnnouncement(id, request, currentUser.getId());
            return ResponseEntity.ok(updatedAnnouncement);
        } catch (ResourceNotFoundException e) {
            log.warn("公告不存在, ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            log.warn("无权更新公告, ID: {}, 错误: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * 删除公告
     *
     * @param id 公告ID
     * @param currentUser 当前用户
     * @return 删除结果
     */
    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYSADMIN')")
    public ResponseEntity<ApiResponse> deleteAnnouncement(
            @PathVariable Long id,
            @CurrentUser UserPrincipal currentUser) {
        
        log.info("删除公告, ID: {}, 用户ID: {}", id, currentUser.getId());
        
        try {
            boolean deleted = announcementService.deleteAnnouncement(id, currentUser.getId());
            
            if (deleted) {
                return ResponseEntity.ok(new ApiResponse(true, "公告删除成功"));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ApiResponse(false, "公告删除失败"));
            }
        } catch (ResourceNotFoundException e) {
            log.warn("公告不存在, ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "公告不存在"));
        } catch (IllegalArgumentException e) {
            log.warn("无权删除公告, ID: {}, 错误: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    /**
     * 获取当前管理员发布的公告（分页）
     *
     * @param page     页码（从1开始）
     * @param pageSize 每页条数
     * @param currentUser 当前用户
     * @return 分页公告列表
     */
    @GetMapping("/admin/mine")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYSADMIN')")
    public ResponseEntity<AnnouncementPageDto> getMyAnnouncements(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @CurrentUser UserPrincipal currentUser) {
        
        log.info("获取当前管理员发布的公告, 用户ID: {}, 页码: {}, 每页条数: {}", 
                currentUser.getId(), page, pageSize);
        
        AnnouncementPageDto result = announcementService.getAnnouncementsByAdminId(
                currentUser.getId(), page, pageSize);
        
        return ResponseEntity.ok(result);
    }
} 