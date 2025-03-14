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

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            log.debug("Login attempt for: {}", loginRequest.getUsernameOrEmail());

            // Print the request parameters for debugging
            log.debug("Login request parameters: username/email={}, password-length={}",
                    loginRequest.getUsernameOrEmail(),
                    loginRequest.getPassword() != null ? loginRequest.getPassword().length() : 0);

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

                JwtResponse response = new JwtResponse(
                        jwt,
                        userDetails.getId(),
                        userDetails.getUsername(),
                        userDetails.getEmail(),
                        userDetails.getRole(),
                        null // Avatar is null for now, can be added later
                );

                return ResponseEntity.ok(ApiResponse.success("登录成功", response));
            } catch (Exception e) {
                // Log the specific error that's happening after authentication
                log.error("Error after successful authentication: ", e);
                throw e; // Re-throw to be caught by the outer catch
            }
        } catch (LockedException ex) {
            log.error("Account locked: {}", ex.getMessage());
            throw ex; // Let the global exception handler handle this
        } catch (UsernameNotFoundException ex) {
            log.error("User not found: {}", ex.getMessage());
            throw new BadCredentialsException("用户名或密码错误");
        } catch (BadCredentialsException ex) {
            log.error("Bad credentials: {}", ex.getMessage());
            throw ex; // Just rethrow to be handled by global exception handler
        } catch (Exception ex) {
            log.error("Login error: ", ex); // Log the full exception with stack trace
            throw new BadCredentialsException("用户名或密码错误");
        }
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
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userService.registerUser(user);

        return ResponseEntity.ok(ApiResponse.success("注册成功，请登录", null));
    }
} 