package com.community.lostandfound.service;

import com.community.lostandfound.dto.common.PagedResponse;
import com.community.lostandfound.dto.post.CreatePostRequest;
import com.community.lostandfound.dto.post.PostResponse;
import com.community.lostandfound.dto.post.UpdatePostRequest;
import com.community.lostandfound.entity.Post;

/**
 * 论坛帖子服务接口
 */
public interface PostService {
    /**
     * 创建一个新帖子
     * @param userId 用户ID
     * @param request 创建帖子请求
     * @return 创建的帖子响应
     */
    PostResponse createPost(Long userId, CreatePostRequest request);

    /**
     * 更新帖子
     * @param postId 帖子ID
     * @param userId 用户ID（用于验证权限）
     * @param request 更新帖子请求
     * @return 更新后的帖子响应
     */
    PostResponse updatePost(Long postId, Long userId, UpdatePostRequest request);

    /**
     * 删除帖子
     * @param postId 帖子ID
     * @param userId 用户ID（用于验证权限）
     */
    void deletePost(Long postId, Long userId);

    /**
     * 根据ID获取帖子详情
     * @param postId 帖子ID
     * @return 帖子响应
     */
    PostResponse getPostById(Long postId);

    /**
     * 分页获取所有帖子
     * @param page 页码
     * @param size 每页大小
     * @return 分页帖子响应
     */
    PagedResponse<PostResponse> getAllPosts(int page, int size);

    /**
     * 分页获取用户的帖子
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 分页帖子响应
     */
    PagedResponse<PostResponse> getUserPosts(Long userId, int page, int size);

    /**
     * 搜索帖子
     * @param keyword 关键词
     * @param page 页码
     * @param size 每页大小
     * @return 分页帖子响应
     */
    PagedResponse<PostResponse> searchPosts(String keyword, int page, int size);
} 