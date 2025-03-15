package com.community.lostandfound.service;

import com.community.lostandfound.entity.LostItem;
import java.util.List;
import java.util.Optional;

/**
 * 寻物启事服务接口
 */
public interface LostItemService {
    
    /**
     * 创建寻物启事
     *
     * @param lostItem 寻物启事
     * @return 创建的寻物启事
     */
    LostItem createLostItem(LostItem lostItem);
    
    /**
     * 查询寻物启事列表（支持筛选）
     *
     * @param category 物品分类
     * @param status 状态
     * @param keyword 关键词
     * @param offset 偏移量
     * @param size 每页条数
     * @return 寻物启事列表
     */
    List<LostItem> getAllLostItems(String category, String status, String keyword, int offset, int size);
    
    /**
     * 统计寻物启事总数（支持筛选）
     *
     * @param category 物品分类
     * @param status 状态
     * @param keyword 关键词
     * @return 寻物启事总数
     */
    int countAllLostItems(String category, String status, String keyword);
    
    /**
     * 根据ID查询寻物启事
     *
     * @param id 寻物启事ID
     * @return 寻物启事（可能为空）
     */
    Optional<LostItem> getLostItemById(Long id);
    
    /**
     * 更新寻物启事
     *
     * @param lostItem 更新的寻物启事
     * @param userId 当前用户ID
     * @return 更新后的寻物启事
     * @throws ResourceNotFoundException 如果寻物启事不存在
     * @throws IllegalArgumentException 如果用户无权更新
     */
    LostItem updateLostItem(LostItem lostItem, Long userId);
    
    /**
     * 更新寻物启事状态
     *
     * @param id 寻物启事ID
     * @param status 新状态 (例如: "found", "closed")
     * @param userId 当前用户ID
     * @return 更新后的寻物启事
     * @throws ResourceNotFoundException 如果寻物启事不存在
     * @throws IllegalArgumentException 如果用户无权更新或状态无效
     */
    LostItem updateLostItemStatus(Long id, String status, Long userId);
    
    /**
     * 删除寻物启事
     *
     * @param id 寻物启事ID
     * @param userId 当前用户ID
     * @throws ResourceNotFoundException 如果寻物启事不存在
     * @throws IllegalArgumentException 如果用户无权删除
     */
    void deleteLostItem(Long id, Long userId);
    
    /**
     * 查询用户的寻物启事
     *
     * @param userId 用户ID
     * @return 用户的寻物启事列表
     */
    List<LostItem> getLostItemsByUserId(Long userId);
} 