package com.community.lostandfound.controller;

import com.community.lostandfound.dto.admin.AdminUserDto;
import com.community.lostandfound.dto.admin.AdminUserPageDto;
import com.community.lostandfound.dto.admin.RegisterAdminRequest;
import com.community.lostandfound.dto.admin.UpdateAdminStatusRequest;
import com.community.lostandfound.dto.common.ApiResponse;
import com.community.lostandfound.dto.user.UserProfileDto;
import com.community.lostandfound.entity.User;
import com.community.lostandfound.exception.BadRequestException;
import com.community.lostandfound.exception.ResourceNotFoundException;
import com.community.lostandfound.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for resident user management (accessible by both system admins and community admins)
 */
@Slf4j
@RestController
@RequestMapping("/residents")
@RequiredArgsConstructor
public class ResidentController {

    private final UserService userService;

    /**
     * Create a new resident user
     * @param residentData the resident data for creation
     * @return the created resident user details
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSADMIN')")
    public ResponseEntity<ApiResponse<AdminUserDto>> createResident(
            @Valid @RequestBody Map<String, Object> residentData) {
        
        log.debug("Creating new resident user: {}", residentData);
        
        // Extract required fields
        String username = (String) residentData.get("username");
        String email = (String) residentData.get("email");
        String password = (String) residentData.get("password");
        String phone = (String) residentData.get("phone");
        Boolean isEnabled = residentData.containsKey("isEnabled") ? 
                (Boolean) residentData.get("isEnabled") : true;
        
        // Validate required fields
        if (username == null || username.isEmpty()) {
            throw new BadRequestException("用户名不能为空");
        }
        
        if (email == null || email.isEmpty()) {
            throw new BadRequestException("邮箱不能为空");
        }
        
        if (password == null || password.isEmpty()) {
            throw new BadRequestException("密码不能为空");
        }
        
        // Check if username is already taken
        if (userService.existsByUsername(username)) {
            throw new BadRequestException("用户名已被使用");
        }
        
        // Check if email is already in use
        if (userService.existsByEmail(email)) {
            throw new BadRequestException("邮箱已被使用");
        }
        
        // Create new resident user
        User resident = new User();
        resident.setUsername(username);
        resident.setEmail(email);
        resident.setPassword(password); // Will be encoded in service layer
        resident.setRole("resident"); // Always set role to resident
        resident.setPhone(phone);
        
        // Optional fields
        if (residentData.containsKey("realName")) {
            resident.setRealName((String) residentData.get("realName"));
        }
        
        if (residentData.containsKey("avatar")) {
            resident.setAvatar((String) residentData.get("avatar"));
        }
        
        resident.setIsEnabled(isEnabled);
        resident.setCreatedAt(LocalDateTime.now());
        resident.setUpdatedAt(LocalDateTime.now());
        
        // Register user
        User createdResident = userService.registerUser(resident);
        log.debug("New resident user created: id={}, username={}", createdResident.getId(), createdResident.getUsername());
        
        // Convert to DTO
        AdminUserDto residentDto = convertToUserDto(createdResident);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("居民用户创建成功", residentDto));
    }

    /**
     * Get all resident users with pagination
     * @param page the page number (1-indexed)
     * @param pageSize the number of items per page
     * @param search optional search query for username, email, or phone
     * @param status optional filter for user status (enabled/disabled)
     * @param startDate optional filter for user creation date (start)
     * @param endDate optional filter for user creation date (end)
     * @return the paginated list of resident users
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSADMIN')")
    public ResponseEntity<ApiResponse<AdminUserPageDto>> getAllResidents(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int pageSize,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false) Boolean status,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate) {
        
        log.debug("Fetching resident users with filters: page={}, pageSize={}, search={}, status={}, dateRange={} to {}", 
                page, pageSize, search, status, startDate, endDate);
        
        // Convert pagination (1-indexed to 0-indexed for service)
        page = Math.max(1, page);
        int effectivePage = page - 1;
        pageSize = Math.max(1, Math.min(100, pageSize));
        
        // Parse date parameters
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;
        
        if (startDate != null && !startDate.isEmpty()) {
            try {
                startDateTime = LocalDate.parse(startDate).atStartOfDay();
            } catch (DateTimeParseException e) {
                log.warn("Invalid startDate format: {}", startDate);
                // Continue with null startDateTime
            }
        }
        
        if (endDate != null && !endDate.isEmpty()) {
            try {
                endDateTime = LocalDate.parse(endDate).atTime(23, 59, 59);
            } catch (DateTimeParseException e) {
                log.warn("Invalid endDate format: {}", endDate);
                // Continue with null endDateTime
            }
        }
        
        // Get filtered resident users
        List<User> filteredUsers = userService.getFilteredUsers(
                search, "resident", status, startDateTime, endDateTime, effectivePage, pageSize);
        
        // Get count of filtered resident users
        int totalItems = userService.countFilteredUsers(
                search, "resident", status, startDateTime, endDateTime);
        
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        
        // Convert to DTOs
        List<AdminUserDto> residentDtos = filteredUsers.stream()
                .map(this::convertToUserDto)
                .collect(Collectors.toList());
        
        // Create page DTO
        AdminUserPageDto pageDto = new AdminUserPageDto(
                residentDtos,
                page,
                pageSize,
                totalItems,
                totalPages
        );
        
        return ResponseEntity.ok(ApiResponse.success("获取居民用户列表成功", pageDto));
    }
    
    /**
     * Get a resident user by ID
     * @param id the resident user ID
     * @return the resident user details
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSADMIN')")
    public ResponseEntity<ApiResponse<AdminUserDto>> getResidentById(@PathVariable Long id) {
        log.debug("Fetching resident user with ID: {}", id);
        
        User user = userService.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resident", "id", id));
        
        // Verify that the user is a resident
        if (!"resident".equals(user.getRole())) {
            throw new BadRequestException("指定用户不是居民");
        }
        
        AdminUserDto userDto = convertToUserDto(user);
        
        return ResponseEntity.ok(ApiResponse.success("获取居民用户信息成功", userDto));
    }
    
    /**
     * Update resident user status (enable/disable)
     * @param id the resident user ID
     * @param request the status update request
     * @return the updated resident user details
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSADMIN')")
    public ResponseEntity<ApiResponse<AdminUserDto>> updateResidentStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAdminStatusRequest request) {
        
        log.debug("Updating resident user status for ID {}: enabled={}", 
                id, request.getIsEnabled());
        
        User user = userService.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resident", "id", id));
        
        // Verify that the user is a resident
        if (!"resident".equals(user.getRole())) {
            throw new BadRequestException("指定用户不是居民");
        }
        
        // Update user status
        user.setIsEnabled(request.getIsEnabled());
        user.setUpdatedAt(LocalDateTime.now());
        
        User updatedUser = userService.updateUser(user);
        
        AdminUserDto userDto = convertToUserDto(updatedUser);
        
        return ResponseEntity.ok(ApiResponse.success("居民用户状态更新成功", userDto));
    }
    
    /**
     * Delete a resident user
     * @param id the resident user ID
     * @return success message
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteResident(@PathVariable Long id) {
        log.debug("Deleting resident user with ID: {}", id);
        
        User user = userService.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resident", "id", id));
        
        // Verify that the user is a resident
        if (!"resident".equals(user.getRole())) {
            throw new BadRequestException("指定用户不是居民");
        }
        
        userService.deleteUser(id);
        
        return ResponseEntity.ok(ApiResponse.success("居民用户删除成功", null));
    }
    
    /**
     * Update a resident user's information
     * @param id the resident user ID
     * @param updateData the user data to update
     * @return the updated resident user details
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSADMIN')")
    public ResponseEntity<ApiResponse<AdminUserDto>> updateResident(
            @PathVariable Long id,
            @Valid @RequestBody Map<String, Object> updateData) {
        
        log.debug("Updating resident user information for ID {}: {}", id, updateData);
        
        User user = userService.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resident", "id", id));
        
        // Verify that the user is a resident
        if (!"resident".equals(user.getRole())) {
            throw new BadRequestException("指定用户不是居民");
        }
        
        // Update user fields if provided in the request
        if (updateData.containsKey("username")) {
            user.setUsername((String) updateData.get("username"));
        }
        
        if (updateData.containsKey("email")) {
            user.setEmail((String) updateData.get("email"));
        }
        
        if (updateData.containsKey("phone")) {
            user.setPhone((String) updateData.get("phone"));
        }
        
        if (updateData.containsKey("realName")) {
            user.setRealName((String) updateData.get("realName"));
        }
        
        if (updateData.containsKey("avatar")) {
            user.setAvatar((String) updateData.get("avatar"));
        }
        
        if (updateData.containsKey("isEnabled")) {
            user.setIsEnabled((Boolean) updateData.get("isEnabled"));
        }
        
        user.setUpdatedAt(LocalDateTime.now());
        
        User updatedUser = userService.updateUser(user);
        AdminUserDto userDto = convertToUserDto(updatedUser);
        
        return ResponseEntity.ok(ApiResponse.success("居民用户信息更新成功", userDto));
    }
    
    /**
     * Helper method to convert User to AdminUserDto
     * @param user the user entity
     * @return the admin user DTO
     */
    private AdminUserDto convertToUserDto(User user) {
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