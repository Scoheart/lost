package com.community.lostandfound.repository;

import com.community.lostandfound.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * 公告数据访问接口
 */
@Mapper
public interface AnnouncementRepository {
    
    /**
     * 保存公告
     *
     * @param announcement 公告对象
     * @return 受影响的行数
     */
    int save(Announcement announcement);
    
    /**
     * 更新公告
     *
     * @param announcement 公告对象
     * @return 受影响的行数
     */
    int update(Announcement announcement);
    
    /**
     * 删除公告
     *
     * @param id 公告ID
     * @return 受影响的行数
     */
    int deleteById(Long id);
    
    /**
     * 查询所有公告（分页）
     *
     * @param offset    偏移量
     * @param pageSize  每页条数
     * @param keyword   搜索关键词（标题或内容，可选）
     * @param adminName 管理员用户名（可选）
     * @return 公告列表
     */
    List<Announcement> findAll(
            @Param("offset") int offset, 
            @Param("pageSize") int pageSize,
            @Param("keyword") String keyword,
            @Param("adminName") String adminName);
    
    /**
     * 根据ID查询公告
     *
     * @param id 公告ID
     * @return 公告对象
     */
    Announcement findById(Long id);
    
    /**
     * 统计公告总数
     *
     * @param keyword   搜索关键词（标题或内容，可选）
     * @param adminName 管理员用户名（可选）
     * @return 公告总数
     */
    long count(
            @Param("keyword") String keyword,
            @Param("adminName") String adminName);
    
    /**
     * 查询已发布公告（分页）
     *
     * @param offset   偏移量
     * @param pageSize 每页条数
     * @return 公告列表
     */
    List<Announcement> findPublished(@Param("offset") int offset, @Param("pageSize") int pageSize);
    
    /**
     * 统计已发布公告总数
     *
     * @return 已发布公告总数
     */
    long countPublished();
    
    /**
     * 根据管理员ID查询公告（分页）
     *
     * @param adminId  管理员ID
     * @param offset   偏移量
     * @param pageSize 每页条数
     * @return 公告列表
     */
    List<Announcement> findByAdminId(@Param("adminId") Long adminId, @Param("offset") int offset, @Param("pageSize") int pageSize);
    
    /**
     * 统计指定管理员发布的公告总数
     *
     * @param adminId 管理员ID
     * @return 公告总数
     */
    long countByAdminId(@Param("adminId") Long adminId);
    
    /**
     * Increment view count for an announcement
     * @param id the announcement id
     */
    void incrementViewCount(@Param("id") Long id);
    
    /**
     * 根据作者ID查询公告
     *
     * @param authorId 作者ID
     * @param offset 起始位置（从0开始）
     * @param pageSize 每页条数
     * @return 该作者发布的公告列表
     */
    List<Announcement> findByAuthorId(
            @Param("authorId") Long authorId, 
            @Param("offset") int offset, 
            @Param("pageSize") int pageSize);
    
    /**
     * 统计指定作者发布的公告数量
     *
     * @param authorId 作者ID
     * @return 公告总数
     */
    long countByAuthorId(@Param("authorId") Long authorId);
    
    /**
     * 根据社区ID查询已发布公告
     *
     * @param communityId 社区ID
     * @param offset 起始位置（从0开始）
     * @param pageSize 每页条数
     * @return 该社区的已发布公告列表
     */
    List<Announcement> findByCommunityId(
            @Param("communityId") Long communityId, 
            @Param("offset") int offset, 
            @Param("pageSize") int pageSize);
    
    /**
     * 统计指定社区已发布公告数量
     *
     * @param communityId 社区ID
     * @return 已发布公告总数
     */
    long countByCommunityId(@Param("communityId") Long communityId);
    
    /**
     * 根据关键词搜索已发布公告（分页）
     *
     * @param offset   偏移量
     * @param pageSize 每页条数
     * @param keyword  搜索关键词
     * @return 公告列表
     */
    List<Announcement> findPublishedByKeyword(
            @Param("offset") int offset, 
            @Param("pageSize") int pageSize, 
            @Param("keyword") String keyword);
    
    /**
     * 统计符合关键词的已发布公告总数
     *
     * @param keyword 搜索关键词
     * @return 已发布公告总数
     */
    long countPublishedByKeyword(@Param("keyword") String keyword);
} 