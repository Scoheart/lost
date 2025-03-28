package com.community.lostandfound.controller;

import com.community.lostandfound.dto.common.ApiResponse;
import com.community.lostandfound.dto.file.FileUploadResponse;
import com.community.lostandfound.dto.user.ChangePasswordRequest;
import com.community.lostandfound.dto.user.UpdateProfileRequest;
import com.community.lostandfound.dto.user.UserProfileDto;
import com.community.lostandfound.entity.User;
import com.community.lostandfound.exception.BadRequestException;
import com.community.lostandfound.exception.ResourceNotFoundException;
import com.community.lostandfound.security.UserDetailsImpl;
import com.community.lostandfound.service.FileStorageService;
import com.community.lostandfound.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    /**
     * Get the current authenticated user's profile
     * @return the user profile information
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileDto>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        log.debug("Authentication: {}", authentication);
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
                    user.getAddress(),
                    user.getCreatedAt(),
                    user.getUpdatedAt()
            );

            return ResponseEntity.ok(ApiResponse.success("获取用户信息成功", profileDto));
        }

        log.warn("No authenticated user found or principal is not UserDetailsImpl");
        return ResponseEntity.status(401).body(ApiResponse.fail("用户未认证"));
    }

    /**
     * 上传用户头像
     *
     * @param file 头像文件
     * @return 上传结果
     */
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        log.debug("Uploading avatar for user: {}", authentication.getName());

        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            return ResponseEntity.status(401).body(ApiResponse.fail("用户未认证"));
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();

        Optional<User> userOptional = userService.getUserById(userId);
        if (!userOptional.isPresent()) {
            throw new ResourceNotFoundException("User", "id", userId);
        }

        User user = userOptional.get();

        // 验证文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.fail("无效的文件名"));
        }

        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png", "gif");

        if (!allowedExtensions.contains(fileExtension)) {
            return ResponseEntity.badRequest().body(ApiResponse.fail("不支持的文件类型，允许的类型: jpg, jpeg, png, gif"));
        }

        // 验证文件大小（限制为2MB）
        if (file.getSize() > 2 * 1024 * 1024) {
            return ResponseEntity.badRequest().body(ApiResponse.fail("文件过大，最大允许2MB"));
        }

        try {
            // 生成唯一的文件名
            String newFilename = "avatar_" + userId + "_" + UUID.randomUUID().toString() + "." + fileExtension;

            // 保存文件
            String filePath = fileStorageService.storeFile(file, newFilename, "avatars");

            // 获取文件URL
            String fileUrl = fileStorageService.getFileUrl(filePath);

            // 更新用户头像
            user.setAvatar(fileUrl);
            user.setUpdatedAt(LocalDateTime.now());
            userService.updateUser(user);

            // 构建响应
            FileUploadResponse response = new FileUploadResponse(
                    filePath,
                    fileUrl,
                    file.getSize(),
                    file.getContentType(),
                    "avatar"
            );

            return ResponseEntity.ok(ApiResponse.success("头像上传成功", response));

        } catch (IOException e) {
            log.error("头像上传失败", e);
            return ResponseEntity.status(500).body(ApiResponse.fail("头像上传失败: " + e.getMessage()));
        }
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

            // Create a copy of the existing user with only updated fields
            User updatedUser = new User();
            updatedUser.setId(user.getId());
            updatedUser.setUsername(user.getUsername()); // Preserve username
            updatedUser.setPassword(user.getPassword()); // Important: Preserve password
            updatedUser.setRole(user.getRole()); // Preserve role
            updatedUser.setIsEnabled(user.getIsEnabled()); // Preserve enabled status
            updatedUser.setCreatedAt(user.getCreatedAt()); // Preserve creation timestamp

            // Preserve the fields that cannot be modified
            updatedUser.setRealName(user.getRealName());

            // Update the fields that were part of the request
            updatedUser.setEmail(updateRequest.getEmail());
            updatedUser.setPhone(updateRequest.getPhone());
            
            // Allow users to update their address
            if (updateRequest.getAddress() != null && !updateRequest.getAddress().isEmpty()) {
                updatedUser.setAddress(updateRequest.getAddress());
            } else {
                updatedUser.setAddress(user.getAddress()); // Preserve existing address if not provided
            }

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
                    resultUser.getAddress(),
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