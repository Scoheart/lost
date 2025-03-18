package com.community.lostandfound.repository;

import com.community.lostandfound.entity.PostComment;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * 帖子评论数据访问接口
 */
@Mapper
public interface PostCommentRepository {
    
    /**
     * 保存评论
     *
     * @param comment 评论对象
     */
    @Insert("INSERT INTO comments (content, item_id, item_type, user_id, created_at, updated_at) " +
            "VALUES (#{content}, #{postId}, 'post', #{userId}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void save(PostComment comment);
    
    /**
     * 根据ID查询评论
     *
     * @param id 评论ID
     * @return 评论对象
     */
    @Select("SELECT c.*, u.username, u.avatar as user_avatar " +
            "FROM comments c " +
            "JOIN users u ON c.user_id = u.id " +
            "WHERE c.id = #{id} AND c.item_type = 'post'")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "content", column = "content"),
        @Result(property = "postId", column = "item_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "username", column = "username"),
        @Result(property = "userAvatar", column = "user_avatar")
    })
    Optional<PostComment> findById(Long id);
    
    /**
     * 查询帖子的所有评论
     *
     * @param postId 帖子ID
     * @return 评论列表
     */
    @Select("SELECT c.*, u.username, u.avatar as user_avatar " +
            "FROM comments c " +
            "JOIN users u ON c.user_id = u.id " +
            "WHERE c.item_id = #{postId} AND c.item_type = 'post' " +
            "ORDER BY c.created_at DESC")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "content", column = "content"),
        @Result(property = "postId", column = "item_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "username", column = "username"),
        @Result(property = "userAvatar", column = "user_avatar")
    })
    List<PostComment> findByPostId(Long postId);
    
    /**
     * 查询帖子的所有评论（分页）
     *
     * @param postId 帖子ID
     * @param offset 偏移量
     * @param limit  每页数量
     * @return 评论列表
     */
    @Select("SELECT c.*, u.username, u.avatar as user_avatar " +
            "FROM comments c " +
            "JOIN users u ON c.user_id = u.id " +
            "WHERE c.item_id = #{postId} AND c.item_type = 'post' " +
            "ORDER BY c.created_at DESC " +
            "LIMIT #{offset}, #{limit}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "content", column = "content"),
        @Result(property = "postId", column = "item_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "username", column = "username"),
        @Result(property = "userAvatar", column = "user_avatar")
    })
    List<PostComment> findByPostIdWithPagination(
            @Param("postId") Long postId, 
            @Param("offset") int offset,
            @Param("limit") int limit);
    
    /**
     * 统计帖子的评论数量
     *
     * @param postId 帖子ID
     * @return 评论数量
     */
    @Select("SELECT COUNT(*) FROM comments WHERE item_id = #{postId} AND item_type = 'post'")
    int countByPostId(Long postId);
    
    /**
     * 删除评论
     *
     * @param id 评论ID
     */
    @Delete("DELETE FROM comments WHERE id = #{id}")
    void deleteById(Long id);
    
    /**
     * 查询用户的所有帖子评论
     *
     * @param userId 用户ID
     * @return 评论列表
     */
    @Select("SELECT c.*, u.username, u.avatar as user_avatar " +
            "FROM comments c " +
            "JOIN users u ON c.user_id = u.id " +
            "WHERE c.user_id = #{userId} AND c.item_type = 'post' " +
            "ORDER BY c.created_at DESC")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "content", column = "content"),
        @Result(property = "postId", column = "item_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "username", column = "username"),
        @Result(property = "userAvatar", column = "user_avatar")
    })
    List<PostComment> findByUserId(Long userId);
    
    /**
     * 删除指定帖子的所有评论
     *
     * @param postId 帖子ID
     */
    @Delete("DELETE FROM comments WHERE item_id = #{postId} AND item_type = 'post'")
    void deleteByPostId(Long postId);
} 