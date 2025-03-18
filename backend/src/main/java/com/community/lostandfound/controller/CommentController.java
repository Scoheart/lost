package com.community.lostandfound.controller;

import com.community.lostandfound.dto.comment.CommentDto;
import com.community.lostandfound.dto.comment.CommentPageDto;
import com.community.lostandfound.dto.comment.CreateCommentRequest;
import com.community.lostandfound.dto.common.ApiResponse;
import com.community.lostandfound.security.CurrentUser;
import com.community.lostandfound.security.UserDetailsImpl;
import com.community.lostandfound.service.CommentService;
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
 * 留言控制器
 * @deprecated 已被拆分为ItemCommentController和PostCommentController，保留此控制器是为了保持兼容性
 * 新代码请使用：
 * - 物品评论：/api/item-comments (ItemCommentController)
 * - 帖子评论：/api/post-comments (PostCommentController)
 */
@Slf4j
@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
@Deprecated
public class CommentController {
    
    private final CommentService commentService;
    
    /**
     * 创建留言
     *
     * @param request     创建留言请求
     * @param currentUser 当前用户
     * @return 创建的留言
     * @deprecated 请使用新的端点：
     *  - 物品评论：POST /api/item-comments
     *  - 帖子评论：POST /api/post-comments
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Deprecated
    public ResponseEntity<ApiResponse<CommentDto>> createComment(
            @Valid @RequestBody CreateCommentRequest request,
            @CurrentUser UserDetailsImpl currentUser) {
        
        log.info("创建留言: 物品ID={}, 物品类型={}", request.getItemId(), request.getItemType());
        
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("未授权操作，请先登录"));
        }
        
        CommentDto comment = commentService.createComment(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("留言创建成功", comment));
    }
    
    /**
     * 获取物品的留言列表（分页）
     *
     * @param itemId   物品ID
     * @param itemType 物品类型
     * @param page     页码
     * @param size     每页条数
     * @return 留言分页列表
     * @deprecated 请使用新的端点：
     *  - 物品评论：GET /api/item-comments
     *  - 帖子评论：GET /api/post-comments
     */
    @GetMapping
    @Deprecated
    public ResponseEntity<ApiResponse<CommentPageDto>> getCommentsByItem(
            @RequestParam Long itemId,
            @RequestParam String itemType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("查询物品留言列表: 物品ID={}, 物品类型={}, 页码={}, 每页条数={}", 
                itemId, itemType, page, size);
        
        CommentPageDto comments = commentService.getCommentsByItem(itemId, itemType, page, size);
        return ResponseEntity.ok(ApiResponse.success("查询留言列表成功", comments));
    }
    
    /**
     * 获取物品的所有留言（不分页）
     *
     * @param itemId   物品ID
     * @param itemType 物品类型
     * @return 留言列表
     * @deprecated 请使用新的端点：
     *  - 物品评论：GET /api/item-comments/all
     *  - 帖子评论：GET /api/post-comments/all
     */
    @GetMapping("/all")
    @Deprecated
    public ResponseEntity<ApiResponse<List<CommentDto>>> getAllCommentsByItem(
            @RequestParam Long itemId,
            @RequestParam String itemType) {
        
        log.info("查询物品所有留言: 物品ID={}, 物品类型={}", itemId, itemType);
        
        List<CommentDto> comments = commentService.getAllCommentsByItem(itemId, itemType);
        return ResponseEntity.ok(ApiResponse.success("查询留言列表成功", comments));
    }
    
    /**
     * 根据ID获取留言
     *
     * @param id 留言ID
     * @return 留言
     * @deprecated 请使用新的端点：
     *  - 物品评论：GET /api/item-comments/{id}
     *  - 帖子评论：GET /api/post-comments/{id}
     */
    @GetMapping("/{id}")
    @Deprecated
    public ResponseEntity<ApiResponse<CommentDto>> getCommentById(@PathVariable Long id) {
        log.info("根据ID查询留言: {}", id);
        
        Optional<CommentDto> comment = commentService.getCommentById(id);
        if (comment.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success("查询留言成功", comment.get()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail("留言不存在"));
        }
    }
    
    /**
     * 删除留言
     *
     * @param id          留言ID
     * @param currentUser 当前用户
     * @return 删除结果
     * @deprecated 请使用新的端点：
     *  - 物品评论：DELETE /api/item-comments/{id}
     *  - 帖子评论：DELETE /api/post-comments/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Deprecated
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long id,
            @CurrentUser UserDetailsImpl currentUser) {
        
        log.info("删除留言: {}", id);
        
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("未授权操作，请先登录"));
        }
        
        boolean success = commentService.deleteComment(id, currentUser.getId());
        if (success) {
            return ResponseEntity.ok(ApiResponse.success("留言删除成功", null));
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.fail("留言删除失败，可能不存在或无权删除"));
        }
    }
    
    /**
     * 获取用户的留言列表
     *
     * @param currentUser 当前用户
     * @return 留言列表
     * @deprecated 请使用新的端点：
     *  - 物品评论：GET /api/item-comments/me
     *  - 帖子评论：GET /api/post-comments/me
     */
    @GetMapping("/my-comments")
    @PreAuthorize("isAuthenticated()")
    @Deprecated
    public ResponseEntity<ApiResponse<List<CommentDto>>> getMyComments(
            @CurrentUser UserDetailsImpl currentUser) {
        
        log.info("查询当前用户的留言列表");
        
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("未授权操作，请先登录"));
        }
        
        List<CommentDto> comments = commentService.getCommentsByUser(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("查询留言列表成功", comments));
    }
} 