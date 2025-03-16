package com.community.lostandfound.service;

import com.community.lostandfound.entity.LostItem;
import java.util.List;
import java.util.Optional;

/**
 * 寻物启事服务接口
 * 提供寻物启事功能的业务逻辑处理
 */
public interface LostItemService extends BaseItemService<LostItem> {
    
    /**
     * 查询当前用户发布的寻物启事
     *
     * @param userId 用户ID
     * @return 该用户发布的所有寻物启事
     */
    List<LostItem> getLostItemsByUserId(Long userId);
    
    /**
     * 查询符合条件的寻物启事列表（不分页）
     *
     * @param category 物品分类（可选）
     * @param status 状态（可选）
     * @param keyword 关键词（可选）
     * @return 符合条件的寻物启事列表
     */
    List<LostItem> getAllLostItemsNoPage(String category, String status, String keyword);
    
    /**
     * 创建寻物启事
     *
     * @param lostItem 寻物启事实体
     * @return 创建成功的寻物启事实体（包含ID）
     */
    LostItem createLostItem(LostItem lostItem);
    
    /**
     * 查询寻物启事列表（支持筛选）
     * 根据类别、状态和关键词进行筛选，支持分页
     *
     * @param category 物品分类（可选，如 "electronics", "documents", "clothing" 等）
     * @param status 状态（可选，如 "pending", "found", "closed"）
     * @param keyword 关键词（可选，匹配标题、描述或地点）
     * @param offset 分页偏移量
     * @param size 每页数量
     * @return 符合条件的寻物启事列表
     */
    List<LostItem> getAllLostItems(String category, String status, String keyword, int offset, int size);
    
    /**
     * 统计符合筛选条件的寻物启事总数
     *
     * @param category 物品分类（可选）
     * @param status 状态（可选）
     * @param keyword 关键词（可选）
     * @return 符合条件的寻物启事总数
     */
    int countAllLostItems(String category, String status, String keyword);
    
    /**
     * 根据ID查询寻物启事详情
     *
     * @param id 寻物启事ID
     * @return 寻物启事详情（Optional包装，可能为空）
     */
    Optional<LostItem> getLostItemById(Long id);
    
    /**
     * 更新寻物启事信息
     * 只有物品发布者或管理员可以更新
     *
     * @param lostItem 更新的寻物启事信息
     * @param userId 当前用户ID
     * @return 更新后的寻物启事
     * @throws ResourceNotFoundException 如果寻物启事不存在
     * @throws IllegalArgumentException 如果用户无权更新
     */
    LostItem updateLostItem(LostItem lostItem, Long userId);
    
    /**
     * 更新寻物启事状态
     * 支持的状态转换：寻找中 -> 已找到/已关闭
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
     * 只有物品发布者或管理员可以删除
     *
     * @param id 寻物启事ID
     * @param userId 当前用户ID
     * @throws ResourceNotFoundException 如果寻物启事不存在
     * @throws IllegalArgumentException 如果用户无权删除
     */
    void deleteLostItem(Long id, Long userId);
} 