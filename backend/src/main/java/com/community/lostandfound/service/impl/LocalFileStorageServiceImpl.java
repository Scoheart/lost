package com.community.lostandfound.service.impl;

import com.community.lostandfound.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * 本地文件存储服务实现
 * 将文件保存在本地文件系统
 */
@Slf4j
@Service
public class LocalFileStorageServiceImpl implements FileStorageService {

    @Value("${file.upload.dir:uploads}")
    private String uploadDir;
    
    @Value("${file.upload.base-url:#{null}}")
    private String baseUrl;

    /**
     * 保存文件到本地文件系统
     * 
     * @param file 上传的文件
     * @param filename 文件名
     * @param subdirectory 子目录
     * @return 保存后的文件路径
     * @throws IOException 文件操作异常
     */
    @Override
    public String storeFile(MultipartFile file, String filename, String subdirectory) throws IOException {
        // 如果子目录为null或空，则使用默认目录
        if (subdirectory == null || subdirectory.trim().isEmpty()) {
            subdirectory = "general";
        }
        
        // 创建完整的目录路径
        String directory = uploadDir + "/" + subdirectory;
        Path dirPath = Paths.get(directory).toAbsolutePath().normalize();
        
        // 确保目录存在
        Files.createDirectories(dirPath);
        
        // 解析文件名，确保是安全的
        String safeFilename = StringUtils.cleanPath(filename);
        
        // 检查文件名是否包含非法字符
        if (safeFilename.contains("..")) {
            throw new IOException("文件名包含非法字符：" + safeFilename);
        }
        
        // 文件完整路径
        Path targetPath = dirPath.resolve(safeFilename);
        
        log.debug("保存文件到: {}", targetPath);
        
        // 将文件保存到目标路径
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        
        // 返回相对路径 (例如: "uploads/avatars/filename.jpg")
        return directory + "/" + safeFilename;
    }

    /**
     * 获取文件的访问URL
     * 
     * @param filePath 文件路径
     * @return 文件访问URL
     */
    @Override
    public String getFileUrl(String filePath) {
        // 如果配置了基础URL，则使用它，否则基于当前请求构建URL
        if (baseUrl != null && !baseUrl.trim().isEmpty()) {
            return baseUrl + "/" + filePath;
        } else {
            // 基于当前请求构建完整的URL
            return ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/" + filePath)
                    .toUriString();
        }
    }

    /**
     * 删除文件
     * 
     * @param filePath 文件路径
     * @return 是否删除成功
     */
    @Override
    public boolean deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath).toAbsolutePath().normalize();
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            log.error("删除文件失败: {}", filePath, e);
            return false;
        }
    }
} 