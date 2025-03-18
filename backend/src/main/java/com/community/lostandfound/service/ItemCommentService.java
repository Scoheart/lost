package com.community.lostandfound.service;

import com.community.lostandfound.dto.comment.ItemCommentDto;
import com.community.lostandfound.dto.comment.ItemCommentPageDto;
import com.community.lostandfound.dto.comment.CreateItemCommentRequest;
import com.community.lostandfound.entity.ItemComment;

import java.util.List;
import java.util.Optional;

/**
 * 物品评论服务接口
 */
public interface ItemCommentService {
    
    /**
     * 创建物品评论
     *
     * @param request 创建评论请求
     * @param userId  用户ID
     * @return 创建的评论
     */
    ItemCommentDto createComment(CreateItemCommentRequest request, Long userId);
    
    /**
     * 获取物品的评论列表（分页）
     *
     * @param itemId   物品ID
     * @param itemType 物品类型
     * @param page     页码
     * @param size     每页条数
     * @return 评论分页列表
     */
    ItemCommentPageDto getCommentsByItem(Long itemId, String itemType, int page, int size);
    
    /**
     * 获取物品的评论列表（不分页）
     *
     * @param itemId   物品ID
     * @param itemType 物品类型
     * @return 评论列表
     */
    List<ItemCommentDto> getAllCommentsByItem(Long itemId, String itemType);
    
    /**
     * 根据ID获取评论
     *
     * @param id 评论ID
     * @return 评论对象
     */
    Optional<ItemCommentDto> getCommentById(Long id);
    
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
    List<ItemCommentDto> getCommentsByUser(Long userId);
    
    /**
     * 将实体转换为DTO
     *
     * @param comment 评论实体
     * @return 评论DTO
     */
    ItemCommentDto convertToDto(ItemComment comment);
} 