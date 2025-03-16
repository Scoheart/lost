package com.community.lostandfound.controller;

import com.community.lostandfound.dto.common.ApiResponse;
import com.community.lostandfound.dto.file.FileUploadResponse;
import com.community.lostandfound.entity.User;
import com.community.lostandfound.exception.BadRequestException;
import com.community.lostandfound.security.CurrentUser;
import com.community.lostandfound.security.UserDetailsImpl;
import com.community.lostandfound.service.FileStorageService;
import com.community.lostandfound.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 文件上传控制器
 * 处理通用文件上传和用户头像上传
 */
@Slf4j
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileStorageService fileStorageService;
    private final UserService userService;

    @Value("${file.upload.allowed-extensions:jpg,jpeg,png,gif}")
    private String allowedExtensions;

    @Value("${file.upload.max-file-size:5}")
    private long maxFileSize; // MB

    /**
     * 通用文件上传接口
     *
     * @param file 要上传的文件
     * @param type 文件类型（可选，如 'avatar', 'item-image'）
     * @return 上传结果
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", required = false, defaultValue = "general") String type,
            @CurrentUser UserDetailsImpl currentUser) {

        log.info("文件上传请求: 文件大小={}, 类型={}, 用户={}", 
                file.getSize(), type, currentUser != null ? currentUser.getUsername() : "unknown");

        // 验证文件类型
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        List<String> allowed = Arrays.asList(allowedExtensions.split(","));

        if (!allowed.contains(extension.toLowerCase())) {
            log.warn("不支持的文件类型: {}", extension);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("不支持的文件类型，支持的格式: " + allowedExtensions));
        }

        // 验证文件大小 (Bytes to MB)
        if (file.getSize() > maxFileSize * 1024 * 1024) {
            log.warn("文件过大: {}MB, 最大限制: {}MB", file.getSize() / (1024 * 1024), maxFileSize);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("文件过大，最大支持: " + maxFileSize + "MB"));
        }

        try {
            // 生成一个唯一的文件名，保持原始扩展名
            String newFilename = generateUniqueFilename(originalFilename);
            
            // 保存文件
            String filePath = fileStorageService.storeFile(file, newFilename, type);
            
            // 构建文件访问URL
            String fileUrl = fileStorageService.getFileUrl(filePath);
            
            log.info("文件上传成功: 原始文件名={}, 保存路径={}", originalFilename, filePath);
            
            FileUploadResponse response = new FileUploadResponse(
                    filePath,
                    fileUrl,
                    file.getSize(),
                    file.getContentType(),
                    type
            );
            
            return ResponseEntity.ok(ApiResponse.success("文件上传成功", response));
            
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("文件上传失败: " + e.getMessage()));
        }
    }

    /**
     * 用户头像上传接口
     *
     * @param file 头像文件
     * @param currentUser 当前用户
     * @return 上传结果
     */
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @CurrentUser UserDetailsImpl currentUser) {

        log.info("头像上传请求: 文件大小={}, 用户={}", 
                file.getSize(), currentUser != null ? currentUser.getUsername() : "unknown");

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("未授权操作，请先登录"));
        }

        // 验证文件类型
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        List<String> allowed = Arrays.asList("jpg", "jpeg", "png", "gif");

        if (!allowed.contains(extension.toLowerCase())) {
            log.warn("不支持的头像格式: {}", extension);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("不支持的头像格式，支持的格式: jpg, jpeg, png, gif"));
        }

        // 头像大小限制为2MB
        if (file.getSize() > 2 * 1024 * 1024) {
            log.warn("头像文件过大: {}MB, 最大限制: 2MB", file.getSize() / (1024 * 1024));
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("头像文件过大，最大支持2MB"));
        }

        try {
            // 获取用户信息
            Optional<User> userOptional = userService.getUserById(currentUser.getId());
            if (!userOptional.isPresent()) {
                throw new BadRequestException("用户不存在");
            }
            
            User user = userOptional.get();
            
            // 生成头像文件名
            String avatarFilename = "avatar_" + user.getId() + "_" + System.currentTimeMillis() + "." + extension;
            
            // 保存头像文件
            String filePath = fileStorageService.storeFile(file, avatarFilename, "avatars");
            
            // 构建头像URL
            String fileUrl = fileStorageService.getFileUrl(filePath);
            
            // 更新用户头像
            user.setAvatar(fileUrl);
            user.setUpdatedAt(LocalDateTime.now());
            userService.updateUser(user);
            
            log.info("用户头像更新成功: 用户ID={}, 新头像路径={}", user.getId(), fileUrl);
            
            FileUploadResponse response = new FileUploadResponse(
                    filePath,
                    fileUrl,
                    file.getSize(),
                    file.getContentType(),
                    "avatar"
            );
            
            return ResponseEntity.ok(ApiResponse.success("头像上传成功", response));
            
        } catch (Exception e) {
            log.error("头像上传失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("头像上传失败: " + e.getMessage()));
        }
    }

    /**
     * 提取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty() || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * 生成唯一的文件名
     */
    private String generateUniqueFilename(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String uniqueId = UUID.randomUUID().toString();
        return uniqueId + (extension.isEmpty() ? "" : "." + extension);
    }
} 