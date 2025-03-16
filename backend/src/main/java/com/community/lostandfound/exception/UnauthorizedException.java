package com.community.lostandfound.exception;

/**
 * 未授权异常
 * 当用户尝试执行没有权限的操作时抛出
 */
public class UnauthorizedException extends RuntimeException {
    
    public UnauthorizedException(String message) {
        super(message);
    }
    
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
} 