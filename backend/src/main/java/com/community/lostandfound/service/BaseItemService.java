package com.community.lostandfound.service;

import com.community.lostandfound.entity.BaseItem;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 基础物品服务接口 - 为LostItemService和FoundItemService提供共同方法
 * @param <T> 物品类型，必须继承自BaseItem
 */
public interface BaseItemService<T extends BaseItem> {
    
    /**
     * 根据ID获取物品
     * @param id 物品ID
     * @return 物品详情
     */
    Optional<T> getItemById(Long id);
    
    /**
     * 创建新物品
     * @param item 物品信息
     * @param userId 用户ID
     * @return 创建的物品
     */
    T createItem(T item, Long userId);
    
    /**
     * 更新物品
     * @param id 物品ID
     * @param item 更新的物品信息
     * @param userId 用户ID
     * @return 更新后的物品
     */
    T updateItem(Long id, T item, Long userId);
    
    /**
     * 删除物品
     * @param id 物品ID
     * @param userId 用户ID
     * @return 是否删除成功
     */
    boolean deleteItem(Long id, Long userId);
    
    /**
     * 获取用户的所有物品
     * @param userId 用户ID
     * @param status 状态筛选（可选）
     * @param page 页码
     * @param size 每页数量
     * @return 物品列表及分页信息
     */
    Map<String, Object> getItemsByUser(Long userId, String status, int page, int size);
    
    /**
     * 分页获取所有物品
     * @param category 类别筛选（可选）
     * @param status 状态筛选（可选）
     * @param keyword 关键词搜索（可选）
     * @param page 页码
     * @param size 每页数量
     * @return 物品列表及分页信息
     */
    Map<String, Object> getAllItems(String category, String status, String keyword, int page, int size);
    
    /**
     * 更新物品状态
     * @param id 物品ID
     * @param status 新状态
     * @param userId 用户ID
     * @return 更新后的物品
     */
    T updateItemStatus(Long id, String status, Long userId);
} 