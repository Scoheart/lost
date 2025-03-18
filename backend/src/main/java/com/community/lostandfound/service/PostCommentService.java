package com.community.lostandfound.service;

import com.community.lostandfound.dto.comment.PostCommentDto;
import com.community.lostandfound.dto.comment.PostCommentPageDto;
import com.community.lostandfound.dto.comment.CreatePostCommentRequest;
import com.community.lostandfound.entity.PostComment;

import java.util.List;
import java.util.Optional;

/**
 * 帖子评论服务接口
 */
public interface PostCommentService {
    
    /**
     * 创建帖子评论
     *
     * @param request 创建评论请求
     * @param userId  用户ID
     * @return 创建的评论
     */
    PostCommentDto createComment(CreatePostCommentRequest request, Long userId);
    
    /**
     * 获取帖子的评论列表（分页）
     *
     * @param postId 帖子ID
     * @param page   页码
     * @param size   每页条数
     * @return 评论分页列表
     */
    PostCommentPageDto getCommentsByPost(Long postId, int page, int size);
    
    /**
     * 获取帖子的评论列表（不分页）
     *
     * @param postId 帖子ID
     * @return 评论列表
     */
    List<PostCommentDto> getAllCommentsByPost(Long postId);
    
    /**
     * 根据ID获取评论
     *
     * @param id 评论ID
     * @return 评论对象
     */
    Optional<PostCommentDto> getCommentById(Long id);
    
    /**
     * 删除评论
     *
     * @param id     评论ID
     * @param userId 用户ID
     * @return 是否删除成功
     */
    boolean deleteComment(Long id, Long userId);
    
    /**
     * 获取用户的评论列表
     *
     * @param userId 用户ID
     * @return 评论列表
     */
    List<PostCommentDto> getCommentsByUser(Long userId);
    
    /**
     * 将实体转换为DTO
     *
     * @param comment 评论实体
     * @return 评论DTO
     */
    PostCommentDto convertToDto(PostComment comment);
} 