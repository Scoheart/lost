package com.community.lostandfound.repository;

import com.community.lostandfound.entity.Post;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * 论坛帖子仓库
 */
@Mapper
public interface PostRepository {
    /**
     * 根据ID查找帖子
     */
    @Select("SELECT * FROM posts WHERE id = #{id}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "title", column = "title"),
        @Result(property = "content", column = "content"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "username", column = "username"),
        @Result(property = "userAvatar", column = "user_avatar"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    Optional<Post> findById(@Param("id") Long id);
    
    /**
     * 保存帖子
     */
    @Insert("INSERT INTO posts(title, content, user_id, username, user_avatar, created_at, updated_at) " +
            "VALUES(#{title}, #{content}, #{userId}, #{username}, #{userAvatar}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void save(Post post);
    
    /**
     * 更新帖子
     */
    @Update("UPDATE posts SET title = #{title}, content = #{content}, updated_at = #{updatedAt} " +
            "WHERE id = #{id}")
    void update(Post post);
    
    /**
     *
     * 删除帖子
     */
    @Delete("DELETE FROM posts WHERE id = #{id}")
    void deleteById(@Param("id") Long id);
    
    /**
     * 根据用户ID查询帖子（分页）
     */
    @Select("SELECT * FROM posts WHERE user_id = #{userId} ORDER BY created_at DESC LIMIT #{offset}, #{limit}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "title", column = "title"),
        @Result(property = "content", column = "content"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "username", column = "username"),
        @Result(property = "userAvatar", column = "user_avatar"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    List<Post> findByUserId(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * 查询所有帖子，按创建时间倒序排序（分页）
     */
    @Select("SELECT * FROM posts ORDER BY created_at DESC LIMIT #{offset}, #{limit}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "title", column = "title"),
        @Result(property = "content", column = "content"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "username", column = "username"),
        @Result(property = "userAvatar", column = "user_avatar"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    List<Post> findAllByOrderByCreatedAtDesc(@Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * 根据标题或内容包含关键词查询帖子（分页）
     */
    @Select("SELECT * FROM posts WHERE title LIKE CONCAT('%', #{keyword}, '%') OR content LIKE CONCAT('%', #{keyword}, '%') " +
            "ORDER BY created_at DESC LIMIT #{offset}, #{limit}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "title", column = "title"),
        @Result(property = "content", column = "content"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "username", column = "username"),
        @Result(property = "userAvatar", column = "user_avatar"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    List<Post> findByTitleContainingOrContentContainingOrderByCreatedAtDesc(
            @Param("keyword") String keyword, @Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * 统计用户的帖子总数
     */
    @Select("SELECT COUNT(*) FROM posts WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Long userId);
    
    /**
     * 统计所有帖子总数
     */
    @Select("SELECT COUNT(*) FROM posts")
    int countAll();
    
    /**
     * 统计符合关键词的帖子总数
     */
    @Select("SELECT COUNT(*) FROM posts WHERE title LIKE CONCAT('%', #{keyword}, '%') OR content LIKE CONCAT('%', #{keyword}, '%')")
    int countByKeyword(@Param("keyword") String keyword);
} 