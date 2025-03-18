package com.community.lostandfound.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 评论基础DTO类
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BaseCommentDto {
    private Long id;
    private String content;
    private Long userId;
    private String username;
    private String userAvatar;
    private LocalDateTime createdAt;
} 