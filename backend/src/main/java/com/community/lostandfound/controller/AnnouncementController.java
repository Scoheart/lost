package com.community.lostandfound.controller;

import com.community.lostandfound.dto.announcement.AnnouncementDto;
import com.community.lostandfound.dto.announcement.AnnouncementPageDto;
import com.community.lostandfound.dto.announcement.CreateAnnouncementRequest;
import com.community.lostandfound.dto.announcement.UpdateAnnouncementRequest;
import com.community.lostandfound.dto.common.ApiResponse;
import com.community.lostandfound.exception.BadRequestException;
import com.community.lostandfound.exception.ResourceNotFoundException;
import com.community.lostandfound.security.CurrentUser;
import com.community.lostandfound.security.UserDetailsImpl;
import com.community.lostandfound.service.AnnouncementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 公告控制器
 * 提供公告管理和查询功能，管理功能仅管理员可用，查询功能对所有人开放
 */
@Slf4j
@RestController
@RequestMapping("/announcements")
@RequiredArgsConstructor
public class AnnouncementController {


    private final AnnouncementService announcementService;

    /**
     * 【管理员接口】获取公告（分页）
     * 系统管理员可以获取所有公告，小区管理员只能获取自己发布的公告
     *
     * @param page      页码（从1开始）
     * @param pageSize  每页条数
     * @param keyword   搜索关键词（模糊匹配标题或内容）
     * @param adminName 管理员用户名筛选（仅系统管理员可用）
     * @param currentUser 当前用户
     * @return 分页公告列表
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYSADMIN')")
    public ResponseEntity<ApiResponse<AnnouncementPageDto>> getAllAnnouncements(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String adminName,
            @CurrentUser UserDetailsImpl currentUser) {

        log.info("获取公告列表, 页码: {}, 每页条数: {}, 关键词: {}, 管理员: {}",
                page, pageSize, keyword, adminName);
                
        // 获取当前用户ID
        Long userId = getCurrentUserId(currentUser);
        if (userId == null) {
            log.error("获取公告列表失败: 无法获取当前用户信息");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("未授权操作，请重新登录"));
        }
        
        // 判断当前用户是否为系统管理员
        boolean isSysAdmin = isCurrentUserSysAdmin();
        
        AnnouncementPageDto result;
        if (isSysAdmin) {
            // 系统管理员可以获取所有公告
            result = announcementService.getAllAnnouncements(page, pageSize, keyword, adminName);
        } else {
            // 小区管理员只能获取自己发布的公告
            // 使用getAllAnnouncements方法，但只过滤当前管理员的公告
            // 这样保留了关键词搜索功能
            result = announcementService.getAllAnnouncements(page, pageSize, keyword, currentUser.getUsername());
        }
        
        return ResponseEntity.ok(ApiResponse.success("获取公告列表成功", result));
    }

    /**
     * 【公开接口】获取已发布公告（分页）
     * 该接口对所有用户开放，用于网站首页和公告展示页面
     *
     * @param page     页码（从1开始）
     * @param pageSize 每页条数
     * @param keyword  搜索关键词（模糊匹配标题或内容，可选）
     * @return 分页公告列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse<AnnouncementPageDto>> getPublishedAnnouncements(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {

        log.info("获取已发布公告列表, 页码: {}, 每页条数: {}, 关键词: {}", page, pageSize, keyword);
        AnnouncementPageDto result = announcementService.getPublishedAnnouncements(page, pageSize, keyword);
        return ResponseEntity.ok(ApiResponse.success("获取已发布公告成功", result));
    }

    /**
     * 【公开接口】根据ID获取公告
     * 该接口对所有用户开放，但非管理员用户只能查看已发布的公告
     *
     * @param id 公告ID
     * @return 公告详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AnnouncementDto>> getAnnouncementById(@PathVariable Long id) {
        log.info("获取公告详情, ID: {}", id);

        try {
            AnnouncementDto announcement = announcementService.getAnnouncementById(id);

            // 如果公告未发布，且当前用户非管理员，则返回404
            if (!"published".equals(announcement.getStatus()) && !isCurrentUserAdmin()) {
                log.warn("非管理员用户尝试访问未发布公告, ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.fail("公告不存在"));
            }

            return ResponseEntity.ok(ApiResponse.success("获取公告成功", announcement));
        } catch (ResourceNotFoundException e) {
            log.warn("公告不存在, ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail("公告不存在"));
        }
    }

    /**
     * 【管理员接口】创建公告
     *
     * @param request     创建公告请求
     * @param currentUser 当前用户
     * @return 创建的公告
     */
    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYSADMIN')")
    public ResponseEntity<ApiResponse<AnnouncementDto>> createAnnouncement(
            @Valid @RequestBody CreateAnnouncementRequest request,
            @CurrentUser UserDetailsImpl currentUser) {

        // 获取当前用户ID
        Long userId = getCurrentUserId(currentUser);
        if (userId == null) {
            log.error("创建公告失败: 无法获取当前用户信息");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("未授权操作，请重新登录"));
        }

        log.info("创建公告: {}, 用户ID: {}", request.getTitle(), userId);
        try {
            AnnouncementDto createdAnnouncement = announcementService.createAnnouncement(request, userId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("公告创建成功", createdAnnouncement));
        } catch (Exception e) {
            log.error("创建公告时发生错误", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("创建公告失败: " + e.getMessage()));
        }
    }

    /**
     * 【管理员接口】更新公告
     * 系统管理员可以更新任何公告，小区管理员只能更新自己发布的公告
     *
     * @param id          公告ID
     * @param request     更新公告请求
     * @param currentUser 当前用户
     * @return 更新后的公告
     */
    @PutMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYSADMIN')")
    public ResponseEntity<ApiResponse<AnnouncementDto>> updateAnnouncement(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAnnouncementRequest request,
            @CurrentUser UserDetailsImpl currentUser) {

        // 获取当前用户ID
        Long userId = getCurrentUserId(currentUser);
        if (userId == null) {
            log.error("更新公告失败: 无法获取当前用户信息");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("未授权操作，请重新登录"));
        }

        log.info("更新公告, ID: {}, 用户ID: {}", id, userId);

        try {
            // 检查是否为系统管理员
            boolean isSysAdmin = isCurrentUserSysAdmin();
            
            // 如果不是系统管理员，检查公告是否属于当前用户
            if (!isSysAdmin) {
                AnnouncementDto announcement = announcementService.getAnnouncementById(id);
                if (!userId.equals(announcement.getAdminId())) {
                    log.warn("用户尝试更新不属于自己的公告, 用户ID: {}, 公告ID: {}", userId, id);
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(ApiResponse.fail("您无权更新此公告"));
                }
            }
            
            AnnouncementDto updatedAnnouncement = announcementService.updateAnnouncement(id, request, userId);
            return ResponseEntity.ok(ApiResponse.success("公告更新成功", updatedAnnouncement));
        } catch (ResourceNotFoundException e) {
            log.warn("公告不存在, ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail("公告不存在"));
        } catch (IllegalArgumentException e) {
            log.warn("无权更新公告, ID: {}, 错误: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.fail(e.getMessage()));
        } catch (Exception e) {
            log.error("更新公告时发生错误", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("更新公告失败: " + e.getMessage()));
        }
    }

    /**
     * 【管理员接口】删除公告
     * 系统管理员可以删除任何公告，小区管理员只能删除自己发布的公告
     *
     * @param id          公告ID
     * @param currentUser 当前用户
     * @return 删除结果
     */
    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYSADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteAnnouncement(
            @PathVariable Long id,
            @CurrentUser UserDetailsImpl currentUser) {

        // 获取当前用户ID
        Long userId = getCurrentUserId(currentUser);
        if (userId == null) {
            log.error("删除公告失败: 无法获取当前用户信息");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("未授权操作，请重新登录"));
        }

        log.info("删除公告, ID: {}, 用户ID: {}", id, userId);

        try {
            // 检查是否为系统管理员
            boolean isSysAdmin = isCurrentUserSysAdmin();
            
            // 如果不是系统管理员，检查公告是否属于当前用户
            if (!isSysAdmin) {
                AnnouncementDto announcement = announcementService.getAnnouncementById(id);
                if (!userId.equals(announcement.getAdminId())) {
                    log.warn("用户尝试删除不属于自己的公告, 用户ID: {}, 公告ID: {}", userId, id);
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(ApiResponse.fail("您无权删除此公告"));
                }
            }
            
            boolean deleted = announcementService.deleteAnnouncement(id, userId);

            if (deleted) {
                return ResponseEntity.ok(ApiResponse.success("公告删除成功", null));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.fail("公告删除失败"));
            }
        } catch (ResourceNotFoundException e) {
            log.warn("公告不存在, ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail("公告不存在"));
        } catch (IllegalArgumentException e) {
            log.warn("无权删除公告, ID: {}, 错误: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.fail(e.getMessage()));
        } catch (Exception e) {
            log.error("删除公告时发生错误", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("删除公告失败: " + e.getMessage()));
        }
    }

    /**
     * 【管理员接口】获取当前管理员发布的公告（分页）
     * （已废弃，对于小区管理员，/admin 接口已能满足需求）
     *
     * @param page        页码（从1开始）
     * @param pageSize    每页条数
     * @param currentUser 当前用户
     * @return 分页公告列表
     */
    @GetMapping("/admin/mine")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYSADMIN')")
    public ResponseEntity<ApiResponse<AnnouncementPageDto>> getMyAnnouncements(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @CurrentUser UserDetailsImpl currentUser) {

        // 获取当前用户ID
        Long userId = getCurrentUserId(currentUser);
        if (userId == null) {
            log.error("获取我的公告失败: 无法获取当前用户信息");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("未授权操作，请重新登录"));
        }

        log.info("获取当前管理员发布的公告, 用户ID: {}, 页码: {}, 每页条数: {}",
                userId, page, pageSize);

        try {
            AnnouncementPageDto result = announcementService.getAnnouncementsByAdminId(
                    userId, page, pageSize);

            return ResponseEntity.ok(ApiResponse.success("获取我的公告成功", result));
        } catch (Exception e) {
            log.error("获取我的公告时发生错误", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("获取我的公告失败: " + e.getMessage()));
        }
    }

    /**
     * 检查当前用户是否为管理员
     *
     * @return 是否为管理员
     */
    private boolean isCurrentUserAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                        a.getAuthority().equals("ROLE_SYSADMIN"));
    }
    
    /**
     * 检查当前用户是否为系统管理员
     *
     * @return 是否为系统管理员
     */
    private boolean isCurrentUserSysAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SYSADMIN"));
    }

    /**
     * 获取当前用户ID
     * 如果通过@CurrentUser获取的用户为null，则尝试从SecurityContextHolder获取
     *
     * @param currentUser 通过@CurrentUser注解获取的当前用户
     * @return 当前用户ID，如果无法获取则返回null
     */
    private Long getCurrentUserId(UserDetailsImpl currentUser) {
        // 首先尝试使用传入的currentUser
        if (currentUser != null) {
            return currentUser.getId();
        }

        // 如果currentUser为null，尝试从SecurityContext获取
        log.debug("从@CurrentUser获取的用户为null，尝试从SecurityContextHolder获取");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
                authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            log.debug("从SecurityContextHolder获取到用户: {}", userDetails.getUsername());
            return userDetails.getId();
        } else if (authentication != null) {
            log.warn("认证信息存在但类型不匹配: {}",
                    authentication.getPrincipal() != null ? authentication.getPrincipal().getClass().getName() : "null");
        }

        log.warn("无法获取当前用户信息");
        return null;
    }
} 