package com.community.lostandfound.security;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.*;

/**
 * 用于获取当前认证用户的自定义注解
 * 可以将注解应用在Controller方法参数上，Spring会自动注入当前登录用户
 * 
 * 注意：此注解现在指向UserDetailsImpl类型
 */
@Target({ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AuthenticationPrincipal
public @interface CurrentUser {
} 