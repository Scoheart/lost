package com.community.lostandfound.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 寻物启事实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class LostItem extends BaseItem {
    // 特有字段
    private LocalDateTime lostDate;
    private String lostLocation;
    private BigDecimal reward;
    
    // 用于日期格式化的字段
    @JsonProperty("lostDate")
    private String lostDateStr;
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter ISO_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    /**
     * 获取用于JSON序列化的日期时间字符串
     * @return 格式化的日期时间字符串，如果日期为null则返回null
     */
    @JsonProperty("lostDate")
    public String getLostDateForSerialization() {
        return lostDate != null ? lostDate.format(DATE_TIME_FORMATTER) : null;
    }
    
    /**
     * 获取日期字符串
     * @return 格式化的日期字符串，如果日期为null则返回null
     */
    @JsonIgnore
    public String getLostDateStr() {
        return lostDate != null ? lostDate.format(DATE_TIME_FORMATTER) : null;
    }
    
    /**
     * 设置日期字符串，支持多种格式
     * @param dateStr 日期字符串，支持标准格式和ISO格式
     */
    @JsonProperty("lostDate")
    public void setLostDateStr(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            this.lostDateStr = null;
            return;
        }
        
        this.lostDateStr = dateStr;
        
        try {
            // 尝试解析日期
            if (dateStr.contains("T")) {
                // ISO格式 "yyyy-MM-ddTHH:mm:ss"
                this.lostDate = LocalDateTime.parse(dateStr);
            } else if (dateStr.contains(" ")) {
                // 标准格式 "yyyy-MM-dd HH:mm:ss"
                this.lostDate = LocalDateTime.parse(dateStr, DATE_TIME_FORMATTER);
            } else {
                // 仅日期格式 "yyyy-MM-dd"
                this.lostDate = LocalDateTime.parse(dateStr + "T00:00:00");
            }
        } catch (Exception e) {
            log.error("解析日期失败: {}", dateStr, e);
            // 保留原始字符串，让控制器处理解析异常
        }
    }
} 