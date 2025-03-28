package com.community.lostandfound.service.impl;

import com.community.lostandfound.dto.common.PagedResponse;
import com.community.lostandfound.dto.post.CreatePostRequest;
import com.community.lostandfound.dto.post.PostResponse;
import com.community.lostandfound.dto.post.UpdatePostRequest;
import com.community.lostandfound.entity.Post;
import com.community.lostandfound.entity.User;
import com.community.lostandfound.exception.ResourceNotFoundException;
import com.community.lostandfound.exception.UnauthorizedException;
import com.community.lostandfound.repository.PostCommentRepository;
import com.community.lostandfound.repository.PostRepository;
import com.community.lostandfound.repository.UserRepository;
import com.community.lostandfound.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 论坛帖子服务实现
 */
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostCommentRepository postCommentRepository;

    @Override
    @Transactional
    public PostResponse createPost(Long userId, CreatePostRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .userId(userId)
                .username(user.getUsername())
                .userAvatar(user.getAvatar())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        postRepository.save(post);
        return convertToPostResponse(post, 0);
    }

    @Override
    @Transactional
    public PostResponse updatePost(Long postId, Long userId, UpdatePostRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("帖子不存在"));

        if (!post.getUserId().equals(userId)) {
            throw new UnauthorizedException("您无权更新此帖子");
        }

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setUpdatedAt(LocalDateTime.now());

        postRepository.update(post);
        int commentCount = postCommentRepository.countByPostId(postId);
        return convertToPostResponse(post, commentCount);
    }

    @Override
    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("帖子不存在"));

        if (!post.getUserId().equals(userId)) {
            throw new UnauthorizedException("您无权删除此帖子");
        }

        // 删除帖子相关的评论
        postCommentRepository.deleteByPostId(postId);
        // 删除帖子
        postRepository.deleteById(postId);
    }

    @Override
    public PostResponse getPostById(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("帖子不存在"));

        int commentCount = postCommentRepository.countByPostId(postId);
        return convertToPostResponse(post, commentCount);
    }

    @Override
    public PagedResponse<PostResponse> getAllPosts(int page, int size) {
        int offset = page * size;
        List<Post> posts = postRepository.findAllByOrderByCreatedAtDesc(offset, size);
        int total = postRepository.countAll();
        
        return createPagedResponse(posts, page, size, total);
    }

    @Override
    public PagedResponse<PostResponse> getUserPosts(Long userId, int page, int size) {
        int offset = page * size;
        List<Post> posts = postRepository.findByUserId(userId, offset, size);
        int total = postRepository.countByUserId(userId);
        
        return createPagedResponse(posts, page, size, total);
    }

    @Override
    public PagedResponse<PostResponse> searchPosts(String keyword, int page, int size) {
        int offset = page * size;
        List<Post> posts = postRepository.findByTitleContainingOrContentContainingOrderByCreatedAtDesc(
                keyword, offset, size);
        int total = postRepository.countByKeyword(keyword);
        
        return createPagedResponse(posts, page, size, total);
    }

    private PagedResponse<PostResponse> createPagedResponse(List<Post> posts, int page, int size, long total) {
        List<PostResponse> postResponses = posts.stream()
                .map(post -> {
                    int commentCount = postCommentRepository.countByPostId(post.getId());
                    return convertToPostResponse(post, commentCount);
                })
                .collect(Collectors.toList());

        return PagedResponse.of(
                postResponses,
                page,
                size,
                total
        );
    }

    private PostResponse convertToPostResponse(Post post, int commentCount) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .userId(post.getUserId())
                .username(post.getUsername())
                .userAvatar(post.getUserAvatar())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .commentCount(commentCount)
                .build();
    }
} 