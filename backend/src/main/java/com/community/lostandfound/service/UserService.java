package com.community.lostandfound.service;

import com.community.lostandfound.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    User registerUser(User user);
    
    User updateUser(User user);
    
    Optional<User> getUserById(Long id);
    
    Optional<User> getUserByUsername(String username);
    
    List<User> getAllUsers();
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    void deleteUser(Long id);
    
    User updateUserRole(Long id, String role);
    
    User enableUser(Long id);
    
    User disableUser(Long id);
    
    User lockUser(Long id);
    
    User unlockUser(Long id);
} 