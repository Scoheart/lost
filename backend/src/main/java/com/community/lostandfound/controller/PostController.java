package com.community.lostandfound.controller;

import com.community.lostandfound.dto.common.ApiResponse;
import com.community.lostandfound.dto.common.PagedResponse;
import com.community.lostandfound.dto.post.CreatePostRequest;
import com.community.lostandfound.dto.post.PostResponse;
import com.community.lostandfound.dto.post.UpdatePostRequest;
import com.community.lostandfound.exception.ResourceNotFoundException;
import com.community.lostandfound.exception.UnauthorizedException;
import com.community.lostandfound.security.CurrentUser;
import com.community.lostandfound.security.UserDetailsImpl;
import com.community.lostandfound.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * 社区论坛帖子控制器
 */
@Slf4j
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * 创建帖子
     *
     * @param request     创建帖子请求
     * @param userDetails 当前用户
     * @return 创建的帖子
     */
    @PostMapping
    @PreAuthorize("hasRole('RESIDENT') or hasRole('ADMIN') or hasRole('SYSADMIN')")
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @Valid @RequestBody CreatePostRequest request,
            @CurrentUser UserDetailsImpl userDetails) {
        log.info("创建帖子: {}", request);
        try {
            PostResponse post = postService.createPost(userDetails.getId(), request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("帖子创建成功", post));
        } catch (Exception e) {
            log.error("创建帖子失败: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("创建帖子失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 更新帖子
     *
     * @param id          帖子ID
     * @param request     更新帖子请求
     * @param userDetails 当前用户
     * @return 更新后的帖子
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RESIDENT') or hasRole('ADMIN') or hasRole('SYSADMIN')")
    public ResponseEntity<ApiResponse<PostResponse>> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePostRequest request,
            @CurrentUser UserDetailsImpl userDetails) {
        log.info("更新帖子: {}", id);
        try {
            PostResponse post = postService.updatePost(id, userDetails.getId(), request);
            return ResponseEntity.ok(ApiResponse.success("帖子更新成功", post));
        } catch (ResourceNotFoundException e) {
            log.warn("更新帖子失败 - 资源不存在: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.NOT_FOUND));
        } catch (UnauthorizedException e) {
            log.warn("更新帖子失败 - 未授权: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.FORBIDDEN));
        } catch (Exception e) {
            log.error("更新帖子失败: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("更新帖子失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 删除帖子
     *
     * @param id          帖子ID
     * @param userDetails 当前用户
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('RESIDENT') or hasRole('ADMIN') or hasRole('SYSADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable Long id,
            @CurrentUser UserDetailsImpl userDetails) {
        log.info("删除帖子: {}", id);
        try {
            postService.deletePost(id, userDetails.getId());
            return ResponseEntity.ok(ApiResponse.success("帖子删除成功"));
        } catch (ResourceNotFoundException e) {
            log.warn("删除帖子失败 - 资源不存在: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.NOT_FOUND));
        } catch (UnauthorizedException e) {
            log.warn("删除帖子失败 - 未授权: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.FORBIDDEN));
        } catch (Exception e) {
            log.error("删除帖子失败: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("删除帖子失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 获取帖子详情
     *
     * @param id 帖子ID
     * @return 帖子详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostResponse>> getPostById(@PathVariable Long id) {
        log.info("获取帖子详情: {}", id);
        try {
            PostResponse post = postService.getPostById(id);
            return ResponseEntity.ok(ApiResponse.success("获取帖子成功", post));
        } catch (ResourceNotFoundException e) {
            log.warn("获取帖子详情失败 - 资源不存在: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            log.error("获取帖子详情失败: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取帖子详情失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 获取所有帖子（分页）
     *
     * @param page 页码 (从0开始)
     * @param size 每页条数
     * @return 帖子列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<PostResponse>>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("获取所有帖子: page={}, size={}", page, size);
        try {
            PagedResponse<PostResponse> posts = postService.getAllPosts(page, size);
            return ResponseEntity.ok(ApiResponse.success("获取帖子列表成功", posts));
        } catch (Exception e) {
            log.error("获取所有帖子失败: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取所有帖子失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 获取用户的帖子（分页）
     *
     * @param userId 用户ID
     * @param page   页码 (从0开始)
     * @param size   每页条数
     * @return 帖子列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<PagedResponse<PostResponse>>> getUserPosts(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("获取用户帖子: userId={}, page={}, size={}", userId, page, size);
        try {
            PagedResponse<PostResponse> posts = postService.getUserPosts(userId, page, size);
            return ResponseEntity.ok(ApiResponse.success("获取用户帖子成功", posts));
        } catch (Exception e) {
            log.error("获取用户帖子失败: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取用户帖子失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 获取当前用户的帖子（分页）
     *
     * @param userDetails 当前用户
     * @param page        页码 (从0开始)
     * @param size        每页条数
     * @return 帖子列表
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('RESIDENT') or hasRole('ADMIN') or hasRole('SYSADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<PostResponse>>> getMyPosts(
            @CurrentUser UserDetailsImpl userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("获取我的帖子: userId={}, page={}, size={}", userDetails.getId(), page, size);
        try {
            PagedResponse<PostResponse> posts = postService.getUserPosts(userDetails.getId(), page, size);
            return ResponseEntity.ok(ApiResponse.success("获取我的帖子成功", posts));
        } catch (Exception e) {
            log.error("获取我的帖子失败: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取我的帖子失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 搜索帖子
     *
     * @param keyword 关键词
     * @param page    页码 (从0开始)
     * @param size    每页条数
     * @return 帖子列表
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponse<PostResponse>>> searchPosts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("搜索帖子: keyword={}, page={}, size={}", keyword, page, size);
        try {
            PagedResponse<PostResponse> posts = postService.searchPosts(keyword, page, size);
            return ResponseEntity.ok(ApiResponse.success("搜索帖子成功", posts));
        } catch (Exception e) {
            log.error("搜索帖子失败: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("搜索帖子失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }
} 