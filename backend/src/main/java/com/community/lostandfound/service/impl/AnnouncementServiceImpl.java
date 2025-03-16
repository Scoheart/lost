package com.community.lostandfound.service.impl;

import com.community.lostandfound.dto.announcement.AnnouncementDto;
import com.community.lostandfound.dto.announcement.AnnouncementPageDto;
import com.community.lostandfound.dto.announcement.CreateAnnouncementRequest;
import com.community.lostandfound.dto.announcement.UpdateAnnouncementRequest;
import com.community.lostandfound.entity.Announcement;
import com.community.lostandfound.exception.ResourceNotFoundException;
import com.community.lostandfound.repository.AnnouncementRepository;
import com.community.lostandfound.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 公告服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementRepository announcementRepository;

    @Override
    public AnnouncementPageDto getAllAnnouncements(int page, int pageSize, String keyword, String adminName) {
        // 验证分页参数
        validatePaginationParams(page, pageSize);
        
        // 计算偏移量
        int offset = (page - 1) * pageSize;
        
        // 查询公告列表
        List<Announcement> announcements = announcementRepository.findAll(offset, pageSize, keyword, adminName);
        
        // 统计总数
        long totalCount = announcementRepository.count(keyword, adminName);
        
        // 转换为DTO
        List<AnnouncementDto> announcementDtos = announcements.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        // 计算总页数
        int totalPages = calculateTotalPages(totalCount, pageSize);
        
        // 构建分页DTO
        return buildPageDto(announcementDtos, page, pageSize, totalPages, totalCount);
    }

    @Override
    public AnnouncementPageDto getPublishedAnnouncements(int page, int pageSize, String keyword) {
        // 验证分页参数
        validatePaginationParams(page, pageSize);
        
        // 计算偏移量
        int offset = (page - 1) * pageSize;
        
        // 查询已发布公告，支持关键词搜索
        List<Announcement> announcements;
        long totalCount;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            log.debug("按关键词搜索已发布公告, 关键词: {}", keyword);
            announcements = announcementRepository.findPublishedByKeyword(offset, pageSize, keyword);
            totalCount = announcementRepository.countPublishedByKeyword(keyword);
        } else {
            announcements = announcementRepository.findPublished(offset, pageSize);
            totalCount = announcementRepository.countPublished();
        }
        
        // 转换为DTO
        List<AnnouncementDto> announcementDtos = announcements.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        // 计算总页数
        int totalPages = calculateTotalPages(totalCount, pageSize);
        
        // 构建分页DTO
        return buildPageDto(announcementDtos, page, pageSize, totalPages, totalCount);
    }

    @Override
    public AnnouncementDto getAnnouncementById(Long id) {
        Announcement announcement = announcementRepository.findById(id);
        if (announcement == null) {
            throw new ResourceNotFoundException("公告不存在: ID = " + id);
        }
        return convertToDto(announcement);
    }

    @Override
    @Transactional
    public AnnouncementDto createAnnouncement(CreateAnnouncementRequest request, Long adminId) {
        // 创建公告对象
        Announcement announcement = Announcement.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .adminId(adminId)
                .status(request.getStatus())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        // 保存公告
        int rows = announcementRepository.save(announcement);
        if (rows <= 0) {
            throw new RuntimeException("保存公告失败");
        }
        
        log.info("公告创建成功: ID = {}, 标题 = {}", announcement.getId(), announcement.getTitle());
        
        // 查询完整信息（包括管理员名称）
        Announcement savedAnnouncement = announcementRepository.findById(announcement.getId());
        return convertToDto(savedAnnouncement);
    }

    @Override
    @Transactional
    public AnnouncementDto updateAnnouncement(Long id, UpdateAnnouncementRequest request, Long adminId) {
        // 查询原公告
        Announcement announcement = announcementRepository.findById(id);
        if (announcement == null) {
            throw new ResourceNotFoundException("公告不存在: ID = " + id);
        }
        
        // 检查是否是公告发布者
        if (!announcement.getAdminId().equals(adminId)) {
            throw new IllegalArgumentException("无权修改此公告");
        }
        
        // 更新公告信息
        if (request.getTitle() != null) {
            announcement.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            announcement.setContent(request.getContent());
        }
        if (request.getStatus() != null) {
            announcement.setStatus(request.getStatus());
        }
        
        // 更新时间
        announcement.setUpdatedAt(LocalDateTime.now());
        
        // 更新公告
        int rows = announcementRepository.update(announcement);
        if (rows <= 0) {
            throw new RuntimeException("更新公告失败");
        }
        
        log.info("公告更新成功: ID = {}, 标题 = {}", announcement.getId(), announcement.getTitle());
        
        // 查询完整信息（包括管理员名称）
        Announcement updatedAnnouncement = announcementRepository.findById(id);
        return convertToDto(updatedAnnouncement);
    }

    @Override
    @Transactional
    public boolean deleteAnnouncement(Long id, Long adminId) {
        // 查询原公告
        Announcement announcement = announcementRepository.findById(id);
        if (announcement == null) {
            throw new ResourceNotFoundException("公告不存在: ID = " + id);
        }
        
        // 检查是否是公告发布者
        if (!announcement.getAdminId().equals(adminId)) {
            throw new IllegalArgumentException("无权删除此公告");
        }
        
        // 删除公告
        int rows = announcementRepository.deleteById(id);
        
        log.info("公告{}删除: ID = {}, 标题 = {}", rows > 0 ? "成功" : "失败", id, announcement.getTitle());
        
        return rows > 0;
    }

    @Override
    public AnnouncementPageDto getAnnouncementsByAdminId(Long adminId, int page, int pageSize) {
        // 验证分页参数
        validatePaginationParams(page, pageSize);
        
        // 计算偏移量
        int offset = (page - 1) * pageSize;
        
        // 查询指定管理员的公告
        List<Announcement> announcements = announcementRepository.findByAdminId(adminId, offset, pageSize);
        
        // 统计总数
        long totalCount = announcementRepository.countByAdminId(adminId);
        
        // 转换为DTO
        List<AnnouncementDto> announcementDtos = announcements.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        // 计算总页数
        int totalPages = calculateTotalPages(totalCount, pageSize);
        
        // 构建分页DTO
        return buildPageDto(announcementDtos, page, pageSize, totalPages, totalCount);
    }

    /**
     * 验证分页参数
     *
     * @param page     页码
     * @param pageSize 每页条数
     */
    private void validatePaginationParams(int page, int pageSize) {
        if (page < 1) {
            throw new IllegalArgumentException("页码必须大于等于1");
        }
        if (pageSize < 1 || pageSize > 100) {
            throw new IllegalArgumentException("每页条数必须在1到100之间");
        }
    }

    /**
     * 计算总页数
     *
     * @param totalCount 总记录数
     * @param pageSize   每页条数
     * @return 总页数
     */
    private int calculateTotalPages(long totalCount, int pageSize) {
        return (int) Math.ceil((double) totalCount / pageSize);
    }

    /**
     * 构建分页DTO
     *
     * @param items       记录列表
     * @param page        当前页码
     * @param pageSize    每页条数
     * @param totalPages  总页数
     * @param totalItems  总记录数
     * @return 分页DTO
     */
    private AnnouncementPageDto buildPageDto(List<AnnouncementDto> items, int page, int pageSize, int totalPages, long totalItems) {
        return AnnouncementPageDto.builder()
                .announcements(items)
                .currentPage(page)
                .pageSize(pageSize)
                .totalPages(totalPages)
                .totalItems(totalItems)
                .build();
    }

    /**
     * 将公告实体转换为DTO
     *
     * @param announcement 公告实体
     * @return 公告DTO
     */
    private AnnouncementDto convertToDto(Announcement announcement) {
        return AnnouncementDto.builder()
                .id(announcement.getId())
                .title(announcement.getTitle())
                .content(announcement.getContent())
                .adminId(announcement.getAdminId())
                .adminName(announcement.getAdminName())
                .status(announcement.getStatus())
                .createdAt(announcement.getCreatedAt())
                .updatedAt(announcement.getUpdatedAt())
                .build();
    }
} 