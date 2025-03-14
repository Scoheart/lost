package com.community.lostandfound.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class LostItem {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    private Long id;
    private String title;
    private String description;
    private LocalDate lostDate;
    private String lostLocation;
    private String category;
    private String images; // Stored as JSON string of image URLs
    private BigDecimal reward;
    private String contactInfo;
    private String status; // 'pending', 'found', 'closed'
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Transient fields for responses
    private String username;
    
    // 临时字段，用于接收前端日期字符串，不会持久化到数据库
    @JsonProperty("lostDate")
    private String lostDateStr;
    
    // 确保JSON序列化时使用lostDate而不是lostDateStr
    @JsonProperty("lostDate")
    public LocalDate getLostDateForSerialization() {
        return lostDate;
    }
    
    // 在JSON反序列化后清除临时字段
    @JsonIgnore
    public String getLostDateStr() {
        return lostDateStr;
    }
    
    // 临时字段，用于接收前端图片数组，不会持久化到数据库
    @JsonProperty("images")
    private List<String> imagesList = new ArrayList<>();
    
    // 在设置imagesList后，将其转换为JSON字符串并存储到images字段
    @JsonProperty("images")
    public void setImagesList(List<String> imagesList) {
        this.imagesList = imagesList;
        try {
            if (imagesList != null) {
                this.images = objectMapper.writeValueAsString(imagesList);
                log.debug("成功将图片数组转换为JSON字符串: {}", this.images);
            }
        } catch (JsonProcessingException e) {
            log.error("图片数组转换为JSON字符串失败", e);
            this.images = "[]";  // 转换失败时设置为空数组
        }
    }
    
    // 确保JSON序列化时使用imagesList而不是images
    @JsonProperty("images") 
    public List<String> getImagesListForSerialization() {
        if (images == null || images.isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            return objectMapper.readValue(images, List.class);
        } catch (JsonProcessingException e) {
            log.error("JSON字符串转换为图片数组失败", e);
            return new ArrayList<>();
        }
    }
} 