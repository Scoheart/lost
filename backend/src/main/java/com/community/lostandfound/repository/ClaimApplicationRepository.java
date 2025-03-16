package com.community.lostandfound.repository;

import com.community.lostandfound.entity.ClaimApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 认领申请数据访问接口
 */
@Mapper
public interface ClaimApplicationRepository {
    
    /**
     * 保存认领申请
     *
     * @param application 认领申请对象
     * @return 受影响的行数
     */
    int save(ClaimApplication application);
    
    /**
     * 更新认领申请
     *
     * @param application 认领申请对象
     * @return 受影响的行数
     */
    int update(ClaimApplication application);
    
    /**
     * 删除认领申请
     *
     * @param id 认领申请ID
     * @return 受影响的行数
     */
    int deleteById(Long id);
    
    /**
     * 查询所有认领申请（分页）
     *
     * @param offset    偏移量
     * @param pageSize  每页条数
     * @param status    状态（可选）
     * @return 认领申请列表
     */
    List<ClaimApplication> findAll(
            @Param("offset") int offset, 
            @Param("pageSize") int pageSize,
            @Param("status") String status);
    
    /**
     * 根据ID查询认领申请
     *
     * @param id 认领申请ID
     * @return 认领申请对象
     */
    ClaimApplication findById(Long id);
    
    /**
     * 统计认领申请总数
     *
     * @param status 状态（可选）
     * @return 认领申请总数
     */
    long count(@Param("status") String status);
    
    /**
     * 根据失物招领ID查询认领申请
     *
     * @param foundItemId 失物招领ID
     * @param offset      偏移量
     * @param pageSize    每页条数
     * @return 认领申请列表
     */
    List<ClaimApplication> findByFoundItemId(
            @Param("foundItemId") Long foundItemId,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize);
    
    /**
     * 统计指定失物招领的认领申请总数
     *
     * @param foundItemId 失物招领ID
     * @return 认领申请总数
     */
    long countByFoundItemId(@Param("foundItemId") Long foundItemId);
    
    /**
     * 根据用户ID查询该用户提交的认领申请（分页）
     *
     * @param userId   用户ID
     * @param offset   偏移量
     * @param pageSize 每页条数
     * @param status   状态（可选）
     * @return 认领申请列表
     */
    List<ClaimApplication> findByApplicantId(
            @Param("applicantId") Long userId,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize,
            @Param("status") String status);
    
    /**
     * 统计指定用户提交的认领申请总数
     *
     * @param userId 用户ID
     * @param status 状态（可选）
     * @return 认领申请总数
     */
    long countByApplicantId(
            @Param("applicantId") Long userId,
            @Param("status") String status);
    
    /**
     * 查询需要由用户处理的认领申请（分页）
     * 即用户发布的失物招领收到的认领申请
     *
     * @param userId   用户ID
     * @param offset   偏移量
     * @param pageSize 每页条数
     * @param status   状态（可选）
     * @return 认领申请列表
     */
    List<ClaimApplication> findByFoundItemOwnerId(
            @Param("ownerId") Long userId,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize,
            @Param("status") String status);
    
    /**
     * 统计指定用户需要处理的认领申请总数
     *
     * @param userId 用户ID
     * @param status 状态（可选）
     * @return 认领申请总数
     */
    long countByFoundItemOwnerId(
            @Param("ownerId") Long userId,
            @Param("status") String status);
    
    /**
     * 检查用户是否已经申请认领某个失物招领
     *
     * @param foundItemId 失物招领ID
     * @param applicantId 申请人ID
     * @return 如果存在申请返回true，否则返回false
     */
    boolean existsByFoundItemIdAndApplicantId(
            @Param("foundItemId") Long foundItemId,
            @Param("applicantId") Long applicantId);
    
    /**
     * 检查用户是否有指定状态的认领申请
     *
     * @param foundItemId 失物招领ID
     * @param applicantId 申请人ID
     * @param statusList 状态列表
     * @return 如果存在指定状态的申请返回true，否则返回false
     */
    boolean existsByFoundItemIdAndApplicantIdAndStatusIn(
            @Param("foundItemId") Long foundItemId,
            @Param("applicantId") Long applicantId,
            @Param("statusList") List<String> statusList);
    
    /**
     * 根据失物招领ID和状态查询认领申请
     *
     * @param foundItemId 失物招领ID
     * @param status      状态
     * @return 认领申请列表
     */
    List<ClaimApplication> findByFoundItemIdAndStatus(
            @Param("foundItemId") Long foundItemId,
            @Param("status") String status);
    
    /**
     * 检查失物招领是否有已批准的认领申请
     *
     * @param foundItemId 失物招领ID
     * @return 如果存在已批准的申请返回true，否则返回false
     */
    boolean hasApprovedApplication(@Param("foundItemId") Long foundItemId);
    
    /**
     * 查询所有认领申请并根据过滤条件筛选（分页）
     *
     * @param status         状态（可选）
     * @param startDateTime  开始日期时间（可选）
     * @param endDateTime    结束日期时间（可选）
     * @param itemTitle      物品名称关键词（可选）
     * @param applicantName  申请人姓名关键词（可选）
     * @param offset         偏移量
     * @param pageSize       每页条数
     * @return 认领申请列表
     */
    List<ClaimApplication> findAllWithFilters(
            @Param("status") String status,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            @Param("itemTitle") String itemTitle,
            @Param("applicantName") String applicantName,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize);
    
    /**
     * 根据过滤条件统计认领申请总数
     *
     * @param status         状态（可选）
     * @param startDateTime  开始日期时间（可选）
     * @param endDateTime    结束日期时间（可选）
     * @param itemTitle      物品名称关键词（可选）
     * @param applicantName  申请人姓名关键词（可选）
     * @return 认领申请总数
     */
    long countWithFilters(
            @Param("status") String status,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            @Param("itemTitle") String itemTitle,
            @Param("applicantName") String applicantName);
} 