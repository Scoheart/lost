package com.community.lostandfound.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

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
    private LocalDate foundDate;
    private String foundLocation;
    private String storageLocation; // Storage location where the item is kept
    private String claimRequirements; // Requirements for claiming the item
    
    // 用于日期格式化的字段
    @JsonProperty("foundDate")
    private String foundDateStr;
    
    @JsonProperty("foundDate")
    public LocalDate getFoundDateForSerialization() {
        return foundDate;
    }
    
    @JsonIgnore
    public String getFoundDateStr() {
        return foundDate != null ? foundDate.toString() : null;
    }
} 