package com.community.lostandfound.service.impl;

import com.community.lostandfound.dto.report.ReportDto;
import com.community.lostandfound.dto.report.ReportPageDto;
import com.community.lostandfound.dto.report.ReportRequest;
import com.community.lostandfound.dto.report.ReportResolutionRequest;
import com.community.lostandfound.entity.FoundItem;
import com.community.lostandfound.entity.ItemComment;
import com.community.lostandfound.entity.LostItem;
import com.community.lostandfound.entity.Post;
import com.community.lostandfound.entity.PostComment;
import com.community.lostandfound.entity.Report;
import com.community.lostandfound.entity.User;
import com.community.lostandfound.exception.BadRequestException;
import com.community.lostandfound.exception.ResourceNotFoundException;
import com.community.lostandfound.repository.FoundItemRepository;
import com.community.lostandfound.repository.ItemCommentRepository;
import com.community.lostandfound.repository.LostItemRepository;
import com.community.lostandfound.repository.PostCommentRepository;
import com.community.lostandfound.repository.PostRepository;
import com.community.lostandfound.repository.ReportRepository;
import com.community.lostandfound.service.ReportService;
import com.community.lostandfound.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final UserService userService;
    private final LostItemRepository lostItemRepository;
    private final FoundItemRepository foundItemRepository;
    private final ItemCommentRepository itemCommentRepository;
    private final PostCommentRepository postCommentRepository;
    private final PostRepository postRepository;

    @Override
    @Transactional
    public ReportDto createReport(ReportRequest request, Long reporterId) {
        // 获取举报人信息
        User reporter = userService.getUserById(reporterId)
                .orElseThrow(() -> new ResourceNotFoundException("举报人不存在"));
        
        // 根据举报类型和内容ID，查找对应的被举报内容和被举报人
        Long reportedUserId = null;
        String reportedItemTitle = null;
        
        switch (request.getReportType()) {
            case LOST_ITEM:
                LostItem lostItem = lostItemRepository.findById(request.getReportedItemId())
                        .orElseThrow(() -> new ResourceNotFoundException("寻物启事不存在"));
                reportedUserId = lostItem.getUserId();
                reportedItemTitle = lostItem.getTitle();
                break;
                
            case FOUND_ITEM:
                FoundItem foundItem = foundItemRepository.findById(request.getReportedItemId())
                        .orElseThrow(() -> new ResourceNotFoundException("失物招领不存在"));
                reportedUserId = foundItem.getUserId();
                reportedItemTitle = foundItem.getTitle();
                break;
                
            case COMMENT:
                // 尝试从ItemComment查找
                ItemComment itemComment = itemCommentRepository.findById(request.getReportedItemId())
                        .orElse(null);
                
                if (itemComment != null) {
                    reportedUserId = itemComment.getUserId();
                    reportedItemTitle = "物品留言ID: " + itemComment.getId();
                } else {
                    // 尝试从PostComment查找
                    PostComment postComment = postCommentRepository.findById(request.getReportedItemId())
                            .orElseThrow(() -> new ResourceNotFoundException("留言不存在"));
                    reportedUserId = postComment.getUserId();
                    reportedItemTitle = "帖子留言ID: " + postComment.getId();
                }
                break;
                
            case POST:
                Post post = postRepository.findById(request.getReportedItemId())
                        .orElseThrow(() -> new ResourceNotFoundException("帖子不存在"));
                reportedUserId = post.getUserId();
                reportedItemTitle = post.getTitle();
                break;
                
            default:
                throw new BadRequestException("不支持的举报类型");
        }
        
        // 自己不能举报自己
        if (reporterId.equals(reportedUserId)) {
            throw new BadRequestException("不能举报自己的内容");
        }
        
        // 检查是否已经举报过同样的内容
        List<Report> existingReports = reportRepository.findByReportTypeAndReportedItemId(
                request.getReportType(), request.getReportedItemId());
        
        boolean alreadyReported = existingReports.stream()
                .anyMatch(r -> r.getReporterId().equals(reporterId));
        
        if (alreadyReported) {
            throw new BadRequestException("你已经举报过该内容");
        }
        
        // 创建举报实体
        Report report = Report.builder()
                .reportType(request.getReportType())
                .reportedItemId(request.getReportedItemId())
                .reporterId(reporterId)
                .reportedUserId(reportedUserId)
                .reason(request.getReason())
                .status(Report.ReportStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        
        // 保存举报
        reportRepository.insert(report);
        
        // 转换为DTO并返回
        ReportDto reportDto = convertToDto(report);
        reportDto.setReporterUsername(reporter.getUsername());
        reportDto.setReportedUsername(userService.getUserById(reportedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("被举报人不存在"))
                .getUsername());
        reportDto.setReportedItemTitle(reportedItemTitle);
        
        return reportDto;
    }

    @Override
    public ReportDto getReportById(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("举报不存在"));
        
        return enrichReportDto(convertToDto(report));
    }

    @Override
    public ReportPageDto getReports(Integer page, Integer size, Report.ReportStatus status, Report.ReportType type) {
        // 计算分页偏移量
        page = page < 1 ? 0 : page - 1;
        int offset = page * size;
        
        List<Report> reports;
        long totalItems;
        
        // 根据查询条件获取相应的数据
        if (status != null && type != null) {
            reports = reportRepository.findByStatusAndReportType(status, type, offset, size);
            totalItems = reportRepository.countByStatusAndReportType(status, type);
        } else if (status != null) {
            reports = reportRepository.findByStatus(status, offset, size);
            totalItems = reportRepository.countByStatus(status);
        } else if (type != null) {
            reports = reportRepository.findByReportType(type, offset, size);
            totalItems = reportRepository.countByReportType(type);
        } else {
            reports = reportRepository.findAll(offset, size);
            totalItems = reportRepository.count();
        }
        
        // 转换为DTO列表
        List<ReportDto> reportDtos = reports.stream()
                .map(this::convertToDto)
                .map(this::enrichReportDto)
                .collect(Collectors.toList());
        
        // 计算总页数
        int totalPages = (int) Math.ceil((double) totalItems / size);
        
        // 获取待处理举报数量
        long pendingCount = reportRepository.countByStatus(Report.ReportStatus.PENDING);
        
        return ReportPageDto.builder()
                .reports(reportDtos)
                .totalItems(totalItems)
                .totalPages(totalPages)
                .currentPage(page + 1)
                .pageSize(size)
                .pendingReportsCount(pendingCount)
                .build();
    }

    @Override
    @Transactional
    public ReportDto resolveReport(Long reportId, ReportResolutionRequest resolution, Long adminId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("举报不存在"));
        
        // 只能处理待处理的举报
        if (report.getStatus() != Report.ReportStatus.PENDING) {
            throw new BadRequestException("该举报已经被处理");
        }
        
        // 更新举报状态
        report.setStatus(resolution.getStatus());
        report.setResolutionNotes(resolution.getResolutionNotes());
        report.setResolvedByAdminId(adminId);
        report.setResolvedAt(LocalDateTime.now());
        
        // 如果要采取行动且举报状态为已处理
        if (resolution.getStatus() == Report.ReportStatus.RESOLVED && 
                resolution.getActionType() != null &&
                resolution.getActionType() != ReportResolutionRequest.ActionType.NONE) {
            
            User reportedUser = userService.getUserById(report.getReportedUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("被举报人不存在"));
            
            switch (resolution.getActionType()) {
                case CONTENT_DELETE:
                    // 根据举报类型删除内容
                    deleteReportedContent(report);
                    break;
                    
                case USER_WARNING:
                    // 警告用户 - 实际实现可能包括发送通知等
                    log.info("警告用户: {}", reportedUser.getUsername());
                    break;
                    
                case USER_BAN:
                    // 封禁用户
                    if (resolution.getActionDays() == null || resolution.getActionDays() <= 0) {
                        throw new BadRequestException("封禁天数必须大于0");
                    }
                    banUser(reportedUser, resolution.getActionDays(), "违反平台规则: " + resolution.getResolutionNotes());
                    break;
                    
                case USER_LOCK:
                    // 锁定用户
                    if (resolution.getActionDays() == null || resolution.getActionDays() <= 0) {
                        throw new BadRequestException("锁定天数必须大于0");
                    }
                    lockUser(reportedUser, resolution.getActionDays(), "违反平台规则: " + resolution.getResolutionNotes());
                    break;
                    
                default:
                    break;
            }
        }
        
        // 更新举报
        reportRepository.update(report);
        return enrichReportDto(convertToDto(report));
    }

    @Override
    public List<ReportDto> getReportsByReporter(Long reporterId) {
        // 获取用户的前100条举报记录
        List<Report> reports = reportRepository.findByReporterId(reporterId, 0, 100);
        
        return reports.stream()
                .map(this::convertToDto)
                .map(this::enrichReportDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReportDto> getReportsByItem(Report.ReportType type, Long itemId) {
        List<Report> reports = reportRepository.findByReportTypeAndReportedItemId(type, itemId);
        
        return reports.stream()
                .map(this::convertToDto)
                .map(this::enrichReportDto)
                .collect(Collectors.toList());
    }

    @Override
    public long getPendingReportsCount() {
        return reportRepository.countByStatus(Report.ReportStatus.PENDING);
    }

    @Override
    @Transactional
    public void deleteReportsByUser(Long userId) {
        // 删除用户的所有举报（作为举报者）
        reportRepository.deleteByReporterId(userId);
        
        // 删除针对用户的所有举报（作为被举报者）
        reportRepository.deleteByReportedUserId(userId);
    }
    
    /**
     * 根据举报内容类型删除对应的内容
     */
    private void deleteReportedContent(Report report) {
        switch (report.getReportType()) {
            case LOST_ITEM:
                lostItemRepository.deleteById(report.getReportedItemId());
                break;
                
            case FOUND_ITEM:
                foundItemRepository.deleteById(report.getReportedItemId());
                break;
                
            case COMMENT:
                // 尝试删除ItemComment
                try {
                    itemCommentRepository.deleteById(report.getReportedItemId());
                } catch (Exception e) {
                    // 如果不是ItemComment，尝试删除PostComment
                    try {
                        postCommentRepository.deleteById(report.getReportedItemId());
                    } catch (Exception ex) {
                        log.error("删除评论失败，ID: {}", report.getReportedItemId(), ex);
                        throw new ResourceNotFoundException("评论不存在或无法删除");
                    }
                }
                break;
                
            case POST:
                postRepository.deleteById(report.getReportedItemId());
                break;
                
            default:
                throw new BadRequestException("不支持的举报类型");
        }
    }
    
    /**
     * 封禁用户
     */
    private void banUser(User user, int days, String reason) {
        user.setIsBanned(true);
        user.setBanEndTime(LocalDateTime.now().plusDays(days));
        user.setBanReason(reason);
        userService.updateUser(user);
        
        log.info("用户 {} 被封禁 {} 天，原因: {}", user.getUsername(), days, reason);
    }
    
    /**
     * 锁定用户
     */
    private void lockUser(User user, int days, String reason) {
        user.setIsLocked(true);
        user.setLockEndTime(LocalDateTime.now().plusDays(days));
        user.setLockReason(reason);
        userService.updateUser(user);
        
        log.info("用户 {} 被锁定 {} 天，原因: {}", user.getUsername(), days, reason);
    }
    
    /**
     * 将Report实体转换为ReportDto
     */
    private ReportDto convertToDto(Report report) {
        return ReportDto.builder()
                .id(report.getId())
                .reportType(report.getReportType())
                .reportedItemId(report.getReportedItemId())
                .reporterId(report.getReporterId())
                .reportedUserId(report.getReportedUserId())
                .reason(report.getReason())
                .status(report.getStatus())
                .resolutionNotes(report.getResolutionNotes())
                .resolvedByAdminId(report.getResolvedByAdminId())
                .createdAt(report.getCreatedAt())
                .resolvedAt(report.getResolvedAt())
                .build();
    }
    
    /**
     * 为ReportDto添加关联数据
     */
    private ReportDto enrichReportDto(ReportDto dto) {
        try {
            // 添加用户名
            User reporter = userService.getUserById(dto.getReporterId())
                    .orElseThrow(() -> new ResourceNotFoundException("举报人不存在"));
            dto.setReporterUsername(reporter.getUsername());
            
            User reportedUser = userService.getUserById(dto.getReportedUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("被举报人不存在"));
            dto.setReportedUsername(reportedUser.getUsername());
            
            // 添加处理管理员用户名
            if (dto.getResolvedByAdminId() != null) {
                User admin = userService.getUserById(dto.getResolvedByAdminId())
                        .orElseThrow(() -> new ResourceNotFoundException("处理管理员不存在"));
                dto.setResolvedByAdminUsername(admin.getUsername());
            }
            
            // 添加被举报内容标题
            String title = "";
            switch (dto.getReportType()) {
                case LOST_ITEM:
                    LostItem lostItem = lostItemRepository.findById(dto.getReportedItemId()).orElse(null);
                    if (lostItem != null) {
                        title = lostItem.getTitle();
                    }
                    break;
                    
                case FOUND_ITEM:
                    FoundItem foundItem = foundItemRepository.findById(dto.getReportedItemId()).orElse(null);
                    if (foundItem != null) {
                        title = foundItem.getTitle();
                    }
                    break;
                    
                case COMMENT:
                    title = "留言ID: " + dto.getReportedItemId();
                    break;
                    
                case POST:
                    Post post = postRepository.findById(dto.getReportedItemId()).orElse(null);
                    if (post != null) {
                        title = post.getTitle();
                    } else {
                        title = "帖子ID: " + dto.getReportedItemId() + " (已删除)";
                    }
                    break;
                    
                default:
                    break;
            }
            
            dto.setReportedItemTitle(title);
            
        } catch (Exception e) {
            log.error("填充举报DTO失败", e);
        }
        
        return dto;
    }
} 