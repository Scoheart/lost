package com.community.lostandfound.dto.claim;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 认领申请请求DTO
 */
@Data
public class ClaimRequestDto {
    
    /**
     * 申请说明
     */
    @NotBlank(message = "申请说明不能为空")
    @Size(min = 10, max = 500, message = "申请说明长度应在10-500个字符之间")
    private String description;
    
    /**
     * 联系方式
     */
    private String contactInfo;
} 