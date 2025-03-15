package com.community.lostandfound.service.impl;

import com.community.lostandfound.entity.User;
import com.community.lostandfound.exception.ResourceNotFoundException;
import com.community.lostandfound.repository.UserRepository;
import com.community.lostandfound.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public User registerUser(User user) {
        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // Set creation and update timestamps
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        
        // Save user
        userRepository.save(user);
        
        return user;
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
        
        // Update user
        userRepository.update(user);
        log.debug("User updated successfully: {}", existingUser.getUsername());
        
        return user;
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

    /**
     * 锁定用户
     * @param userId 用户ID
     * @return 更新后的用户对象
     */
    @Override
    public User lockUser(Long userId) {
        User user = getUserById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        user.setIsEnabled(false);
        user.setUpdatedAt(LocalDateTime.now());
        return updateUser(user);
    }
    
    /**
     * 解锁用户
     * @param userId 用户ID
     * @return 更新后的用户对象
     */
    @Override
    public User unlockUser(Long userId) {
        User user = getUserById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        user.setIsEnabled(true);
        user.setUpdatedAt(LocalDateTime.now());
        return updateUser(user);
    }
} 