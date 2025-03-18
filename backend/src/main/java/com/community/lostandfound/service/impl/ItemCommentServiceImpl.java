package com.community.lostandfound.service.impl;

import com.community.lostandfound.dto.comment.ItemCommentDto;
import com.community.lostandfound.dto.comment.ItemCommentPageDto;
import com.community.lostandfound.dto.comment.CreateItemCommentRequest;
import com.community.lostandfound.entity.ItemComment;
import com.community.lostandfound.exception.ResourceNotFoundException;
import com.community.lostandfound.repository.ItemCommentRepository;
import com.community.lostandfound.service.FoundItemService;
import com.community.lostandfound.service.ItemCommentService;
import com.community.lostandfound.service.LostItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 物品评论服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ItemCommentServiceImpl implements ItemCommentService {
    
    private final ItemCommentRepository itemCommentRepository;
    private final LostItemService lostItemService;
    private final FoundItemService foundItemService;
    
    @Override
    @Transactional
    public ItemCommentDto createComment(CreateItemCommentRequest request, Long userId) {
        log.debug("创建物品评论: {}", request);
        
        // 验证物品是否存在
        validateItemExists(request.getItemId(), request.getItemType());
        
        // 创建并保存评论
        ItemComment comment = new ItemComment();
        comment.setContent(request.getContent());
        comment.setItemId(request.getItemId());
        comment.setItemType(request.getItemType());
        comment.setUserId(userId);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
        
        itemCommentRepository.save(comment);
        log.info("物品评论创建成功: {}", comment.getId());
        
        return convertToDto(comment);
    }
    
    @Override
    public ItemCommentPageDto getCommentsByItem(Long itemId, String itemType, int page, int size) {
        log.debug("查询物品评论列表, 物品ID: {}, 物品类型: {}, 页码: {}, 每页条数: {}", 
                itemId, itemType, page, size);
                
        // 验证物品是否存在
        validateItemExists(itemId, itemType);
        
        // 计算分页参数
        int offset = (page - 1) * size;
        
        // 查询评论
        List<ItemComment> comments = itemCommentRepository.findByItemIdAndTypeWithPagination(itemId, itemType, offset, size);
        int total = itemCommentRepository.countByItemIdAndType(itemId, itemType);
        
        // 计算总页数
        int totalPages = (total + size - 1) / size;
        
        // 转换为DTO
        List<ItemCommentDto> commentDtos = comments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        // 构建分页结果
        return ItemCommentPageDto.builder()
                .comments(commentDtos)
                .currentPage(page)
                .pageSize(size)
                .totalItems(total)
                .totalPages(totalPages)
                .build();
    }
    
    @Override
    public List<ItemCommentDto> getAllCommentsByItem(Long itemId, String itemType) {
        log.debug("查询物品全部评论, 物品ID: {}, 物品类型: {}", itemId, itemType);
        
        // 验证物品是否存在
        validateItemExists(itemId, itemType);
        
        // 查询评论
        List<ItemComment> comments = itemCommentRepository.findByItemIdAndType(itemId, itemType);
        
        // 转换为DTO
        return comments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public Optional<ItemCommentDto> getCommentById(Long id) {
        log.debug("根据ID查询物品评论: {}", id);
        
        return itemCommentRepository.findById(id)
                .map(this::convertToDto);
    }
    
    @Override
    @Transactional
    public boolean deleteComment(Long id, Long userId) {
        log.debug("删除物品评论, ID: {}, 用户ID: {}", id, userId);
        
        // 查询评论
        Optional<ItemComment> optionalComment = itemCommentRepository.findById(id);
        if (optionalComment.isEmpty()) {
            log.warn("物品评论不存在: {}", id);
            return false;
        }
        
        ItemComment comment = optionalComment.get();
        
        // 检查是否为评论作者
        if (!comment.getUserId().equals(userId)) {
            log.warn("用户无权删除物品评论, 用户ID: {}, 评论作者ID: {}", userId, comment.getUserId());
            return false;
        }
        
        // 删除评论
        itemCommentRepository.deleteById(id);
        log.info("物品评论删除成功: {}", id);
        
        return true;
    }
    
    @Override
    public List<ItemCommentDto> getCommentsByUser(Long userId) {
        log.debug("查询用户物品评论列表, 用户ID: {}", userId);
        
        List<ItemComment> comments = itemCommentRepository.findByUserId(userId);
        
        return comments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public ItemCommentDto convertToDto(ItemComment comment) {
        return ItemCommentDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .itemId(comment.getItemId())
                .itemType(comment.getItemType())
                .userId(comment.getUserId())
                .username(comment.getUsername())
                .userAvatar(comment.getUserAvatar())
                .createdAt(comment.getCreatedAt())
                .build();
    }
    
    /**
     * 验证物品是否存在
     *
     * @param itemId   物品ID
     * @param itemType 物品类型
     * @throws ResourceNotFoundException 如果物品不存在
     */
    private void validateItemExists(Long itemId, String itemType) {
        if ("lost".equals(itemType)) {
            lostItemService.getLostItemById(itemId)
                    .orElseThrow(() -> new ResourceNotFoundException("寻物启事不存在: ID = " + itemId));
        } else if ("found".equals(itemType)) {
            foundItemService.getFoundItemById(itemId)
                    .orElseThrow(() -> new ResourceNotFoundException("失物招领不存在: ID = " + itemId));
        } else {
            throw new IllegalArgumentException("不支持的物品类型: " + itemType);
        }
    }
} 