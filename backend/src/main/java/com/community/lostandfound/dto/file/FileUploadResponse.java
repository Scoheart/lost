package com.community.lostandfound.dto.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件上传响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {
    /**
     * 文件存储路径
     */
    private String path;
    
    /**
     * 文件访问URL
     */
    private String url;
    
    /**
     * 文件大小（字节）
     */
    private long size;
    
    /**
     * 文件MIME类型
     */
    private String contentType;
    
    /**
     * 文件类型（例如：avatar, item-image等）
     */
    private String type;
} 