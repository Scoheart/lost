package com.community.lostandfound.controller;

import com.community.lostandfound.dto.admin.AdminUserDto;
import com.community.lostandfound.dto.admin.AdminUserPageDto;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
     * Get all resident users with pagination
     * @param page the page number (1-indexed)
     * @param pageSize the number of items per page
     * @return the paginated list of resident users
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSADMIN')")
    public ResponseEntity<ApiResponse<AdminUserPageDto>> getAllResidents(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        
        log.debug("Fetching all resident users: page={}, pageSize={}", page, pageSize);
        
        // Get all users
        List<User> allUsers = userService.getAllUsers();
        
        // Filter resident users
        List<User> residentUsers = allUsers.stream()
                .filter(user -> "resident".equals(user.getRole()))
                .collect(Collectors.toList());
        
        // Calculate pagination
        page = Math.max(1, page);
        pageSize = Math.max(1, Math.min(100, pageSize));
        
        int totalItems = residentUsers.size();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalItems);
        
        // If fromIndex is out of bounds, return empty list
        List<User> pagedUsers = fromIndex < totalItems 
                ? residentUsers.subList(fromIndex, toIndex) 
                : List.of();
        
        // Convert to DTOs
        List<AdminUserDto> residentDtos = pagedUsers.stream()
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
                null, // Remove isLocked
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
} 