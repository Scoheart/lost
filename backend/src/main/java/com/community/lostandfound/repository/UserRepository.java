package com.community.lostandfound.repository;

import com.community.lostandfound.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Options;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface UserRepository {
    
    @Select("SELECT * FROM users WHERE username = #{username}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "username", column = "username"),
        @Result(property = "email", column = "email"),
        @Result(property = "password", column = "password"),
        @Result(property = "role", column = "role"),
        @Result(property = "avatar", column = "avatar"),
        @Result(property = "phone", column = "phone"),
        @Result(property = "realName", column = "real_name"),
        @Result(property = "address", column = "address"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "isLocked", column = "is_locked"),
        @Result(property = "lockEndTime", column = "lock_end_time"),
        @Result(property = "lockReason", column = "lock_reason")
    })
    Optional<User> findByUsername(@Param("username") String username);
    
    @Select("SELECT * FROM users WHERE email = #{email}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "username", column = "username"),
        @Result(property = "email", column = "email"),
        @Result(property = "password", column = "password"),
        @Result(property = "role", column = "role"),
        @Result(property = "avatar", column = "avatar"),
        @Result(property = "phone", column = "phone"),
        @Result(property = "realName", column = "real_name"),
        @Result(property = "address", column = "address"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "isLocked", column = "is_locked"),
        @Result(property = "lockEndTime", column = "lock_end_time"),
        @Result(property = "lockReason", column = "lock_reason")
    })
    Optional<User> findByEmail(@Param("email") String email);
    
    @Select("SELECT * FROM users WHERE id = #{id}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "username", column = "username"),
        @Result(property = "email", column = "email"),
        @Result(property = "password", column = "password"),
        @Result(property = "role", column = "role"),
        @Result(property = "avatar", column = "avatar"),
        @Result(property = "phone", column = "phone"),
        @Result(property = "realName", column = "real_name"),
        @Result(property = "address", column = "address"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "isLocked", column = "is_locked"),
        @Result(property = "lockEndTime", column = "lock_end_time"),
        @Result(property = "lockReason", column = "lock_reason")
    })
    Optional<User> findById(@Param("id") Long id);
    
    @Insert("INSERT INTO users(username, email, password, role, avatar, phone, real_name, address, created_at, updated_at, is_locked) " +
            "VALUES(#{username}, #{email}, #{password}, #{role}, #{avatar}, #{phone}, #{realName}, #{address}, #{createdAt}, #{updatedAt}, #{isLocked})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void save(User user);
    
    @Update("UPDATE users SET username = #{username}, email = #{email}, password = #{password}, " +
            "role = #{role}, avatar = #{avatar}, phone = #{phone}, real_name = #{realName}, " +
            "updated_at = #{updatedAt}, address = #{address}, " +
            "is_locked = #{isLocked}, lock_end_time = #{lockEndTime}, lock_reason = #{lockReason} " +
            "WHERE id = #{id}")
    void update(User user);
    
    @Select("SELECT * FROM users")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "username", column = "username"),
        @Result(property = "email", column = "email"),
        @Result(property = "password", column = "password"),
        @Result(property = "role", column = "role"),
        @Result(property = "avatar", column = "avatar"),
        @Result(property = "phone", column = "phone"),
        @Result(property = "realName", column = "real_name"),
        @Result(property = "address", column = "address"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "isLocked", column = "is_locked"),
        @Result(property = "lockEndTime", column = "lock_end_time"),
        @Result(property = "lockReason", column = "lock_reason")
    })
    List<User> findAll();
    
    @Delete("DELETE FROM users WHERE id = #{id}")
    void deleteById(@Param("id") Long id);
    
    @Select("SELECT EXISTS(SELECT 1 FROM users WHERE username = #{username})")
    boolean existsByUsername(@Param("username") String username);
    
    @Select("SELECT EXISTS(SELECT 1 FROM users WHERE email = #{email})")
    boolean existsByEmail(@Param("email") String email);
    
    /**
     * 根据条件查询用户并分页
     * @param search 搜索词（匹配用户名、邮箱或电话）
     * @param role 用户角色
     * @param isLocked 账号锁定状态，true表示已锁定，false表示正常
     * @param startDate 注册开始日期
     * @param endDate 注册结束日期
     * @param offset 偏移量
     * @param limit 每页数量
     * @return 用户列表
     */
    @Select({"<script>",
            "SELECT * FROM users",
            "WHERE 1=1",
            "<if test='search != null and search != \"\"'>",
            "  AND (username LIKE CONCAT('%', #{search}, '%') ",
            "       OR email LIKE CONCAT('%', #{search}, '%') ",
            "       OR phone LIKE CONCAT('%', #{search}, '%'))",
            "</if>",
            "<if test='role != null and role != \"\"'>",
            "  AND role = #{role}",
            "</if>",
            "<if test='isLocked != null'>",
            "  AND is_locked = #{isLocked}",
            "</if>",
            "<if test='startDate != null'>",
            "  AND created_at >= #{startDate}",
            "</if>",
            "<if test='endDate != null'>",
            "  AND created_at &lt;= #{endDate}",
            "</if>",
            "ORDER BY id DESC",
            "LIMIT #{offset}, #{limit}",
            "</script>"})
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "username", column = "username"),
        @Result(property = "email", column = "email"),
        @Result(property = "password", column = "password"),
        @Result(property = "role", column = "role"),
        @Result(property = "avatar", column = "avatar"),
        @Result(property = "phone", column = "phone"),
        @Result(property = "realName", column = "real_name"),
        @Result(property = "address", column = "address"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "isLocked", column = "is_locked"),
        @Result(property = "lockEndTime", column = "lock_end_time"),
        @Result(property = "lockReason", column = "lock_reason")
    })
    List<User> findWithFilters(
            @Param("search") String search,
            @Param("role") String role,
            @Param("isLocked") Boolean isLocked,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("offset") int offset,
            @Param("limit") int limit);
    
    /**
     * 统计符合条件的用户总数
     * @param search 搜索词（匹配用户名、邮箱或电话）
     * @param role 用户角色
     * @param isLocked 账号锁定状态，true表示已锁定，false表示正常
     * @param startDate 注册开始日期
     * @param endDate 注册结束日期
     * @return 用户总数
     */
    @Select({"<script>",
            "SELECT COUNT(*) FROM users",
            "WHERE 1=1",
            "<if test='search != null and search != \"\"'>",
            "  AND (username LIKE CONCAT('%', #{search}, '%') ",
            "       OR email LIKE CONCAT('%', #{search}, '%') ",
            "       OR phone LIKE CONCAT('%', #{search}, '%'))",
            "</if>",
            "<if test='role != null and role != \"\"'>",
            "  AND role = #{role}",
            "</if>",
            "<if test='isLocked != null'>",
            "  AND is_locked = #{isLocked}",
            "</if>",
            "<if test='startDate != null'>",
            "  AND created_at >= #{startDate}",
            "</if>",
            "<if test='endDate != null'>",
            "  AND created_at &lt;= #{endDate}",
            "</if>",
            "</script>"})
    int countWithFilters(
            @Param("search") String search,
            @Param("role") String role,
            @Param("isLocked") Boolean isLocked,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
} 