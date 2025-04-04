package com.community.lostandfound.controller;

import com.community.lostandfound.dto.admin.AdminUserDto;
import com.community.lostandfound.dto.admin.AdminUserPageDto;
import com.community.lostandfound.dto.admin.RegisterAdminRequest;
import com.community.lostandfound.dto.admin.UpdateAdminStatusRequest;
import com.community.lostandfound.dto.user.UpdateUserAdminRequest;
import com.community.lostandfound.dto.common.ApiResponse;
import com.community.lostandfound.dto.report.ReportDto;
import com.community.lostandfound.dto.report.ReportPageDto;
import com.community.lostandfound.dto.report.ReportResolutionRequest;
import com.community.lostandfound.entity.Report;
import com.community.lostandfound.entity.User;
import com.community.lostandfound.exception.BadRequestException;
import com.community.lostandfound.exception.ResourceNotFoundException;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for administrator management
 */
@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final ReportService reportService;

    /**
     * Register a new system administrator (only accessible by system admins)
     * @param request the admin registration request
     * @return success message
     */
    @PostMapping("/register-sysadmin")
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<ApiResponse<String>> registerSystemAdmin(
            @Valid @RequestBody RegisterAdminRequest request) {
        
        log.debug("Registering new system administrator: {}", request.getUsername());
        
        // Validate that the role is sysadmin
        if (!"sysadmin".equals(request.getRole())) {
            throw new BadRequestException("管理员类型必须是系统管理员(sysadmin)");
        }

        // Check if username is already taken
        if (userService.existsByUsername(request.getUsername())) {
            throw new BadRequestException("用户名已被使用");
        }

        // Check if email is already in use
        if (userService.existsByEmail(request.getEmail())) {
            throw new BadRequestException("邮箱已被使用");
        }

        // Create new system admin
        User admin = new User();
        admin.setUsername(request.getUsername());
        admin.setEmail(request.getEmail());
        admin.setPassword(request.getPassword()); // Will be encoded in service
        admin.setRole("sysadmin");
        // realName 可以为空
        admin.setRealName(request.getRealName());
        admin.setPhone(request.getPhone());
        admin.setIsEnabled(true);
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());
        
        userService.registerUser(admin);
        
        return ResponseEntity.ok(ApiResponse.success("系统管理员注册成功", null));
    }
    
    /**
     * Register a new community administrator (only accessible by system admins)
     * @param request the admin registration request
     * @return success message
     */
    @PostMapping("/register-admin")
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<ApiResponse<String>> registerCommunityAdmin(
            @Valid @RequestBody RegisterAdminRequest request) {
        
        log.debug("Registering new community administrator: {}", request.getUsername());
        
        // Validate that the role is admin
        if (!"admin".equals(request.getRole())) {
            throw new BadRequestException("管理员类型必须是小区管理员(admin)");
        }

        // Check if username is already taken
        if (userService.existsByUsername(request.getUsername())) {
            throw new BadRequestException("用户名已被使用");
        }

        // Check if email is already in use
        if (userService.existsByEmail(request.getEmail())) {
            throw new BadRequestException("邮箱已被使用");
        }

        // Create new community admin
        User admin = new User();
        admin.setUsername(request.getUsername());
        admin.setEmail(request.getEmail());
        admin.setPassword(request.getPassword()); // Will be encoded in service
        admin.setRole("admin");
        // realName 可以为空
        admin.setRealName(request.getRealName());
        admin.setPhone(request.getPhone());
        admin.setIsEnabled(true);
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());
        
        userService.registerUser(admin);
        
        return ResponseEntity.ok(ApiResponse.success("小区管理员注册成功", null));
    }
    
    /**
     * Get all administrators (system and community) with pagination
     * @param page the page number (1-indexed)
     * @param pageSize the number of items per page
     * @return the paginated list of administrators
     */
    @GetMapping("/admins")
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<ApiResponse<AdminUserPageDto>> getAllAdmins(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        
        log.debug("Fetching all administrators: page={}, pageSize={}", page, pageSize);
        
        // Get all users
        List<User> allUsers = userService.getAllUsers();
        
        // Filter admin users
        List<User> adminUsers = allUsers.stream()
                .filter(user -> "admin".equals(user.getRole()) || "sysadmin".equals(user.getRole()))
                .collect(Collectors.toList());
        
        // Calculate pagination
        page = Math.max(1, page);
        pageSize = Math.max(1, Math.min(100, pageSize));
        
        int totalItems = adminUsers.size();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalItems);
        
        // If fromIndex is out of bounds, return empty list
        List<User> pagedUsers = fromIndex < totalItems 
                ? adminUsers.subList(fromIndex, toIndex) 
                : List.of();
        
        // Convert to DTOs
        List<AdminUserDto> adminDtos = pagedUsers.stream()
                .map(this::convertToAdminDto)
                .collect(Collectors.toList());
        
        // Create page DTO
        AdminUserPageDto pageDto = new AdminUserPageDto(
                adminDtos,
                page,
                pageSize,
                totalItems,
                totalPages
        );
        
        return ResponseEntity.ok(ApiResponse.success("获取管理员列表成功", pageDto));
    }
    
    /**
     * Get an administrator by ID
     * @param id the administrator ID
     * @return the administrator details
     */
    @GetMapping("/admins/{id}")
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<ApiResponse<AdminUserDto>> getAdminById(@PathVariable Long id) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("管理员不存在"));
        
        // 验证用户是否为管理员
        if (!("admin".equals(user.getRole()) || "sysadmin".equals(user.getRole()))) {
            return ResponseEntity.badRequest().body(
                ApiResponse.fail("用户不是管理员")
            );
        }
        
        AdminUserDto adminDto = convertToAdminDto(user);
        return ResponseEntity.ok(ApiResponse.success("获取管理员信息成功", adminDto));
    }
    
    /**
     * Update administrator status (enable/disable)
     * @param id the administrator ID
     * @param request the status update request
     * @return the updated administrator details
     */
    @PutMapping("/admins/{id}/status")
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<ApiResponse<AdminUserDto>> updateAdminStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAdminStatusRequest request) {
        
        User user = userService.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("管理员不存在"));
        
        // 验证用户是否为管理员
        if (!("admin".equals(user.getRole()) || "sysadmin".equals(user.getRole()))) {
            return ResponseEntity.badRequest().body(
                ApiResponse.fail("用户不是管理员")
            );
        }
        
        // 系统管理员不能被禁用
        if ("sysadmin".equals(user.getRole()) && Boolean.FALSE.equals(request.getIsEnabled())) {
            return ResponseEntity.badRequest().body(
                ApiResponse.fail("不能禁用系统管理员账号")
            );
        }
        
        user.setIsEnabled(request.getIsEnabled());
        User updatedUser = userService.updateUser(user);
        
        AdminUserDto adminDto = convertToAdminDto(updatedUser);
        return ResponseEntity.ok(ApiResponse.success("更新管理员状态成功", adminDto));
    }
    
    /**
     * Delete an administrator (only community admins can be deleted)
     * @param id the administrator ID
     * @return success message
     */
    @DeleteMapping("/admins/{id}")
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteAdmin(@PathVariable Long id) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("管理员不存在"));
        
        // 验证用户是否为管理员
        if (!("admin".equals(user.getRole()))) {
            return ResponseEntity.badRequest().body(
                ApiResponse.fail("只能删除小区管理员账号")
            );
        }
        
        userService.deleteUser(id);
        
        return ResponseEntity.ok(ApiResponse.success("删除管理员成功", null));
    }
    
    /**
     * 获取所有用户的分页列表（支持筛选）
     * 
     * @param page 页码（从1开始）
     * @param size 每页数量
     * @param search 搜索关键字
     * @param role 用户角色
     * @param status 用户状态
     * @return 分页用户列表
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<ApiResponse<AdminUserPageDto>> getAllUsers(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "status", required = false) Boolean status) {
        
        log.debug("系统管理员请求获取用户列表: page={}, size={}, search={}, role={}, status={}", 
                page, size, search, role, status);
        
        // 计算分页参数
        page = Math.max(1, page);
        size = Math.max(1, Math.min(100, size));
        
        // 获取过滤后的用户列表
        List<User> filteredUsers = userService.getFilteredUsers(search, role, status, page - 1, size);
        
        // 获取符合条件的总用户数
        int totalItems = userService.countFilteredUsers(search, role, status);
        int totalPages = (int) Math.ceil((double) totalItems / size);
        
        // 转换为DTO
        List<AdminUserDto> userDtos = filteredUsers.stream()
                .map(this::convertToAdminDto)
                .collect(Collectors.toList());
        
        // 创建分页DTO
        AdminUserPageDto pageDto = new AdminUserPageDto(
                userDtos,
                page,
                size,
                totalItems,
                totalPages
        );
        
        return ResponseEntity.ok(ApiResponse.success("获取用户列表成功", pageDto));
    }
    
    /**
     * 获取指定ID的用户详情
     * 
     * @param id 用户ID
     * @return 用户详情
     */
    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<ApiResponse<AdminUserDto>> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        
        AdminUserDto userDto = convertToAdminDto(user);
        return ResponseEntity.ok(ApiResponse.success("获取用户信息成功", userDto));
    }
    
    /**
     * 创建新用户
     * 
     * @param request 用户创建请求
     * @return 创建结果
     */
    @PostMapping("/users")
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<ApiResponse<AdminUserDto>> createUser(
            @Valid @RequestBody RegisterAdminRequest request) {
        
        log.debug("系统管理员创建新用户: {}, 角色: {}", request.getUsername(), request.getRole());
        
        // 检查用户名是否已存在
        if (userService.existsByUsername(request.getUsername())) {
            throw new BadRequestException("用户名已被使用");
        }

        // 检查邮箱是否已存在（如果提供了邮箱）
        if (request.getEmail() != null && !request.getEmail().isEmpty() && 
            userService.existsByEmail(request.getEmail())) {
            throw new BadRequestException("邮箱已被使用");
        }
        
        // 验证角色有效性（只允许小区管理员或居民）
        if (!("resident".equals(request.getRole()) || "admin".equals(request.getRole()))) {
            throw new BadRequestException("角色必须是居民(resident)或小区管理员(admin)");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail()); // 可以为空
        user.setPassword(request.getPassword()); // 服务层会加密密码
        user.setRole(request.getRole());
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setIsEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        User createdUser = userService.registerUser(user);
        log.debug("新用户创建成功: id={}, username={}", createdUser.getId(), createdUser.getUsername());
        
        AdminUserDto userDto = convertToAdminDto(createdUser);
        
        return ResponseEntity.ok(ApiResponse.success("用户创建成功", userDto));
    }
    
    /**
     * 更新用户信息
     * 
     * @param id 用户ID
     * @param request 用户信息更新请求
     * @return 更新后的用户信息
     */
    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<ApiResponse<AdminUserDto>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserAdminRequest request) {
        
        User user = userService.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        
        // 更新用户信息
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userService.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest().body(
                    ApiResponse.fail("邮箱已被使用")
                );
            }
            user.setEmail(request.getEmail());
        }
        
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        
        if (request.getRealName() != null) {
            user.setRealName(request.getRealName());
        }

        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }
        
        // 处理角色更新
        if (request.getRole() != null && !request.getRole().equals(user.getRole())) {
            // 只有系统管理员可以更改角色
            if ("sysadmin".equals(user.getRole()) && !"sysadmin".equals(request.getRole())) {
                return ResponseEntity.badRequest().body(
                    ApiResponse.fail("不能降级系统管理员角色")
                );
            }
            
            // 验证角色有效性
            if (!"resident".equals(request.getRole()) && 
                !"admin".equals(request.getRole()) && 
                !"sysadmin".equals(request.getRole())) {
                return ResponseEntity.badRequest().body(
                    ApiResponse.fail("无效的角色，可选值: resident, admin, sysadmin")
                );
            }
            
            user.setRole(request.getRole());
            log.info("用户角色已更新: {} -> {}", user.getUsername(), request.getRole());
        }
        
        // 处理启用状态
        if (request.getIsEnabled() != null && request.getIsEnabled() != user.getIsEnabled()) {
            // 管理员账号不能被禁用
            if (("admin".equals(user.getRole()) || "sysadmin".equals(user.getRole())) 
                    && Boolean.FALSE.equals(request.getIsEnabled())) {
                return ResponseEntity.badRequest().body(
                    ApiResponse.fail("不能禁用管理员账号")
                );
            }
            
            user.setIsEnabled(request.getIsEnabled());
        }
        
        user.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userService.updateUser(user);
        
        AdminUserDto userDto = convertToAdminDto(updatedUser);
        return ResponseEntity.ok(ApiResponse.success("更新用户信息成功", userDto));
    }
    
    /**
     * 更新用户状态（启用/禁用）
     * 
     * @param id 用户ID
     * @param request 状态更新请求
     * @return 更新后的用户信息
     */
    @PutMapping("/users/{id}/status")
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<ApiResponse<AdminUserDto>> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAdminStatusRequest request) {
        
        User user = userService.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        
        // 管理员账号不能被禁用
        if (("admin".equals(user.getRole()) || "sysadmin".equals(user.getRole())) 
                && Boolean.FALSE.equals(request.getIsEnabled())) {
            return ResponseEntity.badRequest().body(
                ApiResponse.fail("不能禁用管理员账号")
            );
        }
        
        user.setIsEnabled(request.getIsEnabled());
        User updatedUser = userService.updateUser(user);
        
        AdminUserDto userDto = convertToAdminDto(updatedUser);
        return ResponseEntity.ok(ApiResponse.success("更新用户状态成功", userDto));
    }
    
    /**
     * 删除用户
     * 
     * @param id 用户ID
     * @return 删除结果
     */
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long id) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        
        // 管理员账号不能被删除
        if ("admin".equals(user.getRole()) || "sysadmin".equals(user.getRole())) {
            return ResponseEntity.badRequest().body(
                ApiResponse.fail("不能删除管理员账号")
            );
        }
        
        userService.deleteUser(id);
        
        return ResponseEntity.ok(ApiResponse.success("删除用户成功", null));
    }

    /**
     * 重置用户密码
     * 
     * @param id 用户ID
     * @param newPassword 新密码
     * @return 操作结果
     */
    @PutMapping("/users/{id}/reset-password")
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<ApiResponse<String>> resetUserPassword(
            @PathVariable Long id,
            @RequestParam String newPassword) {
        
        User user = userService.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        
        // 密码长度验证
        if (newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(
                ApiResponse.fail("密码长度不能小于6个字符")
            );
        }
        
        // 更新用户密码
        String encodedPassword = passwordEncoder.encode(newPassword);
        boolean success = userService.updatePasswordDirectly(id, encodedPassword);
        
        if (success) {
            return ResponseEntity.ok(ApiResponse.success("密码重置成功", null));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("密码重置失败，请重试"));
        }
    }

    /**
     * Helper method to convert User to AdminUserDto
     * @param user the user entity
     * @return the admin user DTO
     */
    private AdminUserDto convertToAdminDto(User user) {
        if (user == null) {
            return null;
        }
        
        return new AdminUserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getAvatar(),
                user.getPhone(),
                user.getRealName(),
                user.getAddress(),
                user.getIsEnabled(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    /**
     * 获取举报列表
     */
    @GetMapping("/reports")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYSADMIN')")
    public ResponseEntity<ApiResponse<ReportPageDto>> getReports(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "type", required = false) String type) {
        
        Report.ReportStatus reportStatus = null;
        Report.ReportType reportType = null;
        
        if (status != null) {
            try {
                reportStatus = Report.ReportStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(
                    ApiResponse.fail("无效的状态值，有效值: PENDING, RESOLVED, REJECTED")
                );
            }
        }
        
        if (type != null) {
            try {
                reportType = Report.ReportType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(
                    ApiResponse.fail("无效的举报类型，有效值: LOST_ITEM, FOUND_ITEM, COMMENT")
                );
            }
        }
        
        ReportPageDto reports = reportService.getReports(page, size, reportStatus, reportType);
        return ResponseEntity.ok(ApiResponse.success("获取举报列表成功", reports));
    }
    
    /**
     * 获取举报详情
     */
    @GetMapping("/reports/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYSADMIN')")
    public ResponseEntity<ApiResponse<ReportDto>> getReportById(@PathVariable Long id) {
        ReportDto report = reportService.getReportById(id);
        return ResponseEntity.ok(ApiResponse.success("获取举报详情成功", report));
    }
    
    /**
     * 处理举报
     */
    @PutMapping("/reports/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYSADMIN')")
    public ResponseEntity<ApiResponse<ReportDto>> resolveReport(
            @PathVariable Long id,
            @Valid @RequestBody ReportResolutionRequest request) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long adminId = getCurrentUserId(auth);
        
        if (adminId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("未授权操作，请重新登录"));
        }
        
        log.info("管理员 {} 处理举报 {}: {}", adminId, id, request);
        ReportDto resolvedReport = reportService.resolveReport(id, request, adminId);
        
        return ResponseEntity.ok(ApiResponse.success("处理举报成功", resolvedReport));
    }
    
    /**
     * 获取未处理举报数量
     */
    @GetMapping("/reports/count")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYSADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getPendingReportsCount() {
        long count = reportService.getPendingReportsCount();
        Map<String, Long> response = new HashMap<>();
        response.put("pendingCount", count);
        
        return ResponseEntity.ok(ApiResponse.success("获取未处理举报数量成功", response));
    }
    
    /**
     * 锁定用户
     */
    @PutMapping("/users/{id}/lock")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYSADMIN')")
    public ResponseEntity<ApiResponse<AdminUserDto>> lockUser(
            @PathVariable Long id,
            @RequestParam(value = "days", defaultValue = "7") Integer days,
            @RequestParam(value = "reason", required = false) String reason) {
        
        if (days <= 0 || days > 365) {
            return ResponseEntity.badRequest().body(
                ApiResponse.fail("锁定天数必须在1-365天之间")
            );
        }
        
        User user = userService.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        
        // 不能锁定管理员和系统管理员
        if ("admin".equals(user.getRole()) || "sysadmin".equals(user.getRole())) {
            return ResponseEntity.badRequest().body(
                ApiResponse.fail("不能锁定管理员账号")
            );
        }
        
        user.setIsLocked(true);
        user.setLockEndTime(LocalDateTime.now().plusDays(days));
        user.setLockReason(reason != null ? reason : "违反平台规则");
        
        User updatedUser = userService.updateUser(user);
        
        log.info("用户 {} 被锁定 {} 天，原因: {}", updatedUser.getUsername(), days, reason);
        
        AdminUserDto adminUserDto = convertToAdminDto(updatedUser);
        return ResponseEntity.ok(ApiResponse.success("用户锁定成功", adminUserDto));
    }
    
    /**
     * 解除用户锁定
     */
    @PutMapping("/users/{id}/unlock")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SYSADMIN')")
    public ResponseEntity<ApiResponse<AdminUserDto>> unlockUser(@PathVariable Long id) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        
        user.setIsLocked(false);
        user.setLockEndTime(null);
        user.setLockReason(null);
        
        User updatedUser = userService.updateUser(user);
        
        log.info("用户 {} 的锁定已解除", updatedUser.getUsername());
        
        AdminUserDto adminUserDto = convertToAdminDto(updatedUser);
        return ResponseEntity.ok(ApiResponse.success("解除用户锁定成功", adminUserDto));
    }

    /**
     * 从Authentication对象中获取当前用户ID
     * @param auth 认证对象
     * @return 用户ID，如果无法获取则返回null
     */
    private Long getCurrentUserId(Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
            return userDetails.getId();
        }
        return null;
    }
} 