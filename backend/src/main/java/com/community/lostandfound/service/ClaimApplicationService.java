package com.community.lostandfound.service;

import com.community.lostandfound.dto.claim.ClaimApplicationDto;
import com.community.lostandfound.dto.claim.ClaimPageDto;
import com.community.lostandfound.dto.claim.ClaimRequestDto;

/**
 * 认领申请服务接口
 */
public interface ClaimApplicationService {
    
    /**
     * 提交认领申请
     *
     * @param foundItemId 失物招领ID
     * @param request     认领申请请求
     * @param applicantId 申请人ID
     * @return 创建的认领申请DTO
     */
    ClaimApplicationDto createClaimApplication(Long foundItemId, ClaimRequestDto request, Long applicantId);
    
    /**
     * 批准认领申请
     *
     * @param applicationId 认领申请ID
     * @param userId        当前用户ID
     * @return 更新后的认领申请DTO
     */
    ClaimApplicationDto approveClaimApplication(Long applicationId, Long userId);
    
    /**
     * 拒绝认领申请
     *
     * @param applicationId 认领申请ID
     * @param userId        当前用户ID
     * @return 更新后的认领申请DTO
     */
    ClaimApplicationDto rejectClaimApplication(Long applicationId, Long userId);
    
    /**
     * 获取认领申请详情
     *
     * @param applicationId 认领申请ID
     * @return 认领申请DTO
     */
    ClaimApplicationDto getClaimApplicationById(Long applicationId);
    
    /**
     * 查询当前用户提交的认领申请
     *
     * @param userId  用户ID
     * @param status  状态（可选）
     * @param page    页码
     * @param size    每页条数
     * @return 分页认领申请列表
     */
    ClaimPageDto getApplicationsByApplicant(Long userId, String status, int page, int size);
    
    /**
     * 查询需要当前用户处理的认领申请
     * （即用户发布的失物招领收到的申请）
     *
     * @param userId  用户ID
     * @param status  状态（可选）
     * @param page    页码
     * @param size    每页条数
     * @return 分页认领申请列表
     */
    ClaimPageDto getApplicationsForProcessing(Long userId, String status, int page, int size);
    
    /**
     * 根据失物招领ID查询认领申请
     *
     * @param foundItemId 失物招领ID
     * @param page        页码
     * @param size        每页条数
     * @return 分页认领申请列表
     */
    ClaimPageDto getApplicationsByFoundItem(Long foundItemId, int page, int size);
    
    /**
     * 查询所有认领申请（管理员接口）
     *
     * @param status 状态（可选）
     * @param page   页码
     * @param size   每页条数
     * @return 分页认领申请列表
     */
    ClaimPageDto getAllApplications(String status, int page, int size);
} 