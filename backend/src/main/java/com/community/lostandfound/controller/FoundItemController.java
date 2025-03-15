package com.community.lostandfound.controller;

import com.community.lostandfound.dto.common.ApiResponse;
import com.community.lostandfound.entity.FoundItem;
import com.community.lostandfound.exception.ResourceNotFoundException;
import com.community.lostandfound.security.CurrentUser;
import com.community.lostandfound.security.UserDetailsImpl;
import com.community.lostandfound.service.FoundItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 失物招领控制器
 * 提供失物招领的发布、查询、更新和删除功能
 */
@Slf4j
@RestController
@RequestMapping("/found-items")
@RequiredArgsConstructor
public class FoundItemController {

    private final FoundItemService foundItemService;

    /**
     * 发布失物招领
     *
     * @param foundItem   失物招领信息
     * @param currentUser 当前用户
     * @return 创建的失物招领
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<FoundItem>> createFoundItem(
            @Valid @RequestBody FoundItem foundItem,
            @CurrentUser UserDetailsImpl currentUser) {
        
        log.info("发布失物招领: {}", foundItem.getTitle());
        
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("未授权操作，请先登录"));
        }
        
        // 处理日期格式
        if (foundItem.getFoundDate() == null && foundItem.getFoundDateStr() != null) {
            try {
                // 尝试从字符串中提取日期部分
                String dateStr = foundItem.getFoundDateStr();
                if (dateStr.contains(" ")) {
                    dateStr = dateStr.split(" ")[0];
                }
                foundItem.setFoundDate(LocalDate.parse(dateStr));
                log.info("成功解析日期: {}", dateStr);
            } catch (DateTimeParseException e) {
                log.error("日期解析失败: {}", foundItem.getFoundDateStr(), e);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.fail("日期格式不正确，请使用yyyy-MM-dd格式"));
            }
        }
        
        // 记录图片处理信息
        if (foundItem.getImagesList() != null && !foundItem.getImagesList().isEmpty()) {
            log.debug("接收到图片列表，数量: {}", foundItem.getImagesList().size());
        } else {
            log.debug("未接收到图片或图片列表为空");
        }
        
        foundItem.setUserId(currentUser.getId());
        foundItem.setStatus("pending");
        foundItem.setCreatedAt(LocalDateTime.now());
        foundItem.setUpdatedAt(LocalDateTime.now());
        
        FoundItem savedItem = foundItemService.createFoundItem(foundItem);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("失物招领发布成功", savedItem));
    }

    /**
     * 查询失物招领列表（支持筛选）
     *
     * @param category 物品分类
     * @param status   状态
     * @param keyword  关键词
     * @param page     页码
     * @param size     每页条数
     * @return 失物招领列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Object>> getAllFoundItems(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("查询失物招领列表, 分类: {}, 状态: {}, 关键词: {}, 页码: {}, 每页条数: {}", 
                category, status, keyword, page, size);
        
        // 页码转为偏移量，数据库中从0开始计算，前端从1开始
        int offset = (page - 1) * size;
        
        List<FoundItem> items = foundItemService.getAllFoundItems(category, status, keyword, offset, size);
        int total = foundItemService.countAllFoundItems(category, status, keyword);
        
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
        
        return ResponseEntity.ok(ApiResponse.success("查询失物招领列表成功", result));
    }

    /**
     * 根据ID查询失物招领
     *
     * @param id 失物招领ID
     * @return 失物招领信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FoundItem>> getFoundItemById(@PathVariable Long id) {
        log.info("查询失物招领, ID: {}", id);
        
        Optional<FoundItem> item = foundItemService.getFoundItemById(id);
        if (item.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success("查询失物招领成功", item.get()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail("失物招领不存在"));
        }
    }

    /**
     * 更新失物招领
     *
     * @param id         失物招领ID
     * @param foundItem  更新的失物招领信息
     * @param currentUser 当前用户
     * @return 更新后的失物招领
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<FoundItem>> updateFoundItem(
            @PathVariable Long id, 
            @Valid @RequestBody FoundItem foundItem,
            @CurrentUser UserDetailsImpl currentUser) {
        
        log.info("更新失物招领, ID: {}", id);
        
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("未授权操作，请先登录"));
        }
        
        // 处理日期格式
        if (foundItem.getFoundDate() == null && foundItem.getFoundDateStr() != null) {
            try {
                // 尝试从字符串中提取日期部分
                String dateStr = foundItem.getFoundDateStr();
                if (dateStr.contains(" ")) {
                    dateStr = dateStr.split(" ")[0];
                }
                foundItem.setFoundDate(LocalDate.parse(dateStr));
                log.info("成功解析日期: {}", dateStr);
            } catch (DateTimeParseException e) {
                log.error("日期解析失败: {}", foundItem.getFoundDateStr(), e);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.fail("日期格式不正确，请使用yyyy-MM-dd格式"));
            }
        }
        
        // 记录图片处理信息
        if (foundItem.getImagesList() != null && !foundItem.getImagesList().isEmpty()) {
            log.debug("更新图片列表，数量: {}", foundItem.getImagesList().size());
        } else {
            log.debug("未接收到图片或图片列表为空");
        }
        
        // 记录状态信息，帮助诊断问题
        log.debug("更新请求中的状态值: {}", foundItem.getStatus());
        
        try {
            foundItem.setId(id);
            foundItem.setUpdatedAt(LocalDateTime.now());
            
            FoundItem updatedItem = foundItemService.updateFoundItem(foundItem, currentUser.getId());
            log.debug("更新成功，最终状态值: {}", updatedItem.getStatus());
            
            return ResponseEntity.ok(ApiResponse.success("失物招领更新成功", updatedItem));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.fail(e.getMessage()));
        }
    }

    /**
     * 删除失物招领
     *
     * @param id         失物招领ID
     * @param currentUser 当前用户
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteFoundItem(
            @PathVariable Long id,
            @CurrentUser UserDetailsImpl currentUser) {
        
        log.info("删除失物招领, ID: {}", id);
        
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("未授权操作，请先登录"));
        }
        
        try {
            foundItemService.deleteFoundItem(id, currentUser.getId());
            return ResponseEntity.ok(ApiResponse.success("失物招领删除成功", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.fail(e.getMessage()));
        }
    }

    /**
     * 查询当前用户的失物招领
     *
     * @param status 物品状态筛选（可选）：pending, claimed, closed
     * @param page 页码
     * @param size 每页条数
     * @param sort 排序字段：createdAt, updatedAt（默认为createdAt）
     * @param direction 排序方向：desc, asc（默认为desc）
     * @param currentUser 当前用户
     * @return 当前用户的失物招领列表
     */
    @GetMapping("/my-posts")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Object>> getMyFoundItems(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            @CurrentUser UserDetailsImpl currentUser) {
        
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("未授权操作，请先登录"));
        }
        
        log.info("查询当前用户的失物招领, 用户ID: {}, 状态: {}, 页码: {}, 每页条数: {}, 排序: {} {}", 
                currentUser.getId(), status, page, size, sort, direction);
        
        // 验证排序字段和方向
        if (!("createdAt".equals(sort) || "updatedAt".equals(sort))) {
            sort = "createdAt";
        }
        if (!("asc".equals(direction) || "desc".equals(direction))) {
            direction = "desc";
        }
        
        // 页码转为偏移量，数据库中从0开始计算，前端从1开始
        int offset = (page - 1) * size;
        
        // 获取筛选后的数据
        List<FoundItem> items = foundItemService.getFoundItemsByUserId(currentUser.getId());
        
        // 根据状态筛选
        if (status != null && !status.isEmpty()) {
            items = items.stream()
                    .filter(item -> status.equals(item.getStatus()))
                    .collect(Collectors.toList());
        }
        
        // 排序
        Comparator<FoundItem> comparator;
        if ("createdAt".equals(sort)) {
            comparator = Comparator.comparing(FoundItem::getCreatedAt);
        } else {
            comparator = Comparator.comparing(FoundItem::getUpdatedAt);
        }
        // 如果是降序，反转比较器
        if ("desc".equals(direction)) {
            comparator = comparator.reversed();
        }
        items.sort(comparator);
        
        // 总记录数
        int totalItems = items.size();
        
        // 分页
        int totalPages = (totalItems + size - 1) / size;
        int fromIndex = Math.min(offset, totalItems);
        int toIndex = Math.min(fromIndex + size, totalItems);
        List<FoundItem> pagedItems = items.subList(fromIndex, toIndex);
        
        // 构造带分页信息的响应
        var result = new HashMap<String, Object>();
        result.put("items", pagedItems);
        result.put("currentPage", page);
        result.put("pageSize", size);
        result.put("totalItems", totalItems);
        result.put("totalPages", totalPages);
        result.put("sort", sort);
        result.put("direction", direction);
        
        return ResponseEntity.ok(ApiResponse.success("查询当前用户失物招领成功", result));
    }

    /**
     * 更新失物招领状态
     *
     * @param id           失物招领ID
     * @param statusUpdate 包含状态字段的请求体
     * @param currentUser  当前用户
     * @return 更新后的失物招领
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<FoundItem>> updateFoundItemStatus(
            @PathVariable Long id,
            @Valid @RequestBody Map<String, String> statusUpdate,
            @CurrentUser UserDetailsImpl currentUser) {
        
        log.info("更新失物招领状态, ID: {}, 新状态: {}", id, statusUpdate.get("status"));
        
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("未授权操作，请先登录"));
        }
        
        String status = statusUpdate.get("status");
        if (status == null || status.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.fail("状态字段不能为空"));
        }
        
        try {
            FoundItem updatedItem = foundItemService.updateFoundItemStatus(id, status, currentUser.getId());
            
            // 根据不同状态返回不同的成功消息
            String message;
            if ("claimed".equals(status)) {
                message = "物品已标记为已认领";
            } else if ("closed".equals(status)) {
                message = "失物招领已关闭";
            } else {
                message = "失物招领状态已更新";
            }
            
            return ResponseEntity.ok(ApiResponse.success(message, updatedItem));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.fail(e.getMessage()));
        }
    }
} 