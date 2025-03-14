package com.community.lostandfound.repository;

import com.community.lostandfound.entity.LostItem;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

@Mapper
public interface LostItemRepository {
    
    @Insert("INSERT INTO lost_items(title, description, lost_date, lost_location, category, images, " +
            "reward, contact_info, status, user_id, created_at, updated_at) " +
            "VALUES(#{title}, #{description}, #{lostDate}, #{lostLocation}, #{category}, #{images}, " +
            "#{reward}, #{contactInfo}, #{status}, #{userId}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void save(LostItem lostItem);
    
    @Select("SELECT li.*, u.username " +
            "FROM lost_items li " +
            "JOIN users u ON li.user_id = u.id " +
            "WHERE li.id = #{id}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "title", column = "title"),
        @Result(property = "description", column = "description"),
        @Result(property = "lostDate", column = "lost_date"),
        @Result(property = "lostLocation", column = "lost_location"),
        @Result(property = "category", column = "category"),
        @Result(property = "images", column = "images"),
        @Result(property = "reward", column = "reward"),
        @Result(property = "contactInfo", column = "contact_info"),
        @Result(property = "status", column = "status"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "username", column = "username")
    })
    Optional<LostItem> findById(@Param("id") Long id);
    
    @Select({
        "<script>",
        "SELECT li.*, u.username FROM lost_items li ",
        "JOIN users u ON li.user_id = u.id ",
        "<where>",
        "  <if test='category != null'>",
        "    AND li.category = #{category}",
        "  </if>",
        "  <if test='status != null'>",
        "    AND li.status = #{status}",
        "  </if>",
        "  <if test='keyword != null'>",
        "    AND (li.title LIKE CONCAT('%', #{keyword}, '%') ",
        "      OR li.description LIKE CONCAT('%', #{keyword}, '%') ",
        "      OR li.lost_location LIKE CONCAT('%', #{keyword}, '%'))",
        "  </if>",
        "</where>",
        "ORDER BY li.created_at DESC ",
        "LIMIT #{offset}, #{limit}",
        "</script>"
    })
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "title", column = "title"),
        @Result(property = "description", column = "description"),
        @Result(property = "lostDate", column = "lost_date"),
        @Result(property = "lostLocation", column = "lost_location"),
        @Result(property = "category", column = "category"),
        @Result(property = "images", column = "images"),
        @Result(property = "reward", column = "reward"),
        @Result(property = "contactInfo", column = "contact_info"),
        @Result(property = "status", column = "status"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "username", column = "username")
    })
    List<LostItem> findAll(
            @Param("category") String category,
            @Param("status") String status,
            @Param("keyword") String keyword,
            @Param("offset") int offset,
            @Param("limit") int limit);
    
    @Select({
        "<script>",
        "SELECT COUNT(*) FROM lost_items li ",
        "<where>",
        "  <if test='category != null'>",
        "    AND li.category = #{category}",
        "  </if>",
        "  <if test='status != null'>",
        "    AND li.status = #{status}",
        "  </if>",
        "  <if test='keyword != null'>",
        "    AND (li.title LIKE CONCAT('%', #{keyword}, '%') ",
        "      OR li.description LIKE CONCAT('%', #{keyword}, '%') ",
        "      OR li.lost_location LIKE CONCAT('%', #{keyword}, '%'))",
        "  </if>",
        "</where>",
        "</script>"
    })
    int countAll(
            @Param("category") String category,
            @Param("status") String status,
            @Param("keyword") String keyword);
    
    @Select("SELECT li.*, u.username " +
            "FROM lost_items li " +
            "JOIN users u ON li.user_id = u.id " +
            "WHERE li.user_id = #{userId} " +
            "ORDER BY li.created_at DESC")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "title", column = "title"),
        @Result(property = "description", column = "description"),
        @Result(property = "lostDate", column = "lost_date"),
        @Result(property = "lostLocation", column = "lost_location"),
        @Result(property = "category", column = "category"),
        @Result(property = "images", column = "images"),
        @Result(property = "reward", column = "reward"),
        @Result(property = "contactInfo", column = "contact_info"),
        @Result(property = "status", column = "status"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "username", column = "username")
    })
    List<LostItem> findByUserId(@Param("userId") Long userId);
    
    @Update("UPDATE lost_items SET title = #{title}, description = #{description}, " +
            "lost_date = #{lostDate}, lost_location = #{lostLocation}, category = #{category}, " +
            "images = #{images}, reward = #{reward}, contact_info = #{contactInfo}, " +
            "status = #{status}, updated_at = #{updatedAt} " +
            "WHERE id = #{id}")
    void update(LostItem lostItem);
    
    @Delete("DELETE FROM lost_items WHERE id = #{id}")
    void deleteById(@Param("id") Long id);
    
    @Update("UPDATE lost_items SET status = #{status}, updated_at = #{updatedAt} WHERE id = #{id}")
    void updateStatus(@Param("id") Long id, @Param("status") String status, @Param("updatedAt") String updatedAt);
} 