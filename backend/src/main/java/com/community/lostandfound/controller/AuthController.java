package com.community.lostandfound.controller;

import com.community.lostandfound.dto.auth.JwtResponse;
import com.community.lostandfound.dto.auth.LoginRequest;
import com.community.lostandfound.dto.auth.RegisterRequest;
import com.community.lostandfound.dto.common.ApiResponse;
import com.community.lostandfound.entity.User;
import com.community.lostandfound.exception.BadRequestException;
import com.community.lostandfound.security.JwtUtils;
import com.community.lostandfound.security.UserDetailsImpl;
import com.community.lostandfound.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        JwtResponse response = new JwtResponse(
                jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                userDetails.getRole(),
                null // Avatar is null for now, can be added later
        );
        
        return ResponseEntity.ok(ApiResponse.success("登录成功", response));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        // Check if username is already taken
        if (userService.existsByUsername(registerRequest.getUsername())) {
            throw new BadRequestException("用户名已被使用");
        }

        // Check if email is already in use
        if (userService.existsByEmail(registerRequest.getEmail())) {
            throw new BadRequestException("邮箱已被使用");
        }

        // Create new user
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(registerRequest.getPassword()); // Will be encoded in service
        user.setRole("resident");
        user.setIsEnabled(true);
        user.setIsLocked(false);
        
        userService.registerUser(user);
        
        return ResponseEntity.ok(ApiResponse.success("注册成功，请登录", null));
    }
} 