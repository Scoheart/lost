package com.community.lostandfound.service.impl;

import com.community.lostandfound.entity.BaseItem;
import com.community.lostandfound.entity.User;
import com.community.lostandfound.exception.ResourceNotFoundException;
import com.community.lostandfound.exception.UnauthorizedException;
import com.community.lostandfound.service.BaseItemService;
import com.community.lostandfound.service.UserService;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 基础物品服务实现 - 为LostItemServiceImpl和FoundItemServiceImpl提供通用实现
 * @param <T> 物品类型，必须继承自BaseItem
 */
@Slf4j
public abstract class BaseItemServiceImpl<T extends BaseItem> implements BaseItemService<T> {
    
    protected UserService userService;
    
    /**
     * 构造函数注入UserService
     * @param userService 用户服务
     */
    public BaseItemServiceImpl(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * 获取具体的Repository接口
     * 由子类实现
     */
    protected abstract Optional<T> findById(Long id);
    
    /**
     * 保存实体
     * 由子类实现
     */
    protected abstract T save(T item);
    
    /**
     * 更新实体
     * 由子类实现
     */
    protected abstract T update(T item);
    
    /**
     * 删除实体
     * 由子类实现
     */
    protected abstract boolean delete(Long id);
    
    /**
     * 分页查询用户的物品
     * 由子类实现
     */
    protected abstract List<T> findByUserIdAndStatus(Long userId, String status, int offset, int limit);
    
    /**
     * 统计用户的物品数量
     * 由子类实现
     */
    protected abstract long countByUserIdAndStatus(Long userId, String status);
    
    /**
     * 分页查询所有物品
     * 由子类实现
     */
    protected abstract List<T> findAllWithFilters(String category, String status, String keyword, int offset, int limit);
    
    /**
     * 统计物品总数
     * 由子类实现
     */
    protected abstract long countWithFilters(String category, String status, String keyword);
    
    /**
     * 检查用户是否有权限操作物品
     * @param itemUserId 物品所有者ID
     * @param userId 当前用户ID
     * @return 是否有权限
     */
    protected boolean hasPermission(Long itemUserId, Long userId) {
        // 如果是物品的创建者，则有权限
        if (itemUserId.equals(userId)) {
            return true;
        }
        
        // 检查是否为管理员或系统管理员
        Optional<User> userOpt = userService.getUserById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String role = user.getRole();
            return "admin".equals(role) || "sysadmin".equals(role);
        }
        
        return false;
    }
    
    @Override
    public Optional<T> getItemById(Long id) {
        return findById(id);
    }
    
    @Override
    public T createItem(T item, Long userId) {
        item.setUserId(userId);
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        
        // 设置默认状态为pending，除非已经设置了其他状态
        if (item.getStatus() == null || item.getStatus().isEmpty()) {
            item.setStatus("pending");
        }
        
        return save(item);
    }
    
    @Override
    public T updateItem(Long id, T item, Long userId) {
        T existingItem = findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + id));
        
        // 检查是否是物品的创建者或管理员
        if (!hasPermission(existingItem.getUserId(), userId)) {
            throw new UnauthorizedException("您没有权限更新此物品");
        }
        
        // 保留不可修改的字段
        item.setId(id);
        item.setUserId(existingItem.getUserId());
        item.setCreatedAt(existingItem.getCreatedAt());
        item.setUpdatedAt(LocalDateTime.now());
        
        return update(item);
    }
    
    @Override
    public boolean deleteItem(Long id, Long userId) {
        T existingItem = findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + id));
        
        // 检查是否是物品的创建者或管理员
        if (!hasPermission(existingItem.getUserId(), userId)) {
            throw new UnauthorizedException("您没有权限删除此物品");
        }
        
        return delete(id);
    }
    
    @Override
    public Map<String, Object> getItemsByUser(Long userId, String status, int page, int size) {
        // 验证分页参数
        if (page < 1) page = 1;
        if (size < 1) size = 10;
        int offset = (page - 1) * size;
        
        // 查询数据
        List<T> items = findByUserIdAndStatus(userId, status, offset, size);
        long totalItems = countByUserIdAndStatus(userId, status);
        int totalPages = (int) Math.ceil((double) totalItems / size);
        
        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("items", items);
        result.put("currentPage", page);
        result.put("pageSize", size);
        result.put("totalItems", totalItems);
        result.put("totalPages", totalPages);
        
        return result;
    }
    
    @Override
    public Map<String, Object> getAllItems(String category, String status, String keyword, int page, int size) {
        // 验证分页参数
        if (page < 1) page = 1;
        if (size < 1) size = 10;
        int offset = (page - 1) * size;
        
        // 查询数据
        List<T> items = findAllWithFilters(category, status, keyword, offset, size);
        long totalItems = countWithFilters(category, status, keyword);
        int totalPages = (int) Math.ceil((double) totalItems / size);
        
        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("items", items);
        result.put("currentPage", page);
        result.put("pageSize", size);
        result.put("totalItems", totalItems);
        result.put("totalPages", totalPages);
        
        return result;
    }
    
    @Override
    public T updateItemStatus(Long id, String status, Long userId) {
        T existingItem = findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + id));
        
        // 检查是否是物品的创建者或管理员
        if (!hasPermission(existingItem.getUserId(), userId)) {
            throw new UnauthorizedException("您没有权限更新此物品状态");
        }
        
        existingItem.setStatus(status);
        existingItem.setUpdatedAt(LocalDateTime.now());
        
        return update(existingItem);
    }
} 