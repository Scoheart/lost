package com.community.lostandfound.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 帖子评论实体类
 * 用于论坛帖子的评论
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class PostComment extends BaseComment {
    
    /**
     * 关联的帖子ID
     */
    private Long postId;
    
} 