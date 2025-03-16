package com.community.lostandfound.repository;

import com.community.lostandfound.entity.FoundItem;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

@Mapper
public interface FoundItemRepository {
    
    @Insert("INSERT INTO found_items(title, description, found_date, found_location, storage_location, category, images, " +
            "contact_info, claim_requirements, status, user_id, created_at, updated_at) " +
            "VALUES(#{title}, #{description}, #{foundDate}, #{foundLocation}, #{storageLocation}, #{category}, #{images}, " +
            "#{contactInfo}, #{claimRequirements}, #{status}, #{userId}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void save(FoundItem foundItem);
    
    @Select("SELECT fi.*, u.username " +
            "FROM found_items fi " +
            "JOIN users u ON fi.user_id = u.id " +
            "WHERE fi.id = #{id}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "title", column = "title"),
        @Result(property = "description", column = "description"),
        @Result(property = "foundDate", column = "found_date"),
        @Result(property = "foundLocation", column = "found_location"),
        @Result(property = "storageLocation", column = "storage_location"),
        @Result(property = "category", column = "category"),
        @Result(property = "images", column = "images"),
        @Result(property = "contactInfo", column = "contact_info"),
        @Result(property = "claimRequirements", column = "claim_requirements"),
        @Result(property = "status", column = "status"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "username", column = "username")
    })
    Optional<FoundItem> findById(@Param("id") Long id);
    
    @Select({
        "<script>",
        "SELECT fi.*, u.username FROM found_items fi ",
        "JOIN users u ON fi.user_id = u.id ",
        "<where>",
        "  <if test='category != null'>",
        "    AND fi.category = #{category}",
        "  </if>",
        "  <if test='status != null'>",
        "    AND fi.status = #{status}",
        "  </if>",
        "  <if test='keyword != null'>",
        "    AND (fi.title LIKE CONCAT('%', #{keyword}, '%') ",
        "      OR fi.description LIKE CONCAT('%', #{keyword}, '%') ",
        "      OR fi.found_location LIKE CONCAT('%', #{keyword}, '%'))",
        "  </if>",
        "</where>",
        "ORDER BY fi.created_at DESC ",
        "LIMIT #{offset}, #{limit}",
        "</script>"
    })
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "title", column = "title"),
        @Result(property = "description", column = "description"),
        @Result(property = "foundDate", column = "found_date"),
        @Result(property = "foundLocation", column = "found_location"),
        @Result(property = "storageLocation", column = "storage_location"),
        @Result(property = "category", column = "category"),
        @Result(property = "images", column = "images"),
        @Result(property = "contactInfo", column = "contact_info"),
        @Result(property = "claimRequirements", column = "claim_requirements"),
        @Result(property = "status", column = "status"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "username", column = "username")
    })
    List<FoundItem> findAll(
            @Param("category") String category,
            @Param("status") String status,
            @Param("keyword") String keyword,
            @Param("offset") int offset,
            @Param("limit") int limit);
    
    @Select({
        "<script>",
        "SELECT COUNT(*) FROM found_items fi ",
        "<where>",
        "  <if test='category != null'>",
        "    AND fi.category = #{category}",
        "  </if>",
        "  <if test='status != null'>",
        "    AND fi.status = #{status}",
        "  </if>",
        "  <if test='keyword != null'>",
        "    AND (fi.title LIKE CONCAT('%', #{keyword}, '%') ",
        "      OR fi.description LIKE CONCAT('%', #{keyword}, '%') ",
        "      OR fi.found_location LIKE CONCAT('%', #{keyword}, '%'))",
        "  </if>",
        "</where>",
        "</script>"
    })
    int countAll(
            @Param("category") String category,
            @Param("status") String status,
            @Param("keyword") String keyword);
    
    @Select("SELECT fi.*, u.username " +
            "FROM found_items fi " +
            "JOIN users u ON fi.user_id = u.id " +
            "WHERE fi.user_id = #{userId} " +
            "ORDER BY fi.created_at DESC")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "title", column = "title"),
        @Result(property = "description", column = "description"),
        @Result(property = "foundDate", column = "found_date"),
        @Result(property = "foundLocation", column = "found_location"),
        @Result(property = "storageLocation", column = "storage_location"),
        @Result(property = "category", column = "category"),
        @Result(property = "images", column = "images"),
        @Result(property = "contactInfo", column = "contact_info"),
        @Result(property = "claimRequirements", column = "claim_requirements"),
        @Result(property = "status", column = "status"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "username", column = "username")
    })
    List<FoundItem> findByUserId(@Param("userId") Long userId);
    
    @Update("UPDATE found_items SET title = #{title}, description = #{description}, " +
            "found_date = #{foundDate}, found_location = #{foundLocation}, storage_location = #{storageLocation}, category = #{category}, " +
            "images = #{images}, contact_info = #{contactInfo}, claim_requirements = #{claimRequirements}, " +
            "status = #{status}, updated_at = #{updatedAt} " +
            "WHERE id = #{id}")
    void update(FoundItem foundItem);
    
    @Delete("DELETE FROM found_items WHERE id = #{id}")
    void deleteById(@Param("id") Long id);
    
    @Update("UPDATE found_items SET status = #{status}, updated_at = #{updatedAt} WHERE id = #{id}")
    void updateStatus(@Param("id") Long id, @Param("status") String status, @Param("updatedAt") String updatedAt);
} 