package com.community.lostandfound.controller;

import com.community.lostandfound.dto.common.ApiResponse;
import com.community.lostandfound.entity.LostItem;
import com.community.lostandfound.exception.ResourceNotFoundException;
import com.community.lostandfound.security.CurrentUser;
import com.community.lostandfound.security.UserDetailsImpl;
import com.community.lostandfound.service.LostItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * 寻物启事控制器
 * 提供寻物启事的发布、查询、更新和删除功能
 */
@Slf4j
@RestController
@RequestMapping("/lost-items")
@RequiredArgsConstructor
public class LostItemController {

    private final LostItemService lostItemService;

    /**
     * 发布寻物启事
     *
     * @param lostItem    寻物启事信息
     * @param currentUser 当前用户
     * @return 创建的寻物启事
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LostItem>> createLostItem(
            @Valid @RequestBody LostItem lostItem,
            @CurrentUser UserDetailsImpl currentUser) {
        
        log.info("发布寻物启事: {}", lostItem.getTitle());
        
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("未授权操作，请先登录"));
        }
        
        // 处理日期格式
        if (lostItem.getLostDate() == null && lostItem.getLostDateStr() != null) {
            try {
                // 尝试从字符串中提取日期部分
                String dateStr = lostItem.getLostDateStr();
                if (dateStr.contains(" ")) {
                    dateStr = dateStr.split(" ")[0];
                }
                lostItem.setLostDate(LocalDate.parse(dateStr));
                log.info("成功解析日期: {}", dateStr);
            } catch (DateTimeParseException e) {
                log.error("日期解析失败: {}", lostItem.getLostDateStr(), e);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.fail("日期格式不正确，请使用yyyy-MM-dd格式"));
            }
        }
        
        // 记录图片处理信息
        if (lostItem.getImagesList() != null && !lostItem.getImagesList().isEmpty()) {
            log.debug("接收到图片列表，数量: {}", lostItem.getImagesList().size());
        } else {
            log.debug("未接收到图片或图片列表为空");
        }
        
        lostItem.setUserId(currentUser.getId());
        lostItem.setStatus("pending");
        lostItem.setCreatedAt(LocalDateTime.now());
        lostItem.setUpdatedAt(LocalDateTime.now());
        
        LostItem savedItem = lostItemService.createLostItem(lostItem);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("寻物启事发布成功", savedItem));
    }

    /**
     * 查询寻物启事列表（支持筛选）
     *
     * @param category 物品分类
     * @param status   状态
     * @param keyword  关键词
     * @param page     页码
     * @param size     每页条数
     * @return 寻物启事列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Object>> getAllLostItems(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("查询寻物启事列表, 分类: {}, 状态: {}, 关键词: {}, 页码: {}, 每页条数: {}", 
                category, status, keyword, page, size);
        
        // 页码转为偏移量，数据库中从0开始计算，前端从1开始
        int offset = (page - 1) * size;
        
        List<LostItem> items = lostItemService.getAllLostItems(category, status, keyword, offset, size);
        int total = lostItemService.countAllLostItems(category, status, keyword);
        
        // 计算总页数
        int totalPages = (total + size - 1) / size;
        
        // 构造带分页信息的响应
        var result = new HashMap<String, Object>();
        result.put("items", items);
        result.put("currentPage", page);
        result.put("pageSize", size);
        result.put("totalItems", total);
        result.put("totalPages", totalPages);
        
        // 记录日志
        log.debug("查询到{}条记录，共{}页", total, totalPages);
        
        return ResponseEntity.ok(ApiResponse.success("查询寻物启事列表成功", result));
    }

    /**
     * 根据ID查询寻物启事
     *
     * @param id 寻物启事ID
     * @return 寻物启事信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LostItem>> getLostItemById(@PathVariable Long id) {
        log.info("查询寻物启事, ID: {}", id);
        
        Optional<LostItem> item = lostItemService.getLostItemById(id);
        if (item.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success("查询寻物启事成功", item.get()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail("寻物启事不存在"));
        }
    }

    /**
     * 更新寻物启事
     *
     * @param id         寻物启事ID
     * @param lostItem   更新的寻物启事信息
     * @param currentUser 当前用户
     * @return 更新后的寻物启事
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LostItem>> updateLostItem(
            @PathVariable Long id, 
            @Valid @RequestBody LostItem lostItem,
            @CurrentUser UserDetailsImpl currentUser) {
        
        log.info("更新寻物启事, ID: {}", id);
        
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("未授权操作，请先登录"));
        }
        
        // 处理日期格式
        if (lostItem.getLostDate() == null && lostItem.getLostDateStr() != null) {
            try {
                // 尝试从字符串中提取日期部分
                String dateStr = lostItem.getLostDateStr();
                if (dateStr.contains(" ")) {
                    dateStr = dateStr.split(" ")[0];
                }
                lostItem.setLostDate(LocalDate.parse(dateStr));
                log.info("成功解析日期: {}", dateStr);
            } catch (DateTimeParseException e) {
                log.error("日期解析失败: {}", lostItem.getLostDateStr(), e);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.fail("日期格式不正确，请使用yyyy-MM-dd格式"));
            }
        }
        
        // 记录图片处理信息
        if (lostItem.getImagesList() != null && !lostItem.getImagesList().isEmpty()) {
            log.debug("更新图片列表，数量: {}", lostItem.getImagesList().size());
        } else {
            log.debug("未接收到图片或图片列表为空");
        }
        
        try {
            lostItem.setId(id);
            lostItem.setUpdatedAt(LocalDateTime.now());
            
            LostItem updatedItem = lostItemService.updateLostItem(lostItem, currentUser.getId());
            return ResponseEntity.ok(ApiResponse.success("寻物启事更新成功", updatedItem));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.fail(e.getMessage()));
        }
    }

    /**
     * 删除寻物启事
     *
     * @param id         寻物启事ID
     * @param currentUser 当前用户
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteLostItem(
            @PathVariable Long id,
            @CurrentUser UserDetailsImpl currentUser) {
        
        log.info("删除寻物启事, ID: {}", id);
        
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("未授权操作，请先登录"));
        }
        
        try {
            lostItemService.deleteLostItem(id, currentUser.getId());
            return ResponseEntity.ok(ApiResponse.success("寻物启事删除成功", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.fail(e.getMessage()));
        }
    }

    /**
     * 查询当前用户的寻物启事
     *
     * @param currentUser 当前用户
     * @return 当前用户的寻物启事列表
     */
    @GetMapping("/my-posts")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<LostItem>>> getMyLostItems(
            @CurrentUser UserDetailsImpl currentUser) {
        
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("未授权操作，请先登录"));
        }
        
        log.info("查询当前用户的寻物启事, 用户ID: {}", currentUser.getId());
        
        List<LostItem> items = lostItemService.getLostItemsByUserId(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("查询当前用户寻物启事成功", items));
    }
} 