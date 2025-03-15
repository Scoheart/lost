package com.community.lostandfound.repository;

import com.community.lostandfound.entity.Comment;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * 留言数据访问接口
 */
@Mapper
public interface CommentRepository {
    
    /**
     * 保存留言
     *
     * @param comment 留言对象
     */
    @Insert("INSERT INTO comments (content, item_id, item_type, user_id, created_at, updated_at) " +
            "VALUES (#{content}, #{itemId}, #{itemType}, #{userId}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void save(Comment comment);
    
    /**
     * 根据ID查询留言
     *
     * @param id 留言ID
     * @return 留言对象
     */
    @Select("SELECT c.*, u.username, u.avatar as user_avatar " +
            "FROM comments c " +
            "JOIN users u ON c.user_id = u.id " +
            "WHERE c.id = #{id}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "content", column = "content"),
        @Result(property = "itemId", column = "item_id"),
        @Result(property = "itemType", column = "item_type"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "username", column = "username"),
        @Result(property = "userAvatar", column = "user_avatar")
    })
    Optional<Comment> findById(Long id);
    
    /**
     * 查询物品的所有留言
     *
     * @param itemId   物品ID
     * @param itemType 物品类型
     * @return 留言列表
     */
    @Select("SELECT c.*, u.username, u.avatar as user_avatar " +
            "FROM comments c " +
            "JOIN users u ON c.user_id = u.id " +
            "WHERE c.item_id = #{itemId} AND c.item_type = #{itemType} " +
            "ORDER BY c.created_at DESC")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "content", column = "content"),
        @Result(property = "itemId", column = "item_id"),
        @Result(property = "itemType", column = "item_type"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "username", column = "username"),
        @Result(property = "userAvatar", column = "user_avatar")
    })
    List<Comment> findByItemIdAndType(@Param("itemId") Long itemId, @Param("itemType") String itemType);
    
    /**
     * 查询物品的所有留言（分页）
     *
     * @param itemId   物品ID
     * @param itemType 物品类型
     * @param offset   偏移量
     * @param limit    每页数量
     * @return 留言列表
     */
    @Select("SELECT c.*, u.username, u.avatar as user_avatar " +
            "FROM comments c " +
            "JOIN users u ON c.user_id = u.id " +
            "WHERE c.item_id = #{itemId} AND c.item_type = #{itemType} " +
            "ORDER BY c.created_at DESC " +
            "LIMIT #{offset}, #{limit}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "content", column = "content"),
        @Result(property = "itemId", column = "item_id"),
        @Result(property = "itemType", column = "item_type"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "username", column = "username"),
        @Result(property = "userAvatar", column = "user_avatar")
    })
    List<Comment> findByItemIdAndTypeWithPagination(
            @Param("itemId") Long itemId, 
            @Param("itemType") String itemType,
            @Param("offset") int offset,
            @Param("limit") int limit);
    
    /**
     * 统计物品的留言数量
     *
     * @param itemId   物品ID
     * @param itemType 物品类型
     * @return 留言数量
     */
    @Select("SELECT COUNT(*) FROM comments WHERE item_id = #{itemId} AND item_type = #{itemType}")
    int countByItemIdAndType(@Param("itemId") Long itemId, @Param("itemType") String itemType);
    
    /**
     * 删除留言
     *
     * @param id 留言ID
     */
    @Delete("DELETE FROM comments WHERE id = #{id}")
    void deleteById(Long id);
    
    /**
     * 查询用户的所有留言
     *
     * @param userId 用户ID
     * @return 留言列表
     */
    @Select("SELECT c.*, u.username, u.avatar as user_avatar " +
            "FROM comments c " +
            "JOIN users u ON c.user_id = u.id " +
            "WHERE c.user_id = #{userId} " +
            "ORDER BY c.created_at DESC")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "content", column = "content"),
        @Result(property = "itemId", column = "item_id"),
        @Result(property = "itemType", column = "item_type"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "username", column = "username"),
        @Result(property = "userAvatar", column = "user_avatar")
    })
    List<Comment> findByUserId(Long userId);
} 