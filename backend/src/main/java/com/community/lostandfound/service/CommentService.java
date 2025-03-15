package com.community.lostandfound.service;

import com.community.lostandfound.dto.comment.CommentDto;
import com.community.lostandfound.dto.comment.CommentPageDto;
import com.community.lostandfound.dto.comment.CreateCommentRequest;
import com.community.lostandfound.entity.Comment;

import java.util.List;
import java.util.Optional;

/**
 * 留言服务接口
 */
public interface CommentService {
    
    /**
     * 创建留言
     *
     * @param request 创建留言请求
     * @param userId  用户ID
     * @return 创建的留言
     */
    CommentDto createComment(CreateCommentRequest request, Long userId);
    
    /**
     * 获取物品的留言列表（分页）
     *
     * @param itemId   物品ID
     * @param itemType 物品类型
     * @param page     页码
     * @param size     每页条数
     * @return 留言分页列表
     */
    CommentPageDto getCommentsByItem(Long itemId, String itemType, int page, int size);
    
    /**
     * 获取物品的留言列表（不分页）
     *
     * @param itemId   物品ID
     * @param itemType 物品类型
     * @return 留言列表
     */
    List<CommentDto> getAllCommentsByItem(Long itemId, String itemType);
    
    /**
     * 根据ID获取留言
     *
     * @param id 留言ID
     * @return 留言对象
     */
    Optional<CommentDto> getCommentById(Long id);
    
    /**
     * 删除留言
     *
     * @param id     留言ID
     * @param userId 用户ID
     * @return 是否删除成功
     */
    boolean deleteComment(Long id, Long userId);
    
    /**
     * 获取用户的留言列表
     *
     * @param userId 用户ID
     * @return 留言列表
     */
    List<CommentDto> getCommentsByUser(Long userId);
    
    /**
     * 将实体转换为DTO
     *
     * @param comment 留言实体
     * @return 留言DTO
     */
    CommentDto convertToDto(Comment comment);
} 