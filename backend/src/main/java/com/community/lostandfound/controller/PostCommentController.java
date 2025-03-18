package com.community.lostandfound.controller;

import com.community.lostandfound.dto.comment.CreatePostCommentRequest;
import com.community.lostandfound.dto.comment.PostCommentDto;
import com.community.lostandfound.dto.comment.PostCommentPageDto;
import com.community.lostandfound.dto.common.ApiResponse;
import com.community.lostandfound.security.CurrentUser;
import com.community.lostandfound.security.UserDetailsImpl;
import com.community.lostandfound.service.PostCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 帖子评论控制器
 */
@Slf4j
@RestController
@RequestMapping("/post-comments")
@RequiredArgsConstructor
public class PostCommentController {
    
    private final PostCommentService postCommentService;
    
    /**
     * 创建帖子评论
     *
     * @param request     创建评论请求
     * @param currentUser 当前用户
     * @return 创建的评论
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PostCommentDto>> createComment(
            @Valid @RequestBody CreatePostCommentRequest request,
            @CurrentUser UserDetailsImpl currentUser) {
        
        log.info("创建帖子评论: 帖子ID={}", request.getPostId());
        
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("未授权操作，请先登录"));
        }
        
        PostCommentDto comment = postCommentService.createComment(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("评论创建成功", comment));
    }
    
    /**
     * 获取帖子的评论列表（分页）
     *
     * @param postId 帖子ID
     * @param page   页码
     * @param size   每页条数
     * @return 评论分页列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PostCommentPageDto>> getCommentsByPost(
            @RequestParam Long postId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("获取帖子评论列表: 帖子ID={}, 页码={}, 每页条数={}", postId, page, size);
        
        PostCommentPageDto pageDto = postCommentService.getCommentsByPost(postId, page, size);
        return ResponseEntity.ok(ApiResponse.success("获取评论列表成功", pageDto));
    }
    
    /**
     * 获取帖子的所有评论（不分页）
     *
     * @param postId 帖子ID
     * @return 评论列表
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<PostCommentDto>>> getAllCommentsByPost(
            @RequestParam Long postId) {
        
        log.info("获取帖子所有评论: 帖子ID={}", postId);
        
        List<PostCommentDto> comments = postCommentService.getAllCommentsByPost(postId);
        return ResponseEntity.ok(ApiResponse.success("获取评论列表成功", comments));
    }
    
    /**
     * 根据ID获取评论
     *
     * @param id 评论ID
     * @return 评论对象
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostCommentDto>> getCommentById(@PathVariable Long id) {
        
        log.info("获取评论详情: ID={}", id);
        
        Optional<PostCommentDto> comment = postCommentService.getCommentById(id);
        return comment.map(c -> ResponseEntity.ok(ApiResponse.success("获取评论成功", c)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.fail("评论不存在")));
    }
    
    /**
     * 删除评论
     *
     * @param id          评论ID
     * @param currentUser 当前用户
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long id,
            @CurrentUser UserDetailsImpl currentUser) {
        
        log.info("删除评论: ID={}, 用户ID={}", id, currentUser.getId());
        
        boolean success = postCommentService.deleteComment(id, currentUser.getId());
        if (success) {
            return ResponseEntity.ok(ApiResponse.success("评论删除成功"));
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.fail("删除失败，可能无权限操作或评论不存在"));
        }
    }
    
    /**
     * 获取用户的评论列表
     *
     * @param userId 用户ID
     * @return 评论列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<PostCommentDto>>> getCommentsByUser(@PathVariable Long userId) {
        
        log.info("获取用户的帖子评论列表: 用户ID={}", userId);
        
        List<PostCommentDto> comments = postCommentService.getCommentsByUser(userId);
        return ResponseEntity.ok(ApiResponse.success("获取用户评论列表成功", comments));
    }
    
    /**
     * 获取当前用户的评论列表
     *
     * @param currentUser 当前用户
     * @return 评论列表
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<PostCommentDto>>> getMyComments(
            @CurrentUser UserDetailsImpl currentUser) {
        
        log.info("获取当前用户的帖子评论列表: 用户ID={}", currentUser.getId());
        
        List<PostCommentDto> comments = postCommentService.getCommentsByUser(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("获取我的评论列表成功", comments));
    }
} 