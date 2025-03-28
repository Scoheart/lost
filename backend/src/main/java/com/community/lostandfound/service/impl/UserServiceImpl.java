package com.community.lostandfound.service.impl;

import com.community.lostandfound.entity.User;
import com.community.lostandfound.exception.ResourceNotFoundException;
import com.community.lostandfound.repository.UserRepository;
import com.community.lostandfound.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, 
                          @Lazy PasswordEncoder passwordEncoder,
                          JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public User save(User user) {
        try {
            // 密码已经在controller中加密，这里不需要再次加密
            
            // 确保时间戳存在
            if (user.getCreatedAt() == null) {
                user.setCreatedAt(LocalDateTime.now());
            }
            if (user.getUpdatedAt() == null) {
                user.setUpdatedAt(LocalDateTime.now());
            }
            
            // 保存用户
            userRepository.save(user);
            
            return user;
        } catch (DataAccessException e) {
            log.error("保存用户失败，可能是字段映射问题: {}", e.getMessage());
            
            // 尝试备选方法
            return createUserWithMinimalFields(user);
        }
    }

    @Override
    @Transactional
    public User registerUser(User user) {
        try {
            // Encode password
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            
            // Set creation and update timestamps
            LocalDateTime now = LocalDateTime.now();
            user.setCreatedAt(now);
            user.setUpdatedAt(now);
            
            // Save user
            userRepository.save(user);
            
            return user;
        } catch (DataAccessException e) {
            log.error("注册用户失败，可能是字段映射问题: {}", e.getMessage());
            
            // 尝试备选方法
            return createUserWithMinimalFields(user);
        }
    }

    @Override
    @Transactional
    public User updateUser(User user) {
        // Get existing user to ensure it exists
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", user.getId()));
        
        log.debug("Updating user: {}, ID: {}", existingUser.getUsername(), existingUser.getId());
        
        // Only update the password if it has been provided and is not already encoded
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            // Check if the password is already encoded (starts with $2a$)
            if (!user.getPassword().startsWith("$2a$")) {
                log.debug("Encoding new password for user: {}", existingUser.getUsername());
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            } else {
                log.debug("Password already encoded, skipping encoding");
            }
        } else {
            log.debug("Preserving existing password for user: {}", existingUser.getUsername());
            user.setPassword(existingUser.getPassword());
        }
        
        // Set update timestamp
        user.setUpdatedAt(LocalDateTime.now());
        
        try {
            // Update user
            userRepository.update(user);
            log.debug("User updated successfully: {}", existingUser.getUsername());
            
            return user;
        } catch (DataAccessException e) {
            log.error("更新用户失败，可能是字段映射问题: {}", e.getMessage());
            
            // 尝试直接更新
            boolean updated = updateUserDirectly(user);
            if (updated) {
                log.info("使用直接SQL更新用户成功");
                return user;
            } else {
                log.error("所有更新方法都失败");
                throw e;
            }
        }
    }

    /**
     * 使用直接SQL更新用户
     */
    private boolean updateUserDirectly(User user) {
        try {
            String sql = "UPDATE users SET username = ?, email = ?, password = ?, " +
                   "role = ?, updated_at = ?, is_enabled = ?, real_name = ?, phone = ?, avatar = ?, address = ?, " +
                   "is_locked = ?, lock_end_time = ?, lock_reason = ?, " +
                   "is_banned = ?, ban_end_time = ?, ban_reason = ? " +
                   "WHERE id = ?";
            
            int updated = jdbcTemplate.update(sql,
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getRole(),
                user.getUpdatedAt(),
                user.getIsEnabled(),
                user.getRealName(),
                user.getPhone(),
                user.getAvatar(),
                user.getAddress(),
                user.getIsLocked(),
                user.getLockEndTime(),
                user.getLockReason(),
                user.getIsBanned(),
                user.getBanEndTime(),
                user.getBanReason(),
                user.getId()
            );
            
            return updated > 0;
        } catch (Exception e) {
            log.error("直接更新用户失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<User> getUserByUsernameOrEmail(String usernameOrEmail) {
        Optional<User> userByUsername = userRepository.findByUsername(usernameOrEmail);
        
        return userByUsername.isPresent() 
                ? userByUsername 
                : userRepository.findByEmail(usernameOrEmail);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        // Check if user exists
        if (!userRepository.findById(id).isPresent()) {
            throw new ResourceNotFoundException("User", "id", id);
        }
        
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public User updateUserRole(Long id, String role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        
        user.setRole(role);
        user.setUpdatedAt(LocalDateTime.now());
        
        userRepository.update(user);
        
        return user;
    }

    @Override
    @Transactional
    public User enableUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        
        user.setIsEnabled(true);
        user.setUpdatedAt(LocalDateTime.now());
        
        userRepository.update(user);
        
        return user;
    }

    @Override
    @Transactional
    public User disableUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        
        user.setIsEnabled(false);
        user.setUpdatedAt(LocalDateTime.now());
        
        userRepository.update(user);
        
        return user;
    }

    public User lockUser(Long userId) {
        User user = getUserById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        user.setIsEnabled(false);
        user.setUpdatedAt(LocalDateTime.now());
        return updateUser(user);
    }
    
    public User unlockUser(Long userId) {
        User user = getUserById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        user.setIsEnabled(true);
        user.setUpdatedAt(LocalDateTime.now());
        return updateUser(user);
    }

    @Override
    public List<User> getFilteredUsers(String search, String role, Boolean isEnabled, 
            LocalDateTime startDate, LocalDateTime endDate, int page, int size) {
        int offset = page * size;
        return userRepository.findWithFilters(search, role, isEnabled, startDate, endDate, offset, size);
    }
    
    @Override
    public int countFilteredUsers(String search, String role, Boolean isEnabled,
            LocalDateTime startDate, LocalDateTime endDate) {
        return userRepository.countWithFilters(search, role, isEnabled, startDate, endDate);
    }

    @Override
    public void checkUsersTableStructure() {
        try {
            // 检查users表结构
            List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'users'"
            );
            
            log.info("数据库users表结构:");
            for (Map<String, Object> column : columns) {
                log.info("列名: {}", column.get("COLUMN_NAME"));
            }
            
            // 检查是否有real_name列
            boolean hasRealNameColumn = columns.stream()
                .anyMatch(col -> "real_name".equalsIgnoreCase(String.valueOf(col.get("COLUMN_NAME"))));
            
            if (!hasRealNameColumn) {
                log.warn("数据库中没有real_name列，这可能导致实体映射失败");
                
                // 检查是否有realname列（没有下划线）
                boolean hasRealnameColumn = columns.stream()
                    .anyMatch(col -> "realname".equalsIgnoreCase(String.valueOf(col.get("COLUMN_NAME"))));
                
                if (hasRealnameColumn) {
                    log.info("数据库中有realname列（没有下划线），应调整实体映射");
                } else {
                    log.warn("数据库中没有任何形式的realname列，考虑添加此列");
                }
            }
        } catch (Exception e) {
            log.error("检查数据库结构失败: {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public User createUserWithMinimalFields(User user) {
        try {
            log.info("尝试使用最小字段集创建用户: {}", user.getUsername());
            
            // 先检查表结构
            checkUsersTableStructure();
            
            // 构建SQL，只使用基本字段
            StringBuilder sql = new StringBuilder("INSERT INTO users(username, email, password, role");
            StringBuilder values = new StringBuilder("VALUES(?, ?, ?, ?");
            
            // 添加可选字段
            try {
                sql.append(", address, created_at, updated_at, is_enabled");
                values.append(", ?, ?, ?, ?");
            } catch (Exception e) {
                log.warn("添加可选字段失败，继续使用基本字段");
            }
            
            // 完成SQL
            sql.append(") ").append(values).append(")");
            
            // 准备参数
            Object[] params;
            try {
                params = new Object[]{
                    user.getUsername(),
                    user.getEmail(),
                    user.getPassword(),
                    user.getRole(),
                    user.getAddress(),
                    user.getCreatedAt(),
                    user.getUpdatedAt(),
                    user.getIsEnabled()
                };
            } catch (Exception e) {
                // 如果获取可选字段失败，使用更简单的参数集
                params = new Object[]{
                    user.getUsername(),
                    user.getEmail(),
                    user.getPassword(),
                    user.getRole() != null ? user.getRole() : "user"
                };
            }
            
            // 执行插入
            jdbcTemplate.update(sql.toString(), params);
            
            // 获取生成的ID
            Long userId = jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE username = ?", 
                Long.class, 
                user.getUsername()
            );
            
            if (userId != null) {
                user.setId(userId);
                log.info("成功创建用户 ID: {}, 用户名: {}", userId, user.getUsername());
                return user;
            } else {
                log.warn("创建用户成功但未能获取ID");
                return user;
            }
        } catch (Exception e) {
            log.error("使用最小字段集创建用户失败: {}", e.getMessage());
            throw new RuntimeException("无法创建用户: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updatePasswordDirectly(Long userId, String encodedPassword) {
        try {
            String sql = "UPDATE users SET password = ? WHERE id = ?";
            int updated = jdbcTemplate.update(sql, encodedPassword, userId);
            return updated > 0;
        } catch (Exception e) {
            log.error("直接更新密码失败: {}", e.getMessage());
            return false;
        }
    }
} 