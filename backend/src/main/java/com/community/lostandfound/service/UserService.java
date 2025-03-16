package com.community.lostandfound.service;

import com.community.lostandfound.entity.User;

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
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 用户列表
     */
    List<User> getFilteredUsers(String search, String role, Boolean isEnabled, int page, int size);
    
    /**
     * 获取符合过滤条件的用户总数
     * @param search 搜索词（用户名、邮箱或电话号码）
     * @param role 用户角色
     * @param isEnabled 账号状态
     * @return 用户总数
     */
    int countFilteredUsers(String search, String role, Boolean isEnabled);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    void deleteUser(Long id);
    
    User updateUserRole(Long id, String role);
    
    User enableUser(Long id);
    
    User disableUser(Long id);
} 