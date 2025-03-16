package com.community.lostandfound.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 物品基础实体类 - 为LostItem和FoundItem提供共享属性和方法
 */
@Data
@Slf4j
public abstract class BaseItem {
    protected static final ObjectMapper objectMapper = new ObjectMapper();

    private Long id;
    private String title;
    private String description;
    private String category;
    private String images; // Stored as JSON string of image URLs
    private String contactInfo;
    private String status; // 'pending', 'found'/'claimed', 'closed'
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 用于响应的瞬态字段
    private String username;
    
    // JSON图片处理 - 由子类共用
    @JsonProperty("images")
    private List<String> imagesList = new ArrayList<>();
    
    @JsonProperty("images")
    public void setImagesList(List<String> imagesList) {
        try {
            this.images = objectMapper.writeValueAsString(imagesList);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize images list", e);
            this.images = "[]";
        }
    }
    
    @JsonIgnore
    public List<String> getImagesList() {
        if (images == null || images.isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            return objectMapper.readValue(images, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize images JSON", e);
            return new ArrayList<>();
        }
    }
    
    @JsonProperty("images") 
    public List<String> getImagesListForSerialization() {
        return getImagesList();
    }
} 