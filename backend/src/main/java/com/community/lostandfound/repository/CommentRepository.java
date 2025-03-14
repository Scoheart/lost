package com.community.lostandfound.repository;

import com.community.lostandfound.entity.Comment;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

@Mapper
public interface CommentRepository {
    
    @Insert("INSERT INTO comments(content, user_id, item_id, item_type, created_at) " +
            "VALUES(#{content}, #{userId}, #{itemId}, #{itemType}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void save(Comment comment);
    
    @Select("SELECT c.*, u.username " +
            "FROM comments c " +
            "JOIN users u ON c.user_id = u.id " +
            "WHERE c.item_id = #{itemId} AND c.item_type = #{itemType} " +
            "ORDER BY c.created_at DESC")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "content", column = "content"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "itemId", column = "item_id"),
        @Result(property = "itemType", column = "item_type"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "username", column = "username")
    })
    List<Comment> findByItemIdAndItemType(@Param("itemId") Long itemId, @Param("itemType") String itemType);
    
    @Select("SELECT c.*, u.username " +
            "FROM comments c " +
            "JOIN users u ON c.user_id = u.id " +
            "WHERE c.id = #{id}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "content", column = "content"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "itemId", column = "item_id"),
        @Result(property = "itemType", column = "item_type"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "username", column = "username")
    })
    Optional<Comment> findById(@Param("id") Long id);
    
    @Delete("DELETE FROM comments WHERE id = #{id}")
    void deleteById(@Param("id") Long id);
    
    @Delete("DELETE FROM comments WHERE item_id = #{itemId} AND item_type = #{itemType}")
    void deleteByItemIdAndItemType(@Param("itemId") Long itemId, @Param("itemType") String itemType);
    
    @Select("SELECT COUNT(*) FROM comments WHERE item_id = #{itemId} AND item_type = #{itemType}")
    int countByItemIdAndItemType(@Param("itemId") Long itemId, @Param("itemType") String itemType);
} 