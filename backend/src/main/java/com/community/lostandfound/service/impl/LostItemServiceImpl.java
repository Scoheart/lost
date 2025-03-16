package com.community.lostandfound.service.impl;

import com.community.lostandfound.entity.LostItem;
import com.community.lostandfound.exception.ResourceNotFoundException;
import com.community.lostandfound.repository.LostItemRepository;
import com.community.lostandfound.service.LostItemService;
import com.community.lostandfound.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
public class LostItemServiceImpl extends BaseItemServiceImpl<LostItem> implements LostItemService {

    private final LostItemRepository lostItemRepository;
    
    // 有效的状态值
    private static final Set<String> VALID_STATUSES = new HashSet<>(Arrays.asList("pending", "found", "closed"));

    @Autowired
    public LostItemServiceImpl(LostItemRepository lostItemRepository, UserService userService) {
        super(userService);
        this.lostItemRepository = lostItemRepository;
    }

    @Override
    @Transactional
    public LostItem createItem(LostItem lostItem, Long userId) {
        log.debug("创建寻物启事: {}", lostItem.getTitle());
        return super.createItem(lostItem, userId);
    }
    
    // 实现LostItemService的特定方法
    @Override
    public List<LostItem> getLostItemsByUserId(Long userId) {
        log.debug("查询用户的寻物启事列表, 用户ID: {}", userId);
        return lostItemRepository.findByUserId(userId);
    }
    
    @Override
    public List<LostItem> getAllLostItemsNoPage(String category, String status, String keyword) {
        log.debug("查询所有寻物启事（不分页）, 分类: {}, 状态: {}, 关键词: {}", category, status, keyword);
        return lostItemRepository.findAll(category, status, keyword, 0, Integer.MAX_VALUE);
    }
    
    // 实现BaseItemServiceImpl的抽象方法
    @Override
    protected Optional<LostItem> findById(Long id) {
        log.debug("根据ID查询寻物启事: {}", id);
        return lostItemRepository.findById(id);
    }

    @Override
    protected LostItem save(LostItem item) {
        log.debug("保存寻物启事: {}", item.getTitle());
        lostItemRepository.save(item);
        return item;
    }

    @Override
    protected LostItem update(LostItem item) {
        log.debug("更新寻物启事: {}", item.getId());
        lostItemRepository.update(item);
        return item;
    }

    @Override
    protected boolean delete(Long id) {
        log.debug("删除寻物启事: {}", id);
        try {
            lostItemRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            log.error("删除寻物启事失败: {}", id, e);
            return false;
        }
    }

    @Override
    protected List<LostItem> findByUserIdAndStatus(Long userId, String status, int offset, int limit) {
        String keyword = null;
        String category = null;
        log.debug("查询用户的寻物启事列表, 用户ID: {}, 状态: {}, 偏移量: {}, 每页条数: {}", 
                userId, status, offset, limit);
        
        // 查询条件可以根据需要调整，这里简化处理
        return lostItemRepository.findAll(category, status, keyword, offset, limit);
    }

    @Override
    protected long countByUserIdAndStatus(Long userId, String status) {
        String keyword = null;
        String category = null;
        log.debug("统计用户的寻物启事数量, 用户ID: {}, 状态: {}", userId, status);
        
        // 查询条件可以根据需要调整，这里简化处理
        return lostItemRepository.countAll(category, status, keyword);
    }

    @Override
    protected List<LostItem> findAllWithFilters(String category, String status, String keyword, int offset, int limit) {
        log.debug("查询寻物启事列表, 分类: {}, 状态: {}, 关键词: {}, 偏移量: {}, 每页条数: {}", 
                category, status, keyword, offset, limit);
        return lostItemRepository.findAll(category, status, keyword, offset, limit);
    }

    @Override
    protected long countWithFilters(String category, String status, String keyword) {
        log.debug("统计寻物启事数量, 分类: {}, 状态: {}, 关键词: {}", category, status, keyword);
        return lostItemRepository.countAll(category, status, keyword);
    }
    
    // 兼容旧代码的方法实现
    
    @Override
    public LostItem createLostItem(LostItem lostItem) {
        log.debug("调用旧版createLostItem方法，转发到新方法");
        if (lostItem.getUserId() == null) {
            throw new IllegalArgumentException("创建寻物启事必须提供userId");
        }
        return createItem(lostItem, lostItem.getUserId());
    }
    
    @Override
    public List<LostItem> getAllLostItems(String category, String status, String keyword, int offset, int size) {
        log.debug("调用旧版getAllLostItems方法，转发到新方法");
        return findAllWithFilters(category, status, keyword, offset, size);
    }
    
    @Override
    public int countAllLostItems(String category, String status, String keyword) {
        log.debug("调用旧版countAllLostItems方法，转发到新方法");
        return (int) countWithFilters(category, status, keyword);
    }
    
    @Override
    public Optional<LostItem> getLostItemById(Long id) {
        log.debug("调用旧版getLostItemById方法，转发到新方法");
        return getItemById(id);
    }
    
    @Override
    public LostItem updateLostItem(LostItem lostItem, Long userId) {
        log.debug("调用旧版updateLostItem方法，转发到新方法");
        return updateItem(lostItem.getId(), lostItem, userId);
    }
    
    @Override
    public LostItem updateLostItemStatus(Long id, String status, Long userId) {
        log.debug("调用旧版updateLostItemStatus方法，转发到新方法");
        return updateItemStatus(id, status, userId);
    }
    
    @Override
    public void deleteLostItem(Long id, Long userId) {
        log.debug("调用旧版deleteLostItem方法，转发到新方法");
        deleteItem(id, userId);
    }
} 