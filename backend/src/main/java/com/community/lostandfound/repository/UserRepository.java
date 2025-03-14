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
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "isEnabled", column = "is_enabled"),
        @Result(property = "isLocked", column = "is_locked")
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
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "isEnabled", column = "is_enabled"),
        @Result(property = "isLocked", column = "is_locked")
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
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "isEnabled", column = "is_enabled"),
        @Result(property = "isLocked", column = "is_locked")
    })
    Optional<User> findById(@Param("id") Long id);
    
    @Insert("INSERT INTO users(username, email, password, role, avatar, created_at, updated_at, is_enabled, is_locked) " +
            "VALUES(#{username}, #{email}, #{password}, #{role}, #{avatar}, #{createdAt}, #{updatedAt}, #{isEnabled}, #{isLocked})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void save(User user);
    
    @Update("UPDATE users SET username = #{username}, email = #{email}, password = #{password}, " +
            "role = #{role}, avatar = #{avatar}, updated_at = #{updatedAt}, " +
            "is_enabled = #{isEnabled}, is_locked = #{isLocked} " +
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
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "isEnabled", column = "is_enabled"),
        @Result(property = "isLocked", column = "is_locked")
    })
    List<User> findAll();
    
    @Delete("DELETE FROM users WHERE id = #{id}")
    void deleteById(@Param("id") Long id);
    
    @Select("SELECT EXISTS(SELECT 1 FROM users WHERE username = #{username})")
    boolean existsByUsername(@Param("username") String username);
    
    @Select("SELECT EXISTS(SELECT 1 FROM users WHERE email = #{email})")
    boolean existsByEmail(@Param("email") String email);
} 