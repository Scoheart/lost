package com.community.lostandfound.service.impl;

import com.community.lostandfound.entity.LostItem;
import com.community.lostandfound.exception.ResourceNotFoundException;
import com.community.lostandfound.repository.LostItemRepository;
import com.community.lostandfound.service.LostItemService;
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
 * 寻物启事服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LostItemServiceImpl implements LostItemService {

    private final LostItemRepository lostItemRepository;
    
    // 有效的状态值
    private static final Set<String> VALID_STATUSES = new HashSet<>(Arrays.asList("pending", "found", "closed"));

    @Override
    @Transactional
    public LostItem createLostItem(LostItem lostItem) {
        log.debug("创建寻物启事: {}", lostItem.getTitle());
        lostItemRepository.save(lostItem);
        return lostItem;
    }

    @Override
    public List<LostItem> getAllLostItems(String category, String status, String keyword, int offset, int size) {
        log.debug("查询寻物启事列表, 分类: {}, 状态: {}, 关键词: {}, 偏移量: {}, 每页条数: {}", 
                category, status, keyword, offset, size);
        return lostItemRepository.findAll(category, status, keyword, offset, size);
    }

    @Override
    public int countAllLostItems(String category, String status, String keyword) {
        log.debug("统计寻物启事数量, 分类: {}, 状态: {}, 关键词: {}", category, status, keyword);
        return lostItemRepository.countAll(category, status, keyword);
    }

    @Override
    public Optional<LostItem> getLostItemById(Long id) {
        log.debug("根据ID查询寻物启事: {}", id);
        return lostItemRepository.findById(id);
    }

    @Override
    @Transactional
    public LostItem updateLostItem(LostItem lostItem, Long userId) {
        log.debug("更新寻物启事: {}", lostItem.getId());
        
        // 检查寻物启事是否存在
        Optional<LostItem> existingItem = lostItemRepository.findById(lostItem.getId());
        if (!existingItem.isPresent()) {
            throw new ResourceNotFoundException("寻物启事不存在: ID = " + lostItem.getId());
        }
        
        // 检查是否是发布者
        LostItem item = existingItem.get();
        if (!item.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权修改此寻物启事");
        }
        
        // 保留原有的不可修改字段
        lostItem.setUserId(item.getUserId());
        lostItem.setCreatedAt(item.getCreatedAt());
        
        // 如果状态为空，保留原有状态值
        if (lostItem.getStatus() == null) {
            log.debug("前端未提供状态值，保留原有状态: {}", item.getStatus());
            lostItem.setStatus(item.getStatus());
        } else if (!VALID_STATUSES.contains(lostItem.getStatus())) {
            // 如果提供了状态但不是有效值，使用原有状态
            log.warn("提供的状态值无效: {}, 使用原有状态: {}", lostItem.getStatus(), item.getStatus());
            lostItem.setStatus(item.getStatus());
        }
        
        // 更新寻物启事
        lostItemRepository.update(lostItem);
        
        return lostItem;
    }
    
    @Override
    @Transactional
    public LostItem updateLostItemStatus(Long id, String status, Long userId) {
        log.debug("更新寻物启事状态: ID={}, 新状态={}", id, status);
        
        // 验证状态值是否有效
        if (!VALID_STATUSES.contains(status)) {
            throw new IllegalArgumentException("无效的状态值: " + status);
        }
        
        // 检查寻物启事是否存在
        Optional<LostItem> existingItem = lostItemRepository.findById(id);
        if (!existingItem.isPresent()) {
            throw new ResourceNotFoundException("寻物启事不存在: ID = " + id);
        }
        
        // 检查是否是发布者
        LostItem item = existingItem.get();
        if (!item.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权修改此寻物启事");
        }
        
        // 更新状态
        String updatedAt = LocalDateTime.now().toString();
        lostItemRepository.updateStatus(id, status, updatedAt);
        
        // 返回更新后的物品
        item.setStatus(status);
        item.setUpdatedAt(LocalDateTime.now());
        
        return item;
    }

    @Override
    @Transactional
    public void deleteLostItem(Long id, Long userId) {
        log.debug("删除寻物启事: {}", id);
        
        // 检查寻物启事是否存在
        Optional<LostItem> existingItem = lostItemRepository.findById(id);
        if (!existingItem.isPresent()) {
            throw new ResourceNotFoundException("寻物启事不存在: ID = " + id);
        }
        
        // 检查是否是发布者
        LostItem item = existingItem.get();
        if (!item.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权删除此寻物启事");
        }
        
        // 删除寻物启事
        lostItemRepository.deleteById(id);
    }

    @Override
    public List<LostItem> getLostItemsByUserId(Long userId) {
        log.debug("查询用户的寻物启事列表, 用户ID: {}", userId);
        return lostItemRepository.findByUserId(userId);
    }
} 