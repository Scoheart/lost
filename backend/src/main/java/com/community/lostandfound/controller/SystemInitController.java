package com.community.lostandfound.controller;

import com.community.lostandfound.dto.admin.RegisterAdminRequest;
import com.community.lostandfound.dto.common.ApiResponse;
import com.community.lostandfound.entity.User;
import com.community.lostandfound.exception.BadRequestException;
import com.community.lostandfound.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 系统初始化控制器，提供初始化相关的API
 */
@Slf4j
@RestController
@RequestMapping("/system")
@RequiredArgsConstructor
public class SystemInitController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * 生成密码哈希的API，用于调试密码加密问题
     * 
     * @param password 要加密的密码
     * @return 加密后的哈希值和验证结果
     */
    @GetMapping("/password-hash")
    public ResponseEntity<ApiResponse<Map<String, Object>>> generatePasswordHash(@RequestParam String password) {
        String hashedPassword = passwordEncoder.encode(password);
        boolean matches = passwordEncoder.matches(password, hashedPassword);
        
        Map<String, Object> result = new HashMap<>();
        result.put("password", password);
        result.put("hashedPassword", hashedPassword);
        result.put("matches", matches);
        
        return ResponseEntity.ok(ApiResponse.success("密码哈希生成成功", result));
    }
    
    /**
     * 系统初始化API，只有在系统中没有管理员时才能使用
     * 
     * @param request 管理员注册请求
     * @return 成功或失败消息
     */
    @PostMapping("/init-admin")
    public ResponseEntity<ApiResponse<String>> initializeAdmin(@Valid @RequestBody RegisterAdminRequest request) {
        log.info("收到系统初始化请求，创建初始管理员: {}", request.getUsername());
        
        // 验证是否有管理员存在
        List<User> admins = userService.getAllUsers().stream()
                .filter(u -> "admin".equals(u.getRole()) || "sysadmin".equals(u.getRole()))
                .collect(Collectors.toList());
        
        // 如果已存在管理员，则禁止使用此API
        if (!admins.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.fail("系统已初始化，无法创建初始管理员"));
        }
        
        // 验证角色是否为管理员或系统管理员
        if (!("admin".equals(request.getRole()) || "sysadmin".equals(request.getRole()))) {
            throw new BadRequestException("管理员类型必须是管理员(admin)或系统管理员(sysadmin)");
        }
        
        // 检查用户名是否已存在
        if (userService.existsByUsername(request.getUsername())) {
            throw new BadRequestException("用户名已被使用");
        }
        
        // 检查邮箱是否已存在（仅当邮箱不为空时）
        if (request.getEmail() != null && !request.getEmail().isEmpty() && userService.existsByEmail(request.getEmail())) {
            throw new BadRequestException("邮箱已被使用");
        }
        
        // 创建管理员
        User admin = new User();
        admin.setUsername(request.getUsername());
        admin.setEmail(request.getEmail());
        admin.setPassword(request.getPassword()); // 将在service中加密
        admin.setRole("sysadmin");
        admin.setRealName(request.getRealName());
        admin.setPhone(request.getPhone());
        admin.setAddress(request.getAddress());
        admin.setIsLocked(false);
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());
        
        User createdAdmin = userService.registerUser(admin);
        log.info("初始管理员创建成功 - ID: {}, 用户名: {}, 角色: {}", 
                createdAdmin.getId(), createdAdmin.getUsername(), createdAdmin.getRole());
        
        // 验证密码是否正确加密
        boolean matches = passwordEncoder.matches(request.getPassword(), createdAdmin.getPassword());
        log.info("密码验证结果: {}", matches);
        
        return ResponseEntity.ok(ApiResponse.success("系统初始化成功，管理员创建完成", null));
    }
} 