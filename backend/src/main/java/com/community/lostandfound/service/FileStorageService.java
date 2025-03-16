package com.community.lostandfound.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 文件存储服务接口
 */
public interface FileStorageService {
    
    /**
     * 保存文件到存储系统
     *
     * @param file 上传的文件
     * @param filename 文件名
     * @param subdirectory 子目录（可选，如avatars, item-images等）
     * @return 保存后的文件路径
     * @throws IOException 文件操作异常
     */
    String storeFile(MultipartFile file, String filename, String subdirectory) throws IOException;
    
    /**
     * 获取文件的访问URL
     *
     * @param filePath 文件路径
     * @return 文件访问URL
     */
    String getFileUrl(String filePath);
    
    /**
     * 删除文件
     *
     * @param filePath 文件路径
     * @return 是否删除成功
     */
    boolean deleteFile(String filePath);
} 