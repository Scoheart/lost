package com.community.lostandfound.controller;

import com.community.lostandfound.dto.common.ApiResponse;
import com.community.lostandfound.dto.user.ChangePasswordRequest;
import com.community.lostandfound.dto.user.UpdateProfileRequest;
import com.community.lostandfound.dto.user.UserProfileDto;
import com.community.lostandfound.entity.User;
import com.community.lostandfound.exception.BadRequestException;
import com.community.lostandfound.exception.ResourceNotFoundException;
import com.community.lostandfound.security.UserDetailsImpl;
import com.community.lostandfound.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Get the current authenticated user's profile
     * @return the user profile information
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileDto>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        log.debug("Fetching profile for authenticated user: {}", authentication.getName());
        
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Long userId = userDetails.getId();
            
            Optional<User> userOptional = userService.getUserById(userId);
            if (!userOptional.isPresent()) {
                log.warn("User with ID {} not found in database", userId);
                throw new ResourceNotFoundException("User", "id", userId);
            }
            
            User user = userOptional.get();
            log.debug("User found: {}", user.getUsername());
            
            // Convert User to UserProfileDto
            UserProfileDto profileDto = new UserProfileDto(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getRole(),
                    user.getAvatar(),
                    user.getPhone(),
                    user.getRealName(),
                    user.getCreatedAt(),
                    user.getUpdatedAt()
            );
            
            return ResponseEntity.ok(ApiResponse.success("获取用户信息成功", profileDto));
        }
        
        log.warn("No authenticated user found or principal is not UserDetailsImpl");
        return ResponseEntity.status(401).body(ApiResponse.fail("用户未认证"));
    }
    
    /**
     * Update the current user's profile
     * @param updateRequest the updated profile information
     * @return the updated user profile
     */
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileDto>> updateProfile(@Valid @RequestBody UpdateProfileRequest updateRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        log.debug("Updating profile for authenticated user: {}", authentication.getName());
        
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Long userId = userDetails.getId();
            
            Optional<User> userOptional = userService.getUserById(userId);
            if (!userOptional.isPresent()) {
                log.warn("User with ID {} not found in database", userId);
                throw new ResourceNotFoundException("User", "id", userId);
            }
            
            User user = userOptional.get();
            log.debug("Found user: {}", user.getUsername());
            
            // Check if email is already used by another user
            if (!user.getEmail().equals(updateRequest.getEmail()) && 
                userService.existsByEmail(updateRequest.getEmail())) {
                throw new BadRequestException("邮箱已被使用");
            }
            
            // Create a copy of the existing user with only updated fields
            User updatedUser = new User();
            updatedUser.setId(user.getId());
            updatedUser.setUsername(user.getUsername()); // Preserve username
            updatedUser.setPassword(user.getPassword()); // Important: Preserve password
            updatedUser.setRole(user.getRole()); // Preserve role
            updatedUser.setIsEnabled(user.getIsEnabled()); // Preserve enabled status
            updatedUser.setCreatedAt(user.getCreatedAt()); // Preserve creation timestamp
            
            // Update the fields that were part of the request
            updatedUser.setEmail(updateRequest.getEmail());
            updatedUser.setPhone(updateRequest.getPhone());
            updatedUser.setRealName(updateRequest.getRealName());
            
            if (updateRequest.getAvatar() != null && !updateRequest.getAvatar().isEmpty()) {
                updatedUser.setAvatar(updateRequest.getAvatar());
            } else {
                updatedUser.setAvatar(user.getAvatar()); // Preserve avatar if not in request
            }
            
            updatedUser.setUpdatedAt(LocalDateTime.now());
            
            // Save updated user
            User resultUser = userService.updateUser(updatedUser);
            log.debug("User profile updated successfully for user: {}", resultUser.getUsername());
            
            // Convert to DTO
            UserProfileDto profileDto = new UserProfileDto(
                    resultUser.getId(),
                    resultUser.getUsername(),
                    resultUser.getEmail(),
                    resultUser.getRole(),
                    resultUser.getAvatar(),
                    resultUser.getPhone(),
                    resultUser.getRealName(),
                    resultUser.getCreatedAt(),
                    resultUser.getUpdatedAt()
            );
            
            return ResponseEntity.ok(ApiResponse.success("个人信息更新成功", profileDto));
        }
        
        log.warn("No authenticated user found or principal is not UserDetailsImpl");
        return ResponseEntity.status(401).body(ApiResponse.fail("用户未认证"));
    }
    
    /**
     * Change the current user's password
     * @param passwordRequest the password change request
     * @return success message
     */
    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(@Valid @RequestBody ChangePasswordRequest passwordRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        log.debug("Changing password for authenticated user: {}", authentication.getName());
        
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Long userId = userDetails.getId();
            
            Optional<User> userOptional = userService.getUserById(userId);
            if (!userOptional.isPresent()) {
                log.warn("User with ID {} not found in database", userId);
                throw new ResourceNotFoundException("User", "id", userId);
            }
            
            User user = userOptional.get();
            log.debug("Found user: {}", user.getUsername());
            
            // Verify current password
            if (!passwordEncoder.matches(passwordRequest.getOldPassword(), user.getPassword())) {
                log.warn("Invalid current password for user: {}", user.getUsername());
                throw new BadCredentialsException("当前密码不正确");
            }
            
            // Create a new user object with only the password updated
            User passwordUpdateUser = new User();
            passwordUpdateUser.setId(user.getId());
            passwordUpdateUser.setUsername(user.getUsername());
            passwordUpdateUser.setEmail(user.getEmail());
            passwordUpdateUser.setRole(user.getRole());
            passwordUpdateUser.setAvatar(user.getAvatar());
            passwordUpdateUser.setPhone(user.getPhone());
            passwordUpdateUser.setRealName(user.getRealName());
            passwordUpdateUser.setCreatedAt(user.getCreatedAt());
            passwordUpdateUser.setIsEnabled(user.getIsEnabled());
            
            // Set new password - this will be encoded in the service layer
            passwordUpdateUser.setPassword(passwordRequest.getNewPassword());
            passwordUpdateUser.setUpdatedAt(LocalDateTime.now());
            
            // Save updated user (password will be encoded in service layer)
            userService.updateUser(passwordUpdateUser);
            log.debug("Password changed successfully for user: {}", user.getUsername());
            
            return ResponseEntity.ok(ApiResponse.success("密码修改成功", null));
        }
        
        log.warn("No authenticated user found or principal is not UserDetailsImpl");
        return ResponseEntity.status(401).body(ApiResponse.fail("用户未认证"));
    }
} 