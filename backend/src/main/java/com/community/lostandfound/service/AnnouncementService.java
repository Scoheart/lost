package com.community.lostandfound.service;

import com.community.lostandfound.dto.announcement.AnnouncementDto;
import com.community.lostandfound.dto.announcement.AnnouncementPageDto;
import com.community.lostandfound.dto.announcement.CreateAnnouncementRequest;
import com.community.lostandfound.dto.announcement.UpdateAnnouncementRequest;

/**
 * 公告服务接口
 */
public interface AnnouncementService {
    
    /**
     * 获取所有公告（分页）
     *
     * @param page      页码
     * @param pageSize  每页条数
     * @param keyword   搜索关键词（标题或内容，可选）
     * @param adminName 管理员用户名（可选）
     * @return 分页公告列表
     */
    AnnouncementPageDto getAllAnnouncements(int page, int pageSize, String keyword, String adminName);
    
    /**
     * 获取已发布公告（分页）
     *
     * @param page     页码
     * @param pageSize 每页条数
     * @param keyword  搜索关键词（标题或内容，可选）
     * @return 分页公告列表
     */
    AnnouncementPageDto getPublishedAnnouncements(int page, int pageSize, String keyword);
    
    /**
     * 根据ID获取公告
     *
     * @param id 公告ID
     * @return 公告DTO
     */
    AnnouncementDto getAnnouncementById(Long id);
    
    /**
     * 创建公告
     *
     * @param request  创建公告请求
     * @param adminId  管理员ID
     * @return 创建的公告DTO
     */
    AnnouncementDto createAnnouncement(CreateAnnouncementRequest request, Long adminId);
    
    /**
     * 更新公告
     *
     * @param id       公告ID
     * @param request  更新公告请求
     * @param adminId  管理员ID
     * @return 更新后的公告DTO
     */
    AnnouncementDto updateAnnouncement(Long id, UpdateAnnouncementRequest request, Long adminId);
    
    /**
     * 删除公告
     *
     * @param id      公告ID
     * @param adminId 管理员ID
     * @return 是否删除成功
     */
    boolean deleteAnnouncement(Long id, Long adminId);
    
    /**
     * 根据管理员ID获取公告（分页）
     *
     * @param adminId  管理员ID
     * @param page     页码
     * @param pageSize 每页条数
     * @return 分页公告列表
     */
    AnnouncementPageDto getAnnouncementsByAdminId(Long adminId, int page, int pageSize);
} 