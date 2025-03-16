package com.community.lostandfound.controller;

import com.community.lostandfound.dto.admin.AdminUserDto;
import com.community.lostandfound.dto.admin.AdminUserPageDto;
import com.community.lostandfound.dto.admin.RegisterAdminRequest;
import com.community.lostandfound.dto.admin.UpdateAdminStatusRequest;
import com.community.lostandfound.dto.common.ApiResponse;
import com.community.lostandfound.dto.user.UpdateProfileRequest;
import com.community.lostandfound.entity.User;
import com.community.lostandfound.exception.BadRequestException;
import com.community.lostandfound.exception.ResourceNotFoundException;
import com.community.lostandfound.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
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
        log.debug("Fetching administrator with ID: {}", id);
        
        User user = userService.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Administrator", "id", id));
        
        // Verify that the user is an admin
        if (!("admin".equals(user.getRole()) || "sysadmin".equals(user.getRole()))) {
            throw new BadRequestException("指定用户不是管理员");
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
        
        log.debug("Updating administrator status for ID {}: enabled={}", 
                id, request.getIsEnabled());
        
        User user = userService.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Administrator", "id", id));
        
        // Verify that the user is an admin
        if (!("admin".equals(user.getRole()) || "sysadmin".equals(user.getRole()))) {
            throw new BadRequestException("指定用户不是管理员");
        }
        
        // Update user status
        user.setIsEnabled(request.getIsEnabled());
        user.setUpdatedAt(LocalDateTime.now());
        
        User updatedUser = userService.updateUser(user);
        
        AdminUserDto adminDto = convertToAdminDto(updatedUser);
        
        return ResponseEntity.ok(ApiResponse.success("管理员状态更新成功", adminDto));
    }
    
    /**
     * Delete an administrator (only community admins can be deleted)
     * @param id the administrator ID
     * @return success message
     */
    @DeleteMapping("/admins/{id}")
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteAdmin(@PathVariable Long id) {
        log.debug("Deleting administrator with ID: {}", id);
        
        User user = userService.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Administrator", "id", id));
        
        // Verify that the user is a community admin (not a system admin)
        if (!"admin".equals(user.getRole())) {
            throw new BadRequestException("只能删除小区管理员，系统管理员不能被删除");
        }
        
        userService.deleteUser(id);
        
        return ResponseEntity.ok(ApiResponse.success("管理员删除成功", null));
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
        log.debug("系统管理员请求获取用户详情: id={}", id);
        
        User user = userService.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        
        AdminUserDto userDto = convertToAdminDto(user);
        
        return ResponseEntity.ok(ApiResponse.success("获取用户详情成功", userDto));
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

        // 检查邮箱是否已存在
        if (userService.existsByEmail(request.getEmail())) {
            throw new BadRequestException("邮箱已被使用");
        }
        
        // 验证角色有效性（只允许小区管理员或居民）
        if (!("resident".equals(request.getRole()) || "admin".equals(request.getRole()))) {
            throw new BadRequestException("角色必须是居民(resident)或小区管理员(admin)");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword()); // 服务层会加密密码
        user.setRole(request.getRole());
        user.setRealName(request.getRealName()); // realName可以为空
        user.setPhone(request.getPhone());
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
            @Valid @RequestBody UpdateProfileRequest request) {
        
        log.debug("系统管理员更新用户信息: id={}", id);
        
        User user = userService.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        
        // 检查邮箱是否与其他用户冲突
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail()) && 
                userService.existsByEmail(request.getEmail())) {
            throw new BadRequestException("该邮箱已被其他用户使用");
        }
        
        // 更新用户信息
        if (request.getEmail() != null) {
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
        
        user.setUpdatedAt(LocalDateTime.now());
        
        User updatedUser = userService.updateUser(user);
        log.debug("用户信息更新成功: id={}", updatedUser.getId());
        
        AdminUserDto userDto = convertToAdminDto(updatedUser);
        
        return ResponseEntity.ok(ApiResponse.success("用户信息更新成功", userDto));
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
        
        log.debug("系统管理员更新用户状态: id={}, 启用={}", 
                id, request.getIsEnabled());
        
        User user = userService.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        
        // 更新用户状态
        user.setIsEnabled(request.getIsEnabled());
        user.setUpdatedAt(LocalDateTime.now());
        
        User updatedUser = userService.updateUser(user);
        log.debug("用户状态更新成功: id={}", updatedUser.getId());
        
        AdminUserDto userDto = convertToAdminDto(updatedUser);
        
        return ResponseEntity.ok(ApiResponse.success("用户状态更新成功", userDto));
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
        log.debug("系统管理员删除用户: id={}", id);
        
        User user = userService.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        
        // 执行删除
        userService.deleteUser(id);
        log.debug("用户删除成功: id={}, username={}", id, user.getUsername());
        
        return ResponseEntity.ok(ApiResponse.success("用户删除成功", null));
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
        
        log.debug("系统管理员重置用户密码: id={}", id);
        
        if (newPassword == null || newPassword.length() < 6) {
            throw new BadRequestException("密码长度必须至少为6个字符");
        }
        
        User user = userService.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        
        // 设置新密码
        user.setPassword(newPassword); // 服务层会加密密码
        user.setUpdatedAt(LocalDateTime.now());
        
        userService.updateUser(user);
        log.debug("用户密码重置成功: id={}", id);
        
        return ResponseEntity.ok(ApiResponse.success("用户密码重置成功", null));
    }

    /**
     * Helper method to convert User to AdminUserDto
     * @param user the user entity
     * @return the admin user DTO
     */
    private AdminUserDto convertToAdminDto(User user) {
        return new AdminUserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getAvatar(),
                user.getPhone(),
                user.getRealName(),
                user.getIsEnabled(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
} 