package com.community.lostandfound.service.impl;

import com.community.lostandfound.dto.comment.PostCommentDto;
import com.community.lostandfound.dto.comment.PostCommentPageDto;
import com.community.lostandfound.dto.comment.CreatePostCommentRequest;
import com.community.lostandfound.entity.PostComment;
import com.community.lostandfound.exception.ResourceNotFoundException;
import com.community.lostandfound.repository.PostCommentRepository;
import com.community.lostandfound.service.PostCommentService;
import com.community.lostandfound.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 帖子评论服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostCommentServiceImpl implements PostCommentService {
    
    private final PostCommentRepository postCommentRepository;
    private final PostService postService;
    
    @Override
    @Transactional
    public PostCommentDto createComment(CreatePostCommentRequest request, Long userId) {
        log.debug("创建帖子评论: {}", request);
        
        // 验证帖子是否存在
        validatePostExists(request.getPostId());
        
        // 创建并保存评论
        PostComment comment = new PostComment();
        comment.setContent(request.getContent());
        comment.setPostId(request.getPostId());
        comment.setUserId(userId);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
        
        postCommentRepository.save(comment);
        log.info("帖子评论创建成功: {}", comment.getId());
        
        return convertToDto(comment);
    }
    
    @Override
    public PostCommentPageDto getCommentsByPost(Long postId, int page, int size) {
        log.debug("查询帖子评论列表, 帖子ID: {}, 页码: {}, 每页条数: {}", 
                postId, page, size);
                
        // 验证帖子是否存在
        validatePostExists(postId);
        
        // 计算分页参数
        int offset = (page - 1) * size;
        
        // 查询评论
        List<PostComment> comments = postCommentRepository.findByPostIdWithPagination(postId, offset, size);
        int total = postCommentRepository.countByPostId(postId);
        
        // 计算总页数
        int totalPages = (total + size - 1) / size;
        
        // 转换为DTO
        List<PostCommentDto> commentDtos = comments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        // 构建分页结果
        return PostCommentPageDto.builder()
                .comments(commentDtos)
                .currentPage(page)
                .pageSize(size)
                .totalItems(total)
                .totalPages(totalPages)
                .build();
    }
    
    @Override
    public List<PostCommentDto> getAllCommentsByPost(Long postId) {
        log.debug("查询帖子全部评论, 帖子ID: {}", postId);
        
        // 验证帖子是否存在
        validatePostExists(postId);
        
        // 查询评论
        List<PostComment> comments = postCommentRepository.findByPostId(postId);
        
        // 转换为DTO
        return comments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public Optional<PostCommentDto> getCommentById(Long id) {
        log.debug("根据ID查询帖子评论: {}", id);
        
        return postCommentRepository.findById(id)
                .map(this::convertToDto);
    }
    
    @Override
    @Transactional
    public boolean deleteComment(Long id, Long userId) {
        log.debug("删除帖子评论, ID: {}, 用户ID: {}", id, userId);
        
        // 查询评论
        Optional<PostComment> optionalComment = postCommentRepository.findById(id);
        if (optionalComment.isEmpty()) {
            log.warn("帖子评论不存在: {}", id);
            return false;
        }
        
        PostComment comment = optionalComment.get();
        
        // 检查是否为评论作者
        if (!comment.getUserId().equals(userId)) {
            log.warn("用户无权删除帖子评论, 用户ID: {}, 评论作者ID: {}", userId, comment.getUserId());
            return false;
        }
        
        // 删除评论
        postCommentRepository.deleteById(id);
        log.info("帖子评论删除成功: {}", id);
        
        return true;
    }
    
    @Override
    public List<PostCommentDto> getCommentsByUser(Long userId) {
        log.debug("查询用户帖子评论列表, 用户ID: {}", userId);
        
        List<PostComment> comments = postCommentRepository.findByUserId(userId);
        
        return comments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public PostCommentDto convertToDto(PostComment comment) {
        return PostCommentDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .postId(comment.getPostId())
                .userId(comment.getUserId())
                .username(comment.getUsername())
                .userAvatar(comment.getUserAvatar())
                .createdAt(comment.getCreatedAt())
                .build();
    }
    
    /**
     * 验证帖子是否存在
     *
     * @param postId 帖子ID
     * @throws ResourceNotFoundException 如果帖子不存在
     */
    private void validatePostExists(Long postId) {
        try {
            postService.getPostById(postId);
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException("论坛帖子不存在: ID = " + postId);
        }
    }
} 