package com.community.lostandfound.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 失物招领实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class FoundItem extends BaseItem {
    // 特有字段
    private LocalDateTime foundDate;
    private String foundLocation;
    private String storageLocation; // Storage location where the item is kept
    private String claimRequirements; // Requirements for claiming the item
    
    // 用于日期格式化的字段
    @JsonProperty("foundDate")
    private String foundDateStr;
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter ISO_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    /**
     * 获取用于JSON序列化的日期时间字符串
     * @return 格式化的日期时间字符串，如果日期为null则返回null
     */
    @JsonProperty("foundDate")
    public String getFoundDateForSerialization() {
        return foundDate != null ? foundDate.format(DATE_TIME_FORMATTER) : null;
    }
    
    /**
     * 获取日期字符串
     * @return 格式化的日期字符串，如果日期为null则返回null
     */
    @JsonIgnore
    public String getFoundDateStr() {
        return foundDate != null ? foundDate.format(DATE_TIME_FORMATTER) : null;
    }
    
    /**
     * 设置日期字符串，支持多种格式
     * @param dateStr 日期字符串，支持标准格式和ISO格式
     */
    @JsonProperty("foundDate")
    public void setFoundDateStr(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            this.foundDateStr = null;
            return;
        }
        
        this.foundDateStr = dateStr;
        
        try {
            // 尝试解析日期
            if (dateStr.contains("T")) {
                // ISO格式 "yyyy-MM-ddTHH:mm:ss"
                this.foundDate = LocalDateTime.parse(dateStr);
            } else if (dateStr.contains(" ")) {
                // 标准格式 "yyyy-MM-dd HH:mm:ss"
                this.foundDate = LocalDateTime.parse(dateStr, DATE_TIME_FORMATTER);
            } else {
                // 仅日期格式 "yyyy-MM-dd"
                this.foundDate = LocalDateTime.parse(dateStr + "T00:00:00");
            }
        } catch (Exception e) {
            log.error("解析日期失败: {}", dateStr, e);
            // 保留原始字符串，让控制器处理解析异常
        }
    }
} 