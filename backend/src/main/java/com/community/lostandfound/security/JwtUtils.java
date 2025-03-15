package com.community.lostandfound.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    public String generateJwtToken(Authentication authentication) {
        try {
            log.debug("Starting JWT token generation for: {}", authentication.getName());
            UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
            
            log.debug("User details: id={}, username={}, email={}, role={}", 
                    userPrincipal.getId(), 
                    userPrincipal.getUsername(), 
                    userPrincipal.getEmail(), 
                    userPrincipal.getRole());
            
            Map<String, Object> claims = new HashMap<>();
            claims.put("id", userPrincipal.getId());
            claims.put("username", userPrincipal.getUsername());
            claims.put("email", userPrincipal.getEmail());
            claims.put("role", userPrincipal.getRole());
            
            String token = buildToken(claims, userPrincipal.getUsername());
            log.debug("JWT token generation successful");
            return token;
        } catch (Exception e) {
            log.error("Error generating JWT token: ", e);
            throw e;
        }
    }
    
    private String buildToken(Map<String, Object> claims, String subject) {
        try {
            log.debug("Building token for subject: {}", subject);
            if (subject == null || subject.trim().isEmpty()) {
                log.error("Subject for JWT token is null or empty");
                throw new IllegalArgumentException("Subject cannot be null or empty");
            }
            
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + jwtExpirationMs);
            
            log.debug("Token will expire at: {}", expiryDate);
            
            return Jwts.builder()
                    .setClaims(claims)
                    .setSubject(subject)
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                    .compact();
        } catch (Exception e) {
            log.error("Error building JWT token: ", e);
            throw e;
        }
    }
    
    private Key getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }
    
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(authToken);
            return true;
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }
} 