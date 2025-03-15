package com.community.lostandfound.service.impl;

import com.community.lostandfound.entity.FoundItem;
import com.community.lostandfound.exception.ResourceNotFoundException;
import com.community.lostandfound.repository.FoundItemRepository;
import com.community.lostandfound.service.FoundItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 失物招领服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FoundItemServiceImpl implements FoundItemService {

    private final FoundItemRepository foundItemRepository;
    
    // 有效的状态值
    private static final Set<String> VALID_STATUSES = new HashSet<>(Arrays.asList("pending", "claimed", "closed"));

    @Override
    @Transactional
    public FoundItem createFoundItem(FoundItem foundItem) {
        log.debug("创建失物招领: {}", foundItem.getTitle());
        foundItemRepository.save(foundItem);
        return foundItem;
    }

    @Override
    public List<FoundItem> getAllFoundItems(String category, String status, String keyword, int offset, int size) {
        log.debug("查询失物招领列表, 分类: {}, 状态: {}, 关键词: {}, 偏移量: {}, 每页条数: {}", 
                category, status, keyword, offset, size);
        return foundItemRepository.findAll(category, status, keyword, offset, size);
    }

    @Override
    public int countAllFoundItems(String category, String status, String keyword) {
        log.debug("统计失物招领数量, 分类: {}, 状态: {}, 关键词: {}", category, status, keyword);
        return foundItemRepository.countAll(category, status, keyword);
    }

    @Override
    public Optional<FoundItem> getFoundItemById(Long id) {
        log.debug("根据ID查询失物招领: {}", id);
        return foundItemRepository.findById(id);
    }

    @Override
    @Transactional
    public FoundItem updateFoundItem(FoundItem foundItem, Long userId) {
        log.debug("更新失物招领: {}", foundItem.getId());
        
        // 检查失物招领是否存在
        Optional<FoundItem> existingItem = foundItemRepository.findById(foundItem.getId());
        if (!existingItem.isPresent()) {
            throw new ResourceNotFoundException("失物招领不存在: ID = " + foundItem.getId());
        }
        
        // 检查是否是发布者
        FoundItem item = existingItem.get();
        if (!item.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权修改此失物招领");
        }
        
        // 保留原有的不可修改字段
        foundItem.setUserId(item.getUserId());
        foundItem.setCreatedAt(item.getCreatedAt());
        
        // 更新失物招领
        foundItemRepository.update(foundItem);
        
        return foundItem;
    }
    
    @Override
    @Transactional
    public FoundItem updateFoundItemStatus(Long id, String status, Long userId) {
        log.debug("更新失物招领状态: ID={}, 新状态={}", id, status);
        
        // 验证状态值是否有效
        if (!VALID_STATUSES.contains(status)) {
            throw new IllegalArgumentException("无效的状态值: " + status);
        }
        
        // 检查失物招领是否存在
        Optional<FoundItem> existingItem = foundItemRepository.findById(id);
        if (!existingItem.isPresent()) {
            throw new ResourceNotFoundException("失物招领不存在: ID = " + id);
        }
        
        // 检查是否是发布者
        FoundItem item = existingItem.get();
        if (!item.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权修改此失物招领");
        }
        
        // 更新状态
        String updatedAt = LocalDateTime.now().toString();
        foundItemRepository.updateStatus(id, status, updatedAt);
        
        // 返回更新后的物品
        item.setStatus(status);
        item.setUpdatedAt(LocalDateTime.now());
        
        return item;
    }

    @Override
    @Transactional
    public void deleteFoundItem(Long id, Long userId) {
        log.debug("删除失物招领: {}", id);
        
        // 检查失物招领是否存在
        Optional<FoundItem> existingItem = foundItemRepository.findById(id);
        if (!existingItem.isPresent()) {
            throw new ResourceNotFoundException("失物招领不存在: ID = " + id);
        }
        
        // 检查是否是发布者
        FoundItem item = existingItem.get();
        if (!item.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权删除此失物招领");
        }
        
        // 删除失物招领
        foundItemRepository.deleteById(id);
    }

    @Override
    public List<FoundItem> getFoundItemsByUserId(Long userId) {
        log.debug("查询用户的失物招领列表, 用户ID: {}", userId);
        return foundItemRepository.findByUserId(userId);
    }
} 