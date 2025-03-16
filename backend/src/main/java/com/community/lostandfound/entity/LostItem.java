package com.community.lostandfound.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;

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
    private LocalDate lostDate;
    private String lostLocation;
    private BigDecimal reward;
    
    // 用于日期格式化的字段
    @JsonProperty("lostDate")
    private String lostDateStr;
    
    @JsonProperty("lostDate")
    public LocalDate getLostDateForSerialization() {
        return lostDate;
    }
    
    @JsonIgnore
    public String getLostDateStr() {
        return lostDate != null ? lostDate.toString() : null;
    }
} 