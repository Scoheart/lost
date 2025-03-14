package com.community.lostandfound.service.impl;

import com.community.lostandfound.entity.User;
import com.community.lostandfound.exception.ResourceNotFoundException;
import com.community.lostandfound.repository.UserRepository;
import com.community.lostandfound.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
        
        // Only update the password if it has been provided
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(existingUser.getPassword());
        }
        
        // Set update timestamp
        user.setUpdatedAt(LocalDateTime.now());
        
        // Update user
        userRepository.update(user);
        
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

    @Override
    @Transactional
    public User lockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        
        user.setIsLocked(true);
        user.setUpdatedAt(LocalDateTime.now());
        
        userRepository.update(user);
        
        return user;
    }

    @Override
    @Transactional
    public User unlockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        
        user.setIsLocked(false);
        user.setUpdatedAt(LocalDateTime.now());
        
        userRepository.update(user);
        
        return user;
    }
} 