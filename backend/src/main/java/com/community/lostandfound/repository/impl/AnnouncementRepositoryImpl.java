package com.community.lostandfound.repository.impl;

import com.community.lostandfound.entity.Announcement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 这个类保留作为兼容性参考，但不再被使用
 * 公告存储库已迁移到MyBatis实现方式
 * @deprecated 请直接使用AnnouncementRepository接口，它现在由MyBatis实现
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Deprecated
public class AnnouncementRepositoryImpl {

    private final JdbcTemplate jdbcTemplate;
    
    private final RowMapper<Announcement> announcementRowMapper = (rs, rowNum) -> {
        Announcement announcement = new Announcement();
        announcement.setId(rs.getLong("id"));
        announcement.setTitle(rs.getString("title"));
        announcement.setContent(rs.getString("content"));
        announcement.setAdminId(rs.getLong("admin_id"));
        announcement.setIsSticky(rs.getBoolean("is_sticky"));
        announcement.setStatus(rs.getString("status"));
        announcement.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        announcement.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        
        // Try to get admin name if joined with users table
        try {
            announcement.setAdminName(rs.getString("username"));
        } catch (Exception e) {
            // Column not available, it's ok
        }
        
        return announcement;
    };

    // 保留方法仅作参考
    // 实际代码已迁移到MyBatis XML配置中
} 