package com.community.lostandfound.service;

import com.community.lostandfound.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserService {

    User registerUser(User user);
    
    User updateUser(User user);
    
    Optional<User> getUserById(Long id);
    
    Optional<User> getUserByUsername(String username);
    
    Optional<User> getUserByEmail(String email);
    
    Optional<User> getUserByUsernameOrEmail(String usernameOrEmail);
    
    List<User> getAllUsers();
    
    /**
     * 获取过滤后的用户列表
     * @param search 搜索词（用户名、邮箱或电话号码）
     * @param role 用户角色
     * @param isEnabled 账号状态
     * @param startDate 注册开始日期
     * @param endDate 注册结束日期
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 用户列表
     */
    List<User> getFilteredUsers(String search, String role, Boolean isEnabled, 
            LocalDateTime startDate, LocalDateTime endDate, int page, int size);
    
    /**
     * 获取符合过滤条件的用户总数
     * @param search 搜索词（用户名、邮箱或电话号码）
     * @param role 用户角色
     * @param isEnabled 账号状态
     * @param startDate 注册开始日期
     * @param endDate 注册结束日期
     * @return 用户总数
     */
    int countFilteredUsers(String search, String role, Boolean isEnabled,
            LocalDateTime startDate, LocalDateTime endDate);
    
    // Overloaded methods for backward compatibility
    default List<User> getFilteredUsers(String search, String role, Boolean isEnabled, int page, int size) {
        return getFilteredUsers(search, role, isEnabled, null, null, page, size);
    }
    
    default int countFilteredUsers(String search, String role, Boolean isEnabled) {
        return countFilteredUsers(search, role, isEnabled, null, null);
    }
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    void deleteUser(Long id);
    
    User updateUserRole(Long id, String role);
    
    User enableUser(Long id);
    
    User disableUser(Long id);
    
    /**
     * 检查用户表结构
     * 检查数据库中用户表的实际结构，用于诊断字段不匹配问题
     */
    void checkUsersTableStructure();
    
    /**
     * 使用最小字段集创建用户
     * 当标准方法失败时的备选方案
     * @param user 包含基本字段的用户对象
     * @return 创建的用户
     */
    User createUserWithMinimalFields(User user);
    
    /**
     * 直接更新用户密码
     * 通过直接执行SQL来更新密码，避免ORM映射问题
     * @param userId 用户ID
     * @param encodedPassword 已加密的密码
     * @return 是否更新成功
     */
    boolean updatePasswordDirectly(Long userId, String encodedPassword);
} 