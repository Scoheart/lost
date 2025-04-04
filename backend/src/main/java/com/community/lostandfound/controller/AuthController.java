package com.community.lostandfound.controller;

import com.community.lostandfound.dto.auth.JwtResponse;
import com.community.lostandfound.dto.auth.LoginRequest;
import com.community.lostandfound.dto.auth.RegisterRequest;
import com.community.lostandfound.dto.common.ApiResponse;
import com.community.lostandfound.entity.User;
import com.community.lostandfound.exception.BadRequestException;
import com.community.lostandfound.exception.ResourceNotFoundException;
import com.community.lostandfound.security.JwtUtils;
import com.community.lostandfound.security.UserDetailsImpl;
import com.community.lostandfound.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            log.debug("Login attempt for: {}", loginRequest.getUsernameOrEmail());

            // Print the request parameters for debugging
            log.debug("Login request parameters: username/email={}, password-length={}",
                    loginRequest.getUsernameOrEmail(),
                    loginRequest.getPassword() != null ? loginRequest.getPassword().length() : 0);

            // 检查用户是否被锁定 - 添加这段预检查逻辑
            Optional<User> userOpt = userService.getUserByUsernameOrEmail(loginRequest.getUsernameOrEmail());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                // 明确检查用户的锁定状态
                if (user.isLocked()) {
                    log.warn("用户被锁定，无法登录: {}", user.getUsername());
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.fail("账户已被锁定，请联系管理员解锁"));
                }
                if (!user.getIsEnabled()) {
                    log.warn("用户被禁用，无法登录: {}", user.getUsername());
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.fail("账户已被禁用，请联系管理员"));
                }
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsernameOrEmail(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Add detailed logging
            log.debug("Authentication successful in controller for: {}", loginRequest.getUsernameOrEmail());

            try {
                String jwt = jwtUtils.generateJwtToken(authentication);
                log.debug("JWT token generated successfully");

                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                log.debug("User details retrieved: {}", userDetails.getUsername());

                // Fetch complete user information from database
                Optional<User> userOptional = userService.getUserById(userDetails.getId());
                if (!userOptional.isPresent()) {
                    log.warn("User with ID {} not found in database", userDetails.getId());
                    throw new ResourceNotFoundException("User", "id", userDetails.getId());
                }

                User user = userOptional.get();
                log.debug("Complete user information retrieved from database: {}", user.getUsername());

                // Create response with complete user information
                JwtResponse response = new JwtResponse(
                        jwt,
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getRole(),
                        user.getAvatar(),
                        user.getPhone(),
                        user.getRealName(),
                        user.getAddress(),
                        user.getCreatedAt(),
                        user.getUpdatedAt()
                );

                return ResponseEntity.ok(ApiResponse.success("登录成功", response));
            } catch (Exception e) {
                // Log the specific error that's happening after authentication
                log.error("Error after successful authentication: ", e);
                throw e; // Re-throw to be caught by the outer catch
            }
        } catch (LockedException ex) {
            log.error("Account locked: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                   .body(ApiResponse.fail("账户已被锁定，请联系管理员解锁"));
        } catch (UsernameNotFoundException ex) {
            log.error("User not found: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                   .body(ApiResponse.fail("用户名或密码错误"));
        } catch (BadCredentialsException ex) {
            log.error("Bad credentials: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                   .body(ApiResponse.fail("用户名或密码错误"));
        } catch (Exception ex) {
            log.error("Login error: ", ex); // Log the full exception with stack trace
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                   .body(ApiResponse.fail("用户名或密码错误"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Object> register(@Valid @RequestBody RegisterRequest request) {
        // 先检查用户名是否已存在
        if (userService.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "用户名已存在，请更换后重试"));
        }

        // 创建用户
        User user = User.builder()
                .username(request.getUsername())
                .realName(request.getRealName())
                .email(request.getEmail()) // 可能为null
                .address(request.getAddress())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("resident")
                .isEnabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        User savedUser = userService.save(user);

        return ResponseEntity.ok(new ApiResponse(true, "注册成功"));
    }
} 