package com.community.lostandfound.service.impl;

import com.community.lostandfound.dto.claim.ClaimApplicationDto;
import com.community.lostandfound.dto.claim.ClaimPageDto;
import com.community.lostandfound.dto.claim.ClaimRequestDto;
import com.community.lostandfound.entity.ClaimApplication;
import com.community.lostandfound.entity.FoundItem;
import com.community.lostandfound.exception.BadRequestException;
import com.community.lostandfound.exception.ResourceNotFoundException;
import com.community.lostandfound.repository.ClaimApplicationRepository;
import com.community.lostandfound.repository.FoundItemRepository;
import com.community.lostandfound.service.ClaimApplicationService;
import com.community.lostandfound.service.FoundItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 认领申请服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClaimApplicationServiceImpl implements ClaimApplicationService {

    private final ClaimApplicationRepository claimApplicationRepository;
    private final FoundItemRepository foundItemRepository;
    private final FoundItemService foundItemService;

    @Override
    @Transactional
    public ClaimApplicationDto createClaimApplication(Long foundItemId, ClaimRequestDto request, Long applicantId) {
        // 检查失物招领是否存在
        Optional<FoundItem> foundItemOptional = foundItemService.getFoundItemById(foundItemId);
        if (!foundItemOptional.isPresent()) {
            throw new ResourceNotFoundException("失物招领不存在: ID = " + foundItemId);
        }
        
        FoundItem foundItem = foundItemOptional.get();
        
        // 检查失物招领是否可以被认领（状态为pending）
        if (!"pending".equals(foundItem.getStatus())) {
            throw new BadRequestException("该失物招领当前不可认领，状态: " + foundItem.getStatus());
        }
        
        // 检查用户是否是失物招领的发布者
        if (foundItem.getUserId().equals(applicantId)) {
            throw new BadRequestException("不能认领自己发布的失物招领");
        }
        
        // 检查用户是否有未处理或已批准的认领申请
        // 只有当用户没有未处理或已批准的认领申请时，才允许再次申请
        // 这样，用户的申请被拒绝后可以再次申请
        boolean hasActiveApplication = claimApplicationRepository.existsByFoundItemIdAndApplicantIdAndStatusIn(
                foundItemId, applicantId, List.of("pending", "approved"));
        if (hasActiveApplication) {
            throw new BadRequestException("您已有未处理或已批准的认领申请，请勿重复申请");
        }
        
        // 创建认领申请对象
        LocalDateTime now = LocalDateTime.now();
        ClaimApplication application = ClaimApplication.builder()
                .foundItemId(foundItemId)
                .applicantId(applicantId)
                .description(request.getDescription())
                .status("pending")
                .createdAt(now)
                .updatedAt(now)
                .build();
        
        // 保存认领申请
        int rows = claimApplicationRepository.save(application);
        if (rows <= 0) {
            throw new RuntimeException("保存认领申请失败");
        }
        
        log.info("认领申请创建成功: ID = {}, 失物招领ID = {}, 申请人ID = {}", 
                application.getId(), foundItemId, applicantId);
        
        // 更新失物招领状态为"认领中"
        foundItem.setStatus("processing");
        foundItem.setUpdatedAt(now);
        foundItemRepository.update(foundItem);
        
        log.info("失物招领状态已更新为'认领中': ID = {}", foundItemId);
        
        // 查询完整信息
        ClaimApplication savedApplication = claimApplicationRepository.findById(application.getId());
        return convertToDto(savedApplication);
    }

    @Override
    @Transactional
    public ClaimApplicationDto approveClaimApplication(Long applicationId, Long userId) {
        // 查询认领申请
        ClaimApplication application = getApplicationById(applicationId);
        
        // 查询失物招领
        Optional<FoundItem> foundItemOptional = foundItemService.getFoundItemById(application.getFoundItemId());
        if (!foundItemOptional.isPresent()) {
            throw new ResourceNotFoundException("失物招领不存在: ID = " + application.getFoundItemId());
        }
        
        FoundItem foundItem = foundItemOptional.get();
        
        // 检查用户是否有权处理该申请（是否是失物招领的发布者）
        if (!foundItem.getUserId().equals(userId)) {
            throw new BadRequestException("您没有权限处理该认领申请");
        }
        
        // 检查申请状态是否为待处理
        if (!"pending".equals(application.getStatus())) {
            throw new BadRequestException("该认领申请已经被处理过，当前状态: " + application.getStatus());
        }
        
        // 检查失物招领状态是否为认领中
        if (!"processing".equals(foundItem.getStatus())) {
            throw new BadRequestException("该失物招领状态不是'认领中'，当前状态: " + foundItem.getStatus());
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        // 更新认领申请状态为已批准
        application.setStatus("approved");
        application.setUpdatedAt(now);
        application.setProcessedAt(now);
        claimApplicationRepository.update(application);
        
        log.info("认领申请已批准: ID = {}", applicationId);
        
        // 更新失物招领状态为已认领
        foundItem.setStatus("claimed");
        foundItem.setUpdatedAt(now);
        foundItemRepository.update(foundItem);
        
        log.info("失物招领状态已更新为'已认领': ID = {}", foundItem.getId());
        
        // 查询完整信息
        ClaimApplication updatedApplication = claimApplicationRepository.findById(applicationId);
        return convertToDto(updatedApplication);
    }

    @Override
    @Transactional
    public ClaimApplicationDto rejectClaimApplication(Long applicationId, Long userId) {
        // 查询认领申请
        ClaimApplication application = getApplicationById(applicationId);
        
        // 查询失物招领
        Optional<FoundItem> foundItemOptional = foundItemService.getFoundItemById(application.getFoundItemId());
        if (!foundItemOptional.isPresent()) {
            throw new ResourceNotFoundException("失物招领不存在: ID = " + application.getFoundItemId());
        }
        
        FoundItem foundItem = foundItemOptional.get();
        
        // 检查用户是否有权处理该申请（是否是失物招领的发布者）
        if (!foundItem.getUserId().equals(userId)) {
            throw new BadRequestException("您没有权限处理该认领申请");
        }
        
        // 检查申请状态是否为待处理
        if (!"pending".equals(application.getStatus())) {
            throw new BadRequestException("该认领申请已经被处理过，当前状态: " + application.getStatus());
        }
        
        // 检查失物招领状态是否为认领中
        if (!"processing".equals(foundItem.getStatus())) {
            throw new BadRequestException("该失物招领状态不是'认领中'，当前状态: " + foundItem.getStatus());
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        // 更新认领申请状态为已拒绝
        application.setStatus("rejected");
        application.setUpdatedAt(now);
        application.setProcessedAt(now);
        claimApplicationRepository.update(application);
        
        log.info("认领申请已拒绝: ID = {}", applicationId);
        
        // 更新失物招领状态为待认领
        foundItem.setStatus("pending");
        foundItem.setUpdatedAt(now);
        foundItemRepository.update(foundItem);
        
        log.info("失物招领状态已更新为'待认领': ID = {}", foundItem.getId());
        
        // 查询完整信息
        ClaimApplication updatedApplication = claimApplicationRepository.findById(applicationId);
        return convertToDto(updatedApplication);
    }

    @Override
    public ClaimApplicationDto getClaimApplicationById(Long applicationId) {
        ClaimApplication application = getApplicationById(applicationId);
        return convertToDto(application);
    }

    @Override
    public ClaimPageDto getApplicationsByApplicant(Long userId, String status, int page, int size) {
        // 验证分页参数
        validatePaginationParams(page, size);
        
        // 计算偏移量
        int offset = (page - 1) * size;
        
        // 查询用户提交的认领申请
        List<ClaimApplication> applications = claimApplicationRepository.findByApplicantId(userId, offset, size, status);
        
        // 统计总数
        long totalCount = claimApplicationRepository.countByApplicantId(userId, status);
        
        // 转换为DTO
        List<ClaimApplicationDto> applicationDtos = applications.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        // 计算总页数
        int totalPages = calculateTotalPages(totalCount, size);
        
        // 构建分页DTO
        return buildPageDto(applicationDtos, page, size, totalPages, totalCount);
    }

    @Override
    public ClaimPageDto getApplicationsForProcessing(Long userId, String status, int page, int size) {
        // 验证分页参数
        validatePaginationParams(page, size);
        
        // 计算偏移量
        int offset = (page - 1) * size;
        
        // 查询需要用户处理的认领申请
        List<ClaimApplication> applications = claimApplicationRepository.findByFoundItemOwnerId(userId, offset, size, status);
        
        // 统计总数
        long totalCount = claimApplicationRepository.countByFoundItemOwnerId(userId, status);
        
        // 转换为DTO
        List<ClaimApplicationDto> applicationDtos = applications.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        // 计算总页数
        int totalPages = calculateTotalPages(totalCount, size);
        
        // 构建分页DTO
        return buildPageDto(applicationDtos, page, size, totalPages, totalCount);
    }

    @Override
    public ClaimPageDto getApplicationsByFoundItem(Long foundItemId, int page, int size) {
        // 验证分页参数
        validatePaginationParams(page, size);
        
        // 计算偏移量
        int offset = (page - 1) * size;
        
        // 查询失物招领的认领申请
        List<ClaimApplication> applications = claimApplicationRepository.findByFoundItemId(foundItemId, offset, size);
        
        // 统计总数
        long totalCount = claimApplicationRepository.countByFoundItemId(foundItemId);
        
        // 转换为DTO
        List<ClaimApplicationDto> applicationDtos = applications.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        // 计算总页数
        int totalPages = calculateTotalPages(totalCount, size);
        
        // 构建分页DTO
        return buildPageDto(applicationDtos, page, size, totalPages, totalCount);
    }

    @Override
    public ClaimPageDto getAllApplications(
            String status, 
            String startDate, 
            String endDate, 
            String itemTitle, 
            String applicantName, 
            int page, 
            int size) {
        // 验证分页参数
        validatePaginationParams(page, size);
        
        // 计算偏移量
        int offset = (page - 1) * size;
        
        // 处理日期过滤
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;
        
        if (startDate != null && !startDate.isEmpty()) {
            try {
                startDateTime = LocalDate.parse(startDate).atStartOfDay();
            } catch (Exception e) {
                log.warn("解析开始日期失败: {}", startDate, e);
            }
        }
        
        if (endDate != null && !endDate.isEmpty()) {
            try {
                endDateTime = LocalDate.parse(endDate).atTime(23, 59, 59);
            } catch (Exception e) {
                log.warn("解析结束日期失败: {}", endDate, e);
            }
        }
        
        // 查询所有认领申请
        List<ClaimApplication> applications = claimApplicationRepository.findAllWithFilters(
                status, startDateTime, endDateTime, itemTitle, applicantName, offset, size);
        
        // 统计总数
        long totalCount = claimApplicationRepository.countWithFilters(
                status, startDateTime, endDateTime, itemTitle, applicantName);
        
        // 转换为DTO
        List<ClaimApplicationDto> applicationDtos = applications.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        // 计算总页数
        int totalPages = calculateTotalPages(totalCount, size);
        
        // 构建分页DTO
        return buildPageDto(applicationDtos, page, size, totalPages, totalCount);
    }
    
    @Override
    @Transactional
    public void deleteClaimApplication(Long applicationId) {
        // 查询认领申请是否存在
        ClaimApplication application = getApplicationById(applicationId);
        
        // 如果认领申请状态为"已批准"并且对应的失物招领状态为"已认领"，需要将失物招领状态改回"待认领"
        if ("approved".equals(application.getStatus())) {
            Optional<FoundItem> foundItemOptional = foundItemService.getFoundItemById(application.getFoundItemId());
            if (foundItemOptional.isPresent()) {
                FoundItem foundItem = foundItemOptional.get();
                if ("claimed".equals(foundItem.getStatus())) {
                    foundItem.setStatus("pending");
                    foundItem.setUpdatedAt(LocalDateTime.now());
                    foundItemRepository.update(foundItem);
                    
                    log.info("删除已批准的认领申请，失物招领状态已更新为'待认领': 失物招领ID = {}", foundItem.getId());
                }
            }
        }
        
        // 删除认领申请
        int rows = claimApplicationRepository.deleteById(applicationId);
        if (rows <= 0) {
            throw new RuntimeException("删除认领申请失败: ID = " + applicationId);
        }
        
        log.info("认领申请已删除: ID = {}", applicationId);
    }
    
    /**
     * 根据ID获取认领申请
     *
     * @param applicationId 认领申请ID
     * @return 认领申请对象
     * @throws ResourceNotFoundException 如果认领申请不存在
     */
    private ClaimApplication getApplicationById(Long applicationId) {
        ClaimApplication application = claimApplicationRepository.findById(applicationId);
        if (application == null) {
            throw new ResourceNotFoundException("认领申请不存在: ID = " + applicationId);
        }
        return application;
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
    private ClaimPageDto buildPageDto(List<ClaimApplicationDto> items, int page, int pageSize, int totalPages, long totalItems) {
        return ClaimPageDto.builder()
                .applications(items)
                .currentPage(page)
                .pageSize(pageSize)
                .totalPages(totalPages)
                .totalItems(totalItems)
                .build();
    }

    /**
     * 将认领申请实体转换为DTO
     *
     * @param application 认领申请实体
     * @return 认领申请DTO
     */
    private ClaimApplicationDto convertToDto(ClaimApplication application) {
        // 获取失物招领图片（如果有）
        String foundItemImage = null;
        Optional<FoundItem> foundItemOptional = foundItemService.getFoundItemById(application.getFoundItemId());
        if (foundItemOptional.isPresent()) {
            FoundItem foundItem = foundItemOptional.get();
            List<String> images = foundItem.getImagesListForSerialization();
            if (images != null && !images.isEmpty()) {
                foundItemImage = images.get(0);
            }
        }
        
        return ClaimApplicationDto.builder()
                .id(application.getId())
                .foundItemId(application.getFoundItemId())
                .foundItemTitle(application.getFoundItemTitle())
                .foundItemImage(foundItemImage)
                .applicantId(application.getApplicantId())
                .applicantName(application.getApplicantName())
                .applicantContact(application.getApplicantContact())
                .ownerId(application.getFoundItemOwnerId())
                .ownerName(application.getFoundItemOwnerName())
                .description(application.getDescription())
                .status(application.getStatus())
                .createdAt(application.getCreatedAt())
                .updatedAt(application.getUpdatedAt())
                .processedAt(application.getProcessedAt())
                .build();
    }
} 