package com.community.lostandfound.repository;

import com.community.lostandfound.entity.ItemComment;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * 物品评论数据访问接口
 */
@Mapper
public interface ItemCommentRepository {
    
    /**
     * 保存评论
     *
     * @param comment 评论对象
     */
    @Insert("INSERT INTO item_comments (content, item_id, item_type, user_id, created_at, updated_at) " +
            "VALUES (#{content}, #{itemId}, #{itemType}, #{userId}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void save(ItemComment comment);
    
    /**
     * 根据ID查询评论
     *
     * @param id 评论ID
     * @return 评论对象
     */
    @Select("SELECT c.*, u.username, u.avatar as user_avatar " +
            "FROM item_comments c " +
            "JOIN users u ON c.user_id = u.id " +
            "WHERE c.id = #{id} AND (c.item_type = 'lost' OR c.item_type = 'found')")
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
    Optional<ItemComment> findById(Long id);
    
    /**
     * 查询物品的所有评论
     *
     * @param itemId   物品ID
     * @param itemType 物品类型 (lost 或 found)
     * @return 评论列表
     */
    @Select("SELECT c.*, u.username, u.avatar as user_avatar " +
            "FROM item_comments c " +
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
    List<ItemComment> findByItemIdAndType(@Param("itemId") Long itemId, @Param("itemType") String itemType);
    
    /**
     * 查询物品的所有评论（分页）
     *
     * @param itemId   物品ID
     * @param itemType 物品类型 (lost 或 found)
     * @param offset   偏移量
     * @param limit    每页数量
     * @return 评论列表
     */
    @Select("SELECT c.*, u.username, u.avatar as user_avatar " +
            "FROM item_comments c " +
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
    List<ItemComment> findByItemIdAndTypeWithPagination(
            @Param("itemId") Long itemId, 
            @Param("itemType") String itemType,
            @Param("offset") int offset,
            @Param("limit") int limit);
    
    /**
     * 统计物品的评论数量
     *
     * @param itemId   物品ID
     * @param itemType 物品类型 (lost 或 found)
     * @return 评论数量
     */
    @Select("SELECT COUNT(*) FROM item_comments WHERE item_id = #{itemId} AND item_type = #{itemType}")
    int countByItemIdAndType(@Param("itemId") Long itemId, @Param("itemType") String itemType);
    
    /**
     * 删除评论
     *
     * @param id 评论ID
     */
    @Delete("DELETE FROM item_comments WHERE id = #{id}")
    void deleteById(Long id);
    
    /**
     * 查询用户的所有物品评论
     *
     * @param userId 用户ID
     * @return 评论列表
     */
    @Select("SELECT c.*, u.username, u.avatar as user_avatar " +
            "FROM item_comments c " +
            "JOIN users u ON c.user_id = u.id " +
            "WHERE c.user_id = #{userId} AND (c.item_type = 'lost' OR c.item_type = 'found') " +
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
    List<ItemComment> findByUserId(Long userId);
    
    /**
     * 删除指定物品的所有评论
     *
     * @param itemId   物品ID
     * @param itemType 物品类型 (lost 或 found)
     */
    @Delete("DELETE FROM item_comments WHERE item_id = #{itemId} AND item_type = #{itemType}")
    void deleteByItemIdAndItemType(@Param("itemId") Long itemId, @Param("itemType") String itemType);
} 