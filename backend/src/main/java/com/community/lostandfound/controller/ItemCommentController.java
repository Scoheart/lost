package com.community.lostandfound.controller;

import com.community.lostandfound.dto.comment.CreateItemCommentRequest;
import com.community.lostandfound.dto.comment.ItemCommentDto;
import com.community.lostandfound.dto.comment.ItemCommentPageDto;
import com.community.lostandfound.dto.common.ApiResponse;
import com.community.lostandfound.security.CurrentUser;
import com.community.lostandfound.security.UserDetailsImpl;
import com.community.lostandfound.service.ItemCommentService;
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
 * 物品评论控制器
 */
@Slf4j
@RestController
@RequestMapping("/item-comments")
@RequiredArgsConstructor
public class ItemCommentController {
    
    private final ItemCommentService itemCommentService;
    
    /**
     * 创建物品评论
     *
     * @param request     创建评论请求
     * @param currentUser 当前用户
     * @return 创建的评论
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ItemCommentDto>> createComment(
            @Valid @RequestBody CreateItemCommentRequest request,
            @CurrentUser UserDetailsImpl currentUser) {
        
        log.info("创建物品评论: 物品ID={}, 物品类型={}", request.getItemId(), request.getItemType());
        
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("未授权操作，请先登录"));
        }
        
        // 禁止对失物招领进行评论
        if ("found".equalsIgnoreCase(request.getItemType())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.fail("失物招领不支持评论功能"));
        }
        
        ItemCommentDto comment = itemCommentService.createComment(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("评论创建成功", comment));
    }
    
    /**
     * 获取物品的评论列表（分页）
     *
     * @param itemId   物品ID
     * @param itemType 物品类型
     * @param page     页码
     * @param size     每页条数
     * @return 评论分页列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse<ItemCommentPageDto>> getCommentsByItem(
            @RequestParam Long itemId,
            @RequestParam String itemType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("获取物品评论列表: 物品ID={}, 物品类型={}, 页码={}, 每页条数={}", itemId, itemType, page, size);
        
        // 禁止查询失物招领的评论
        if ("found".equalsIgnoreCase(itemType)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.fail("失物招领不支持评论功能"));
        }
        
        ItemCommentPageDto pageDto = itemCommentService.getCommentsByItem(itemId, itemType, page, size);
        return ResponseEntity.ok(ApiResponse.success("获取评论列表成功", pageDto));
    }
    
    /**
     * 获取物品的所有评论（不分页）
     *
     * @param itemId   物品ID
     * @param itemType 物品类型
     * @return 评论列表
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<ItemCommentDto>>> getAllCommentsByItem(
            @RequestParam Long itemId,
            @RequestParam String itemType) {
        
        log.info("获取物品所有评论: 物品ID={}, 物品类型={}", itemId, itemType);
        
        // 禁止查询失物招领的评论
        if ("found".equalsIgnoreCase(itemType)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.fail("失物招领不支持评论功能"));
        }
        
        List<ItemCommentDto> comments = itemCommentService.getAllCommentsByItem(itemId, itemType);
        return ResponseEntity.ok(ApiResponse.success("获取评论列表成功", comments));
    }
    
    /**
     * 根据ID获取评论
     *
     * @param id 评论ID
     * @return 评论对象
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemCommentDto>> getCommentById(@PathVariable Long id) {
        
        log.info("获取评论详情: ID={}", id);
        
        Optional<ItemCommentDto> comment = itemCommentService.getCommentById(id);
        
        if (comment.isPresent() && "found".equalsIgnoreCase(comment.get().getItemType())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.fail("失物招领不支持评论功能"));
        }
        
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
        
        // 检查评论是否存在以及类型是否为found
        Optional<ItemCommentDto> comment = itemCommentService.getCommentById(id);
        if (comment.isPresent() && "found".equalsIgnoreCase(comment.get().getItemType())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.fail("失物招领不支持评论功能"));
        }
        
        boolean success = itemCommentService.deleteComment(id, currentUser.getId());
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
    public ResponseEntity<ApiResponse<List<ItemCommentDto>>> getCommentsByUser(@PathVariable Long userId) {
        
        log.info("获取用户的物品评论列表: 用户ID={}", userId);
        
        List<ItemCommentDto> comments = itemCommentService.getCommentsByUser(userId);
        // 过滤掉失物招领的评论
        comments = comments.stream()
                .filter(comment -> !"found".equalsIgnoreCase(comment.getItemType()))
                .toList();
        
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
    public ResponseEntity<ApiResponse<List<ItemCommentDto>>> getMyComments(
            @CurrentUser UserDetailsImpl currentUser) {
        
        log.info("获取当前用户的物品评论列表: 用户ID={}", currentUser.getId());
        
        List<ItemCommentDto> comments = itemCommentService.getCommentsByUser(currentUser.getId());
        // 过滤掉失物招领的评论
        comments = comments.stream()
                .filter(comment -> !"found".equalsIgnoreCase(comment.getItemType()))
                .toList();
        
        return ResponseEntity.ok(ApiResponse.success("获取我的评论列表成功", comments));
    }
} 