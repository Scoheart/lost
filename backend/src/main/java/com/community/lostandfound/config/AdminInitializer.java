package com.community.lostandfound.config;

import com.community.lostandfound.entity.User;
import com.community.lostandfound.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 系统初始化组件，负责在系统启动时创建初始管理员账户
 */
@Slf4j
@Component
public class AdminInitializer implements ApplicationRunner {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    
    // 初始管理员配置
    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_EMAIL = "admin@example.com";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin123";
    private static final String DEFAULT_SYSADMIN_USERNAME = "sysadmin";
    private static final String DEFAULT_SYSADMIN_EMAIL = "sysadmin@example.com";
    private static final String DEFAULT_SYSADMIN_PASSWORD = "admin123";
    
    @Autowired
    public AdminInitializer(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    public void run(ApplicationArguments args) {
        log.info("正在检查系统管理员账户...");
        
        // 1. 检查是否已有系统管理员
        List<User> allUsers = userService.getAllUsers();
        boolean hasSysAdmin = allUsers.stream()
                .anyMatch(u -> "sysadmin".equals(u.getRole()));
        
        // 2. 如果没有系统管理员，创建一个
        if (!hasSysAdmin) {
            createInitialSysAdmin();
        } else {
            log.info("系统中已存在系统管理员账户，跳过初始化");
            
            // 尝试重置密码，如果登录有问题
            Optional<User> existingAdmin = allUsers.stream()
                    .filter(u -> DEFAULT_ADMIN_USERNAME.equals(u.getUsername()) || DEFAULT_SYSADMIN_USERNAME.equals(u.getUsername()))
                    .findFirst();
            
            if (existingAdmin.isPresent()) {
                resetAdminPassword(existingAdmin.get());
            }
        }
    }
    
    /**
     * 创建初始系统管理员账户
     */
    private void createInitialSysAdmin() {
        log.info("系统中没有系统管理员账户，正在创建初始管理员...");
        
        try {
            // 先检查用户名是否已存在
            if (userService.existsByUsername(DEFAULT_SYSADMIN_USERNAME)) {
                log.warn("用户名 {} 已存在，但角色不是系统管理员", DEFAULT_SYSADMIN_USERNAME);
                return;
            }
            
            // 创建新的系统管理员
            User admin = new User();
            admin.setUsername(DEFAULT_SYSADMIN_USERNAME);
            admin.setEmail(DEFAULT_SYSADMIN_EMAIL);
            admin.setPassword(DEFAULT_SYSADMIN_PASSWORD); // 将被service自动加密
            admin.setRole("sysadmin");
            admin.setRealName("系统管理员");
            admin.setPhone("");
            admin.setCreatedAt(LocalDateTime.now());
            admin.setUpdatedAt(LocalDateTime.now());
            admin.setIsEnabled(true);
            admin.setIsLocked(false);
            
            User createdAdmin = userService.registerUser(admin);
            
            log.info("初始系统管理员创建成功 - ID: {}, 用户名: {}, 密码: {}", 
                    createdAdmin.getId(), createdAdmin.getUsername(), DEFAULT_SYSADMIN_PASSWORD);
            
            // 验证密码哈希是否正确
            String encodedPassword = createdAdmin.getPassword();
            boolean matches = passwordEncoder.matches(DEFAULT_SYSADMIN_PASSWORD, encodedPassword);
            log.info("密码验证结果: {}, 密码哈希: {}", matches, encodedPassword);
            
            if (!matches) {
                log.warn("密码验证失败，可能存在加密问题");
            }
            
        } catch (Exception e) {
            log.error("创建系统管理员失败", e);
        }
    }
    
    /**
     * 重置管理员密码
     */
    private void resetAdminPassword(User admin) {
        try {
            log.info("正在重置管理员 {} 的密码...", admin.getUsername());
            
            // 手动设置明文密码(将被service加密)
            String defaultPassword = "sysadmin".equals(admin.getRole()) 
                    ? DEFAULT_SYSADMIN_PASSWORD 
                    : DEFAULT_ADMIN_PASSWORD;
            
            admin.setPassword(defaultPassword);
            admin.setUpdatedAt(LocalDateTime.now());
            
            User updatedAdmin = userService.updateUser(admin);
            
            log.info("管理员 {} 密码重置成功，新密码: {}", 
                    updatedAdmin.getUsername(), defaultPassword);
            
            // 验证密码哈希是否正确
            String encodedPassword = updatedAdmin.getPassword();
            boolean matches = passwordEncoder.matches(defaultPassword, encodedPassword);
            log.info("密码验证结果: {}, 密码哈希: {}", matches, encodedPassword);
            
            if (!matches) {
                log.warn("密码验证失败，可能存在加密问题");
                
                // 尝试直接更新密码哈希
                String directlyEncoded = passwordEncoder.encode(defaultPassword);
                log.info("直接加密的密码哈希: {}", directlyEncoded);
                
                // 检查是否可以匹配
                boolean directMatches = passwordEncoder.matches(defaultPassword, directlyEncoded);
                log.info("直接加密密码验证结果: {}", directMatches);
            }
            
        } catch (Exception e) {
            log.error("重置管理员密码失败", e);
        }
    }
} 