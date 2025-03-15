package com.community.lostandfound.service.impl;

import com.community.lostandfound.dto.comment.CommentDto;
import com.community.lostandfound.dto.comment.CommentPageDto;
import com.community.lostandfound.dto.comment.CreateCommentRequest;
import com.community.lostandfound.entity.Comment;
import com.community.lostandfound.exception.ResourceNotFoundException;
import com.community.lostandfound.repository.CommentRepository;
import com.community.lostandfound.service.CommentService;
import com.community.lostandfound.service.FoundItemService;
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
 * 留言服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    
    private final CommentRepository commentRepository;
    private final LostItemService lostItemService;
    private final FoundItemService foundItemService;
    
    @Override
    @Transactional
    public CommentDto createComment(CreateCommentRequest request, Long userId) {
        log.debug("创建留言: {}", request);
        
        // 验证物品是否存在
        validateItemExists(request.getItemId(), request.getItemType());
        
        // 创建并保存留言
        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setItemId(request.getItemId());
        comment.setItemType(request.getItemType());
        comment.setUserId(userId);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
        
        commentRepository.save(comment);
        log.info("留言创建成功: {}", comment.getId());
        
        return convertToDto(comment);
    }
    
    @Override
    public CommentPageDto getCommentsByItem(Long itemId, String itemType, int page, int size) {
        log.debug("查询物品留言列表, 物品ID: {}, 物品类型: {}, 页码: {}, 每页条数: {}", 
                itemId, itemType, page, size);
                
        // 验证物品是否存在
        validateItemExists(itemId, itemType);
        
        // 计算分页参数
        int offset = (page - 1) * size;
        
        // 查询留言
        List<Comment> comments = commentRepository.findByItemIdAndTypeWithPagination(itemId, itemType, offset, size);
        int total = commentRepository.countByItemIdAndType(itemId, itemType);
        
        // 计算总页数
        int totalPages = (total + size - 1) / size;
        
        // 转换为DTO
        List<CommentDto> commentDtos = comments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        // 构建分页结果
        return CommentPageDto.builder()
                .comments(commentDtos)
                .currentPage(page)
                .pageSize(size)
                .totalItems(total)
                .totalPages(totalPages)
                .build();
    }
    
    @Override
    public List<CommentDto> getAllCommentsByItem(Long itemId, String itemType) {
        log.debug("查询物品全部留言, 物品ID: {}, 物品类型: {}", itemId, itemType);
        
        // 验证物品是否存在
        validateItemExists(itemId, itemType);
        
        // 查询留言
        List<Comment> comments = commentRepository.findByItemIdAndType(itemId, itemType);
        
        // 转换为DTO
        return comments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public Optional<CommentDto> getCommentById(Long id) {
        log.debug("根据ID查询留言: {}", id);
        
        return commentRepository.findById(id)
                .map(this::convertToDto);
    }
    
    @Override
    @Transactional
    public boolean deleteComment(Long id, Long userId) {
        log.debug("删除留言, ID: {}, 用户ID: {}", id, userId);
        
        // 查询留言
        Optional<Comment> optionalComment = commentRepository.findById(id);
        if (!optionalComment.isPresent()) {
            log.warn("留言不存在: {}", id);
            return false;
        }
        
        Comment comment = optionalComment.get();
        
        // 检查是否为留言作者
        if (!comment.getUserId().equals(userId)) {
            log.warn("用户无权删除留言, 用户ID: {}, 留言作者ID: {}", userId, comment.getUserId());
            return false;
        }
        
        // 删除留言
        commentRepository.deleteById(id);
        log.info("留言删除成功: {}", id);
        
        return true;
    }
    
    @Override
    public List<CommentDto> getCommentsByUser(Long userId) {
        log.debug("查询用户留言列表, 用户ID: {}", userId);
        
        List<Comment> comments = commentRepository.findByUserId(userId);
        
        return comments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public CommentDto convertToDto(Comment comment) {
        return CommentDto.builder()
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