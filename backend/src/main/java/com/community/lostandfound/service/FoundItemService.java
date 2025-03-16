package com.community.lostandfound.service;

import com.community.lostandfound.entity.FoundItem;
import java.util.List;
import java.util.Optional;

/**
 * 失物招领服务接口
 * 提供失物招领功能的业务逻辑处理
 */
public interface FoundItemService {
    
    /**
     * 创建失物招领
     *
     * @param foundItem 失物招领实体
     * @return 创建成功的失物招领实体（包含ID）
     */
    FoundItem createFoundItem(FoundItem foundItem);
    
    /**
     * 查询失物招领列表（支持筛选）
     * 根据类别、状态和关键词进行筛选，支持分页
     *
     * @param category 物品分类（可选，如 "electronics", "documents", "clothing" 等）
     * @param status 状态（可选，如 "pending", "claimed", "closed"）
     * @param keyword 关键词（可选，匹配标题、描述或地点）
     * @param offset 分页偏移量
     * @param size 每页数量
     * @return 符合条件的失物招领列表
     */
    List<FoundItem> getAllFoundItems(String category, String status, String keyword, int offset, int size);
    
    /**
     * 统计符合筛选条件的失物招领总数
     *
     * @param category 物品分类（可选）
     * @param status 状态（可选）
     * @param keyword 关键词（可选）
     * @return 符合条件的失物招领总数
     */
    int countAllFoundItems(String category, String status, String keyword);
    
    /**
     * 根据ID查询失物招领详情
     *
     * @param id 失物招领ID
     * @return 失物招领详情（Optional包装，可能为空）
     */
    Optional<FoundItem> getFoundItemById(Long id);
    
    /**
     * 更新失物招领信息
     * 只有物品发布者或管理员可以更新
     *
     * @param foundItem 更新的失物招领信息
     * @param userId 当前用户ID
     * @return 更新后的失物招领
     * @throws ResourceNotFoundException 如果失物招领不存在
     * @throws IllegalArgumentException 如果用户无权更新
     */
    FoundItem updateFoundItem(FoundItem foundItem, Long userId);
    
    /**
     * 更新失物招领状态
     * 支持的状态转换：待认领 -> 已认领/已关闭
     *
     * @param id 失物招领ID
     * @param status 新状态 (例如: "claimed", "closed")
     * @param userId 当前用户ID
     * @return 更新后的失物招领
     * @throws ResourceNotFoundException 如果失物招领不存在
     * @throws IllegalArgumentException 如果用户无权更新或状态无效
     */
    FoundItem updateFoundItemStatus(Long id, String status, Long userId);
    
    /**
     * 删除失物招领
     * 只有物品发布者或管理员可以删除
     *
     * @param id 失物招领ID
     * @param userId 当前用户ID
     * @throws ResourceNotFoundException 如果失物招领不存在
     * @throws IllegalArgumentException 如果用户无权删除
     */
    void deleteFoundItem(Long id, Long userId);
    
    /**
     * 查询当前用户发布的失物招领
     *
     * @param userId 用户ID
     * @return 该用户发布的所有失物招领
     */
    List<FoundItem> getFoundItemsByUserId(Long userId);
} 