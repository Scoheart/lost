package com.community.lostandfound.service;

import com.community.lostandfound.entity.FoundItem;
import java.util.List;
import java.util.Optional;

/**
 * 失物招领服务接口
 */
public interface FoundItemService {
    
    /**
     * 创建失物招领
     *
     * @param foundItem 失物招领
     * @return 创建的失物招领
     */
    FoundItem createFoundItem(FoundItem foundItem);
    
    /**
     * 查询失物招领列表（支持筛选）
     *
     * @param category 物品分类
     * @param status 状态
     * @param keyword 关键词
     * @param offset 偏移量
     * @param size 每页条数
     * @return 失物招领列表
     */
    List<FoundItem> getAllFoundItems(String category, String status, String keyword, int offset, int size);
    
    /**
     * 统计失物招领总数（支持筛选）
     *
     * @param category 物品分类
     * @param status 状态
     * @param keyword 关键词
     * @return 失物招领总数
     */
    int countAllFoundItems(String category, String status, String keyword);
    
    /**
     * 根据ID查询失物招领
     *
     * @param id 失物招领ID
     * @return 失物招领（可能为空）
     */
    Optional<FoundItem> getFoundItemById(Long id);
    
    /**
     * 更新失物招领
     *
     * @param foundItem 更新的失物招领
     * @param userId 当前用户ID
     * @return 更新后的失物招领
     * @throws ResourceNotFoundException 如果失物招领不存在
     * @throws IllegalArgumentException 如果用户无权更新
     */
    FoundItem updateFoundItem(FoundItem foundItem, Long userId);
    
    /**
     * 删除失物招领
     *
     * @param id 失物招领ID
     * @param userId 当前用户ID
     * @throws ResourceNotFoundException 如果失物招领不存在
     * @throws IllegalArgumentException 如果用户无权删除
     */
    void deleteFoundItem(Long id, Long userId);
    
    /**
     * 查询用户的失物招领
     *
     * @param userId 用户ID
     * @return 用户的失物招领列表
     */
    List<FoundItem> getFoundItemsByUserId(Long userId);
} 