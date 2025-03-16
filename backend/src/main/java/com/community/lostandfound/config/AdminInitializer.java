package com.community.lostandfound.config;

import com.community.lostandfound.entity.User;
import com.community.lostandfound.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataAccessException;
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
        
        try {
            // 1. 尝试获取所有用户
            List<User> allUsers;
            try {
                allUsers = userService.getAllUsers();
                
                // 2. 检查是否已有系统管理员
                boolean hasSysAdmin = allUsers.stream()
                        .anyMatch(u -> "sysadmin".equals(u.getRole()));
                
                // 3. 如果没有系统管理员，创建一个
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
            } catch (DataAccessException e) {
                log.error("获取用户列表时出错，可能是数据库表结构不匹配", e);
                // 尝试直接创建管理员，使用更直接的方法
                createSysAdminDirectly();
            }
        } catch (Exception e) {
            log.error("初始化管理员账户时发生错误", e);
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
            // 检查是否有realName字段
            try {
                admin.setRealName("系统管理员");
            } catch (Exception e) {
                log.warn("设置realName字段失败，该字段可能不存在", e);
            }
            admin.setPhone("");
            admin.setIsEnabled(true);
            admin.setCreatedAt(LocalDateTime.now());
            admin.setUpdatedAt(LocalDateTime.now());
            
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
     * 尝试使用JDBC直接方式创建系统管理员
     * 这是一个后备方法，当标准方法失败时使用
     */
    private void createSysAdminDirectly() {
        log.info("尝试使用备用方法创建系统管理员...");
        try {
            // 先检查用户表结构
            userService.checkUsersTableStructure();
            
            // 准备一个最基本的用户对象
            User admin = new User();
            admin.setUsername(DEFAULT_SYSADMIN_USERNAME);
            admin.setEmail(DEFAULT_SYSADMIN_EMAIL);
            // 手动加密密码
            String encryptedPassword = passwordEncoder.encode(DEFAULT_SYSADMIN_PASSWORD);
            admin.setPassword(encryptedPassword);
            admin.setRole("sysadmin");
            admin.setIsEnabled(true);
            admin.setCreatedAt(LocalDateTime.now());
            admin.setUpdatedAt(LocalDateTime.now());
            
            // 使用最小字段集创建用户
            User createdAdmin = userService.createUserWithMinimalFields(admin);
            
            if (createdAdmin != null && createdAdmin.getId() != null) {
                log.info("成功使用备用方法创建系统管理员 - ID: {}, 用户名: {}", 
                        createdAdmin.getId(), createdAdmin.getUsername());
            } else {
                log.warn("使用备用方法创建系统管理员未能返回有效用户ID");
            }
        } catch (Exception e) {
            log.error("使用备用方法创建系统管理员失败", e);
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
            
            try {
                User updatedAdmin = userService.updateUser(admin);
                
                log.info("管理员 {} 密码重置成功，新密码: {}", 
                        updatedAdmin.getUsername(), defaultPassword);
                
                // 验证密码哈希是否正确
                String encodedPassword = updatedAdmin.getPassword();
                boolean matches = passwordEncoder.matches(defaultPassword, encodedPassword);
                log.info("密码验证结果: {}, 密码哈希: {}", matches, encodedPassword);
                
                if (!matches) {
                    // 直接更新密码哈希
                    resetPasswordDirectly(admin.getId(), defaultPassword);
                }
            } catch (Exception e) {
                log.error("使用标准方法重置密码失败，尝试直接更新密码", e);
                resetPasswordDirectly(admin.getId(), defaultPassword);
            }
        } catch (Exception e) {
            log.error("重置管理员密码失败", e);
        }
    }
    
    /**
     * 直接更新密码哈希值
     */
    private void resetPasswordDirectly(Long userId, String rawPassword) {
        try {
            log.info("尝试直接更新用户 ID: {} 的密码哈希...", userId);
            String encryptedPassword = passwordEncoder.encode(rawPassword);
            
            // 尝试直接执行密码更新
            boolean updated = userService.updatePasswordDirectly(userId, encryptedPassword);
            
            if (updated) {
                log.info("直接更新密码哈希成功");
            } else {
                log.warn("直接更新密码哈希失败，请检查数据库日志");
            }
        } catch (Exception e) {
            log.error("直接更新密码哈希失败", e);
        }
    }
} 