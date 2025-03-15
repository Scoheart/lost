package com.community.lostandfound.security;

import com.community.lostandfound.entity.User;
import com.community.lostandfound.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    
    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        log.debug("Loading user by username or email: {}", usernameOrEmail);
        
        Optional<User> user = userService.getUserByUsernameOrEmail(usernameOrEmail);
                
        // If not found, throw exception
        if (!user.isPresent()) {
            log.warn("User not found with username or email: {}", usernameOrEmail);
            throw new UsernameNotFoundException("User Not Found with username or email: " + usernameOrEmail);
        }
        
        User foundUser = user.get();
        log.debug("User found: id={}, username={}, email={}", 
                foundUser.getId(), foundUser.getUsername(), foundUser.getEmail());

        return UserDetailsImpl.build(foundUser);
    }
} 