package com.community.lostandfound.controller;

import com.community.lostandfound.dto.claim.ClaimApplicationDto;
import com.community.lostandfound.dto.claim.ClaimPageDto;
import com.community.lostandfound.dto.claim.ClaimRequestDto;
import com.community.lostandfound.dto.common.ApiResponse;
import com.community.lostandfound.exception.BadRequestException;
import com.community.lostandfound.exception.ResourceNotFoundException;
import com.community.lostandfound.security.CurrentUser;
import com.community.lostandfound.security.UserDetailsImpl;
import com.community.lostandfound.service.ClaimApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 认领申请控制器
 * 提供认领申请的提交、审核、查询等功能
 */
@Slf4j
@RestController
@RequestMapping("/claims")
@RequiredArgsConstructor
public class ClaimApplicationController {

    private final ClaimApplicationService claimApplicationService;

    /**
     * 提交认领申请
     *
     * @param foundItemId 失物招领ID
     * @param request     认领申请请求
     * @param currentUser 当前用户
     * @return 认领申请详情
     */
    @PostMapping("/apply/{foundItemId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ClaimApplicationDto>> applyForClaim(
            @PathVariable Long foundItemId,
            @Valid @RequestBody ClaimRequestDto request,
            @CurrentUser UserDetailsImpl currentUser) {
        
        log.info("用户提交认领申请, 用户ID: {}, 失物招领ID: {}", currentUser.getId(), foundItemId);
        
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("未授权操作，请先登录"));
        }
        
        try {
            ClaimApplicationDto application = claimApplicationService.createClaimApplication(
                    foundItemId, request, currentUser.getId());
            
            log.debug("认领申请提交成功, 申请ID: {}", application.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("认领申请已提交", application));
        } catch (ResourceNotFoundException e) {
            log.warn("提交认领申请失败, 找不到对应的失物招领: {}", foundItemId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail(e.getMessage()));
        } catch (BadRequestException e) {
            log.warn("提交认领申请失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.fail(e.getMessage()));
        } catch (Exception e) {
            log.error("提交认领申请时发生错误", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("提交认领申请失败：" + e.getMessage()));
        }
    }

    /**
     * 批准认领申请
     *
     * @param applicationId 认领申请ID
     * @param currentUser   当前用户
     * @return 认领申请详情
     */
    @PostMapping("/approve/{applicationId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ClaimApplicationDto>> approveApplication(
            @PathVariable Long applicationId,
            @CurrentUser UserDetailsImpl currentUser) {
        
        log.info("用户批准认领申请, 用户ID: {}, 申请ID: {}", currentUser.getId(), applicationId);
        
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("未授权操作，请先登录"));
        }
        
        try {
            ClaimApplicationDto application = claimApplicationService.approveClaimApplication(
                    applicationId, currentUser.getId());
            
            log.debug("认领申请已批准, 申请ID: {}", application.getId());
            return ResponseEntity.ok(ApiResponse.success("已批准认领申请", application));
        } catch (ResourceNotFoundException e) {
            log.warn("批准认领申请失败, 找不到对应的申请: {}", applicationId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail(e.getMessage()));
        } catch (BadRequestException e) {
            log.warn("批准认领申请失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.fail(e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.warn("无权批准认领申请: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.fail(e.getMessage()));
        } catch (Exception e) {
            log.error("批准认领申请时发生错误", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("批准认领申请失败：" + e.getMessage()));
        }
    }

    /**
     * 拒绝认领申请
     *
     * @param applicationId 认领申请ID
     * @param currentUser   当前用户
     * @return 认领申请详情
     */
    @PostMapping("/reject/{applicationId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ClaimApplicationDto>> rejectApplication(
            @PathVariable Long applicationId,
            @CurrentUser UserDetailsImpl currentUser) {
        
        log.info("用户拒绝认领申请, 用户ID: {}, 申请ID: {}", currentUser.getId(), applicationId);
        
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("未授权操作，请先登录"));
        }
        
        try {
            ClaimApplicationDto application = claimApplicationService.rejectClaimApplication(
                    applicationId, currentUser.getId());
            
            log.debug("认领申请已拒绝, 申请ID: {}", application.getId());
            return ResponseEntity.ok(ApiResponse.success("已拒绝认领申请", application));
        } catch (ResourceNotFoundException e) {
            log.warn("拒绝认领申请失败, 找不到对应的申请: {}", applicationId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail(e.getMessage()));
        } catch (BadRequestException e) {
            log.warn("拒绝认领申请失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.fail(e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.warn("无权拒绝认领申请: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.fail(e.getMessage()));
        } catch (Exception e) {
            log.error("拒绝认领申请时发生错误", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("拒绝认领申请失败：" + e.getMessage()));
        }
    }

    /**
     * 获取认领申请详情
     *
     * @param applicationId 认领申请ID
     * @return 认领申请详情
     */
    @GetMapping("/{applicationId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ClaimApplicationDto>> getApplicationDetail(
            @PathVariable Long applicationId,
            @CurrentUser UserDetailsImpl currentUser) {
        
        log.info("获取认领申请详情, 申请ID: {}, 用户ID: {}", applicationId, currentUser.getId());
        
        try {
            ClaimApplicationDto application = claimApplicationService.getClaimApplicationById(applicationId);
            
            log.debug("获取认领申请详情成功, 申请ID: {}", application.getId());
            return ResponseEntity.ok(ApiResponse.success("获取认领申请详情成功", application));
        } catch (ResourceNotFoundException e) {
            log.warn("获取认领申请详情失败, 找不到对应的申请: {}", applicationId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail(e.getMessage()));
        } catch (Exception e) {
            log.error("获取认领申请详情时发生错误", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("获取认领申请详情失败：" + e.getMessage()));
        }
    }

    /**
     * 获取当前用户发起的认领申请列表
     *
     * @param status     申请状态，可选（不传则查询所有状态）
     * @param page       页码，默认1
     * @param size       每页条数，默认10
     * @param currentUser 当前用户
     * @return 认领申请分页列表
     */
    @GetMapping("/my-applications")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ClaimPageDto>> getMyApplications(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @CurrentUser UserDetailsImpl currentUser) {
        
        log.info("获取用户发起的认领申请列表, 用户ID: {}, 状态: {}, 页码: {}, 每页条数: {}", 
                currentUser.getId(), status, page, size);
        
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("未授权操作，请先登录"));
        }
        
        try {
            ClaimPageDto pageDto = claimApplicationService.getApplicationsByApplicant(
                    currentUser.getId(), status, page, size);
            
            log.debug("获取用户认领申请列表成功, 用户ID: {}, 总条数: {}", 
                    currentUser.getId(), pageDto.getTotalItems());
            
            return ResponseEntity.ok(ApiResponse.success("获取我的认领申请列表成功", pageDto));
        } catch (Exception e) {
            log.error("获取用户认领申请列表时发生错误", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("获取认领申请列表失败：" + e.getMessage()));
        }
    }

    /**
     * 获取需要当前用户处理的认领申请列表
     *
     * @param status     申请状态，可选（不传则查询所有状态）
     * @param page       页码，默认1
     * @param size       每页条数，默认10
     * @param currentUser 当前用户
     * @return 认领申请分页列表
     */
    @GetMapping("/for-processing")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ClaimPageDto>> getApplicationsForProcessing(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @CurrentUser UserDetailsImpl currentUser) {
        
        log.info("获取需要处理的认领申请列表, 用户ID: {}, 状态: {}, 页码: {}, 每页条数: {}", 
                currentUser.getId(), status, page, size);
        
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("未授权操作，请先登录"));
        }
        
        try {
            ClaimPageDto pageDto = claimApplicationService.getApplicationsForProcessing(
                    currentUser.getId(), status, page, size);
            
            log.debug("获取待处理认领申请列表成功, 用户ID: {}, 总条数: {}", 
                    currentUser.getId(), pageDto.getTotalItems());
            
            return ResponseEntity.ok(ApiResponse.success("获取待处理认领申请列表成功", pageDto));
        } catch (Exception e) {
            log.error("获取待处理认领申请列表时发生错误", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("获取待处理认领申请列表失败：" + e.getMessage()));
        }
    }

    /**
     * 获取指定失物招领的认领申请列表
     *
     * @param foundItemId 失物招领ID
     * @param page        页码，默认1
     * @param size        每页条数，默认10
     * @param currentUser 当前用户
     * @return 认领申请分页列表
     */
    @GetMapping("/by-found-item/{foundItemId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ClaimPageDto>> getApplicationsByFoundItem(
            @PathVariable Long foundItemId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @CurrentUser UserDetailsImpl currentUser) {
        
        log.info("获取失物招领的认领申请列表, 失物招领ID: {}, 页码: {}, 每页条数: {}, 用户ID: {}", 
                foundItemId, page, size, currentUser.getId());
        
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("未授权操作，请先登录"));
        }
        
        try {
            ClaimPageDto pageDto = claimApplicationService.getApplicationsByFoundItem(
                    foundItemId, page, size);
            
            log.debug("获取失物招领认领申请列表成功, 失物招领ID: {}, 总条数: {}", 
                    foundItemId, pageDto.getTotalItems());
            
            return ResponseEntity.ok(ApiResponse.success("获取失物招领认领申请列表成功", pageDto));
        } catch (ResourceNotFoundException e) {
            log.warn("获取失物招领认领申请列表失败, 找不到对应的失物招领: {}", foundItemId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail(e.getMessage()));
        } catch (Exception e) {
            log.error("获取失物招领认领申请列表时发生错误", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("获取失物招领认领申请列表失败：" + e.getMessage()));
        }
    }

    /**
     * 获取所有认领申请列表（管理员用）
     *
     * @param status         申请状态，可选（不传则查询所有状态）
     * @param startDate      申请开始日期，可选，格式yyyy-MM-dd
     * @param endDate        申请结束日期，可选，格式yyyy-MM-dd
     * @param itemTitle      物品名称关键词，可选
     * @param applicantName  申请人姓名关键词，可选
     * @param page           页码，默认1
     * @param size           每页条数，默认10
     * @return 认领申请分页列表
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSADMIN')")
    public ResponseEntity<ApiResponse<ClaimPageDto>> getAllApplications(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String itemTitle,
            @RequestParam(required = false) String applicantName,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("管理员获取认领申请列表, 状态: {}, 日期范围: {} 至 {}, 物品名称: {}, 申请人: {}, 页码: {}, 每页条数: {}", 
                status, startDate, endDate, itemTitle, applicantName, page, size);
        
        try {
            ClaimPageDto pageDto = claimApplicationService.getAllApplications(
                    status, startDate, endDate, itemTitle, applicantName, page, size);
            
            log.debug("获取认领申请列表成功, 总条数: {}", pageDto.getTotalItems());
            
            return ResponseEntity.ok(ApiResponse.success("获取认领申请列表成功", pageDto));
        } catch (Exception e) {
            log.error("获取认领申请列表时发生错误", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("获取认领申请列表失败：" + e.getMessage()));
        }
    }

    /**
     * 删除认领申请（管理员用）
     * 
     * @param applicationId 认领申请ID
     * @return 操作结果
     */
    @DeleteMapping("/admin/{applicationId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteApplication(
            @PathVariable Long applicationId,
            @CurrentUser UserDetailsImpl currentUser) {
        
        log.info("管理员删除认领申请, 管理员ID: {}, 申请ID: {}", currentUser.getId(), applicationId);
        
        try {
            claimApplicationService.deleteClaimApplication(applicationId);
            
            log.debug("删除认领申请成功, 申请ID: {}", applicationId);
            
            return ResponseEntity.ok(ApiResponse.success("删除认领申请成功"));
        } catch (ResourceNotFoundException e) {
            log.warn("删除认领申请失败, 找不到对应的申请: {}", applicationId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail(e.getMessage()));
        } catch (Exception e) {
            log.error("删除认领申请时发生错误", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("删除认领申请失败：" + e.getMessage()));
        }
    }
} 