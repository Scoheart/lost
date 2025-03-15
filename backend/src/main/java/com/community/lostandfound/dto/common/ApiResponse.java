package com.community.lostandfound.dto.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 通用API响应对象
 * @param <T> 响应数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private int code;
    
    /**
     * 创建成功响应（带数据）
     * 
     * @param message 成功消息
     * @param data 响应数据
     * @return 成功响应
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, HttpStatus.OK.value());
    }
    
    /**
     * 创建成功响应（无数据）
     * 
     * @param message 成功消息
     * @return 成功响应
     */
    public static <T> ApiResponse<T> success(String message) {
        return success(message, null);
    }
    
    /**
     * 创建失败响应
     * 
     * @param message 错误消息
     * @return 失败响应
     */
    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>(false, message, null, HttpStatus.BAD_REQUEST.value());
    }
    
    /**
     * 创建自定义错误响应
     * 
     * @param message 错误消息
     * @param status HTTP状态码
     * @return 错误响应
     */
    public static <T> ApiResponse<T> error(String message, HttpStatus status) {
        return new ApiResponse<>(false, message, null, status.value());
    }
    
    /**
     * 简单构造函数
     * 
     * @param success 是否成功
     * @param message 消息
     */
    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.data = null;
        this.code = success ? HttpStatus.OK.value() : HttpStatus.BAD_REQUEST.value();
    }
} 