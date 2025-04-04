package com.community.lostandfound.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailsServiceImpl userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public CustomAuthenticationProvider(UserDetailsServiceImpl userDetailsService, @Lazy PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String usernameOrEmail = authentication.getName();
        String password = authentication.getCredentials().toString();
        
        UserDetails userDetails = userDetailsService.loadUserByUsername(usernameOrEmail);
        
        // Check if the account is locked
        if (!userDetails.isAccountNonLocked()) {
            log.warn("Account is locked for user: {}", usernameOrEmail);
            throw new LockedException("User account is locked");
        }
        
        // Verify password
        log.debug("Verifying password for user: {}", usernameOrEmail);
        log.debug("Stored password hash: {}", userDetails.getPassword());
        boolean passwordMatches = passwordEncoder.matches(password, userDetails.getPassword());
        log.debug("Password match result: {}", passwordMatches);
        
        if (!passwordMatches) {
            log.warn("Invalid password for user: {}", usernameOrEmail);
            throw new BadCredentialsException("Invalid password");
        }
        
        log.debug("Authentication successful for user: {}", usernameOrEmail);
        return new UsernamePasswordAuthenticationToken(
                userDetails, 
                null, 
                userDetails.getAuthorities()
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
} 