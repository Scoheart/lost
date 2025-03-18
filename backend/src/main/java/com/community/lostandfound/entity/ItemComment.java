package com.community.lostandfound.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 物品留言实体类
 * 用于寻物启事和失物招领的留言
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ItemComment extends BaseComment {
    
    /**
     * 关联的物品ID（寻物启事或失物招领的ID）
     */
    private Long itemId;
    
    /**
     * 物品类型：lost - 寻物启事，found - 失物招领
     */
    private String itemType;
    
} 