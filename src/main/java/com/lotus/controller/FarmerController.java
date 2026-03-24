package com.lotus.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lotus.common.ResultVO;
import com.lotus.entity.LotusRootProduct;
import com.lotus.entity.OrderMain;
import com.lotus.entity.OrderItem;
import com.lotus.service.LotusRootProductService;
import com.lotus.service.OrderMainService;
import com.lotus.service.OrderItemService;
import com.lotus.service.TraceabilityInfoService;
import com.lotus.service.impl.AIServiceImpl;
import com.lotus.entity.TraceabilityInfo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.UUID;
import java.util.List;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import com.alibaba.fastjson.JSON;
import com.lotus.config.OSSConfig;
import com.aliyun.oss.OSS;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 农户控制器
 * 处理农户相关的商品管理、订单管理等请求
 * 角色要求：FARMER（农户）
 */
@Slf4j
@RestController
@RequestMapping("/farmer")
@RequiredArgsConstructor
@PreAuthorize("hasRole('FARMER')")
public class FarmerController {

    private final LotusRootProductService productService;
    private final OrderMainService orderService;
    private final OrderItemService orderItemService;
    private final TraceabilityInfoService traceabilityInfoService;
    private final OSS ossClient;
    private final OSSConfig ossConfig;
    private final AIServiceImpl aiService;

    /**
     * 获取农户商品列表（分页+条件查询）
     * @param page 当前页码
     * @param size 每页条数
     * @param farmerId 农户ID
     * @param status 商品状态
     * @return 分页商品列表
     */
    @GetMapping("/products")
    public ResultVO<IPage<LotusRootProduct>> getFarmerProducts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam Long farmerId,
            @RequestParam(required = false) String status) {
        
        log.info("获取农户商品列表: farmerId={}, page={}, size={}, status={}", farmerId, page, size, status);
        
        // 使用MyBatis-Plus分页插件
        Page<LotusRootProduct> pageInfo = new Page<>(page, size);
        
        // 使用LambdaQueryWrapper进行条件查询
        LambdaQueryWrapper<LotusRootProduct> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LotusRootProduct::getFarmerId, farmerId);
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq(LotusRootProduct::getStatus, status);
        }
        queryWrapper.orderByDesc(LotusRootProduct::getCreateTime);
        
        IPage<LotusRootProduct> result = productService.page(pageInfo, queryWrapper);
        
        return ResultVO.success(result);
    }

    /**
     * 获取农户商品统计
     * @param farmerId 农户ID
     * @return 商品统计信息
     */
    @GetMapping("/products/stats/{farmerId}")
    public ResultVO<Map<String, Object>> getProductStats(@PathVariable Long farmerId) {
        log.info("获取农户商品统计: farmerId={}", farmerId);
        
        Map<String, Object> stats = new HashMap<>();
        
        // 使用LambdaQueryWrapper统计总数
        LambdaQueryWrapper<LotusRootProduct> totalWrapper = new LambdaQueryWrapper<>();
        totalWrapper.eq(LotusRootProduct::getFarmerId, farmerId);
        stats.put("total", productService.count(totalWrapper));
        
        // 统计上架商品数
        LambdaQueryWrapper<LotusRootProduct> onSaleWrapper = new LambdaQueryWrapper<>();
        onSaleWrapper.eq(LotusRootProduct::getFarmerId, farmerId);
        onSaleWrapper.eq(LotusRootProduct::getStatus, "ON_SALE");
        stats.put("onSale", productService.count(onSaleWrapper));
        
        // 统计待审核商品数
        LambdaQueryWrapper<LotusRootProduct> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(LotusRootProduct::getFarmerId, farmerId);
        pendingWrapper.eq(LotusRootProduct::getStatus, "PENDING");
        stats.put("pending", productService.count(pendingWrapper));
        
        // 统计已拒绝商品数
        LambdaQueryWrapper<LotusRootProduct> rejectedWrapper = new LambdaQueryWrapper<>();
        rejectedWrapper.eq(LotusRootProduct::getFarmerId, farmerId);
        rejectedWrapper.eq(LotusRootProduct::getStatus, "REJECTED");
        stats.put("rejected", productService.count(rejectedWrapper));
        
        return ResultVO.success(stats);
    }

    /**
     * 添加商品
     * @param product 商品信息
     * @return 添加结果
     */
    @PostMapping("/products")
    public ResultVO<Void> addProduct(@RequestBody LotusRootProduct product) {
        log.info("添加商品: productName={}", product.getProductName());
        
        // 设置默认状态为待审核
        product.setStatus("PENDING");
        
        boolean success = productService.save(product);
        if (success) {
            return ResultVO.success("商品添加成功，等待审核");
        } else {
            return ResultVO.error("商品添加失败");
        }
    }

    /**
     * 更新商品
     * @param id 商品ID
     * @param product 商品信息
     * @return 更新结果
     */
    @PutMapping("/products/{id}")
    public ResultVO<Void> updateProduct(@PathVariable Long id, @RequestBody LotusRootProduct product) {
        log.info("更新商品: id={}", id);
        
        product.setId(id);
        // 更新时重置为待审核状态
        product.setStatus("PENDING");
        
        boolean success = productService.updateById(product);
        if (success) {
            return ResultVO.success("商品更新成功，等待审核");
        } else {
            return ResultVO.error("商品更新失败");
        }
    }

    /**
     * 更新商品状态（上架/下架）
     * @param id 商品ID
     * @param status 商品状态
     * @return 更新结果
     */
    @PutMapping("/products/{id}/status")
    public ResultVO<Void> updateProductStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        log.info("更新商品状态: id={}, status={}", id, status);
        
        LotusRootProduct product = productService.getById(id);
        if (product == null) {
            return ResultVO.notFound();
        }
        
        // 只有已审核通过的商品才能上下架
        if (!"APPROVED".equals(product.getStatus()) && !"ON_SALE".equals(product.getStatus()) && !"OFF_SALE".equals(product.getStatus())) {
            return ResultVO.error("只有已审核通过的商品才能上下架");
        }
        
        product.setStatus(status);
        boolean success = productService.updateById(product);
        
        if (success) {
            String message = "ON_SALE".equals(status) ? "商品上架成功" : "商品下架成功";
            return ResultVO.success(message);
        } else {
            return ResultVO.error("操作失败");
        }
    }
    
    /**
     * 更新商品库存（补货）
     * @param id 商品ID
     * @param quantity 补货数量
     * @return 更新结果
     */
    @PutMapping("/products/{id}/stock")
    public ResultVO<Void> updateProductStock(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> request) {
        log.info("更新商品库存: id={}, quantity={}", id, request.get("quantity"));
        
        LotusRootProduct product = productService.getById(id);
        if (product == null) {
            return ResultVO.notFound();
        }
        
        Integer quantity = request.get("quantity");
        if (quantity == null || quantity <= 0) {
            return ResultVO.error("请输入有效的补货数量");
        }
        
        // 增加库存
        product.setStock(product.getStock() + quantity);
        boolean success = productService.updateById(product);
        
        if (success) {
            return ResultVO.success("补货成功");
        } else {
            return ResultVO.error("补货失败");
        }
    }

    /**
     * 删除商品
     * @param id 商品ID
     * @return 删除结果
     */
    @DeleteMapping("/products/{id}")
    public ResultVO<Void> deleteProduct(@PathVariable Long id) {
        log.info("删除商品: id={}", id);
        
        boolean success = productService.removeById(id);
        if (success) {
            return ResultVO.success("商品删除成功");
        } else {
            return ResultVO.error("商品删除失败");
        }
    }

    /**
     * 获取农户订单列表（分页+条件查询）
     * @param page 当前页码
     * @param size 每页条数
     * @param farmerId 农户ID
     * @param status 订单状态
     * @return 分页订单列表
     */
    @GetMapping("/orders")
    public ResultVO<IPage<Map<String, Object>>> getFarmerOrders(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam Long farmerId,
            @RequestParam(required = false) String status) {
        
        log.info("获取农户订单列表: farmerId={}, page={}, size={}", farmerId, page, size);
        
        // 查询该农户的订单项
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getFarmerId, farmerId);
        List<OrderItem> orderItems = orderItemService.list(itemWrapper);
        
        if (orderItems.isEmpty()) {
            return ResultVO.success(new Page<>());
        }
        
        // 获取订单ID列表
        List<Long> orderIds = orderItems.stream()
                .map(OrderItem::getOrderId)
                .distinct()
                .toList();
        
        // 分页查询订单
        Page<OrderMain> pageInfo = new Page<>(page, size);
        LambdaQueryWrapper<OrderMain> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(OrderMain::getId, orderIds);
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq(OrderMain::getStatus, status);
        }
        queryWrapper.orderByDesc(OrderMain::getCreateTime);
        
        IPage<OrderMain> result = orderService.page(pageInfo, queryWrapper);
        
        // 转换为包含订单项的订单列表
        List<Map<String, Object>> orderList = new ArrayList<>();
        for (OrderMain order : result.getRecords()) {
            Map<String, Object> orderMap = new HashMap<>();
            orderMap.put("id", order.getId());
            orderMap.put("orderNo", order.getOrderNo());
            orderMap.put("userId", order.getUserId());
            orderMap.put("addressId", order.getAddressId());
            orderMap.put("receiverName", order.getReceiverName());
            orderMap.put("receiverPhone", order.getReceiverPhone());
            orderMap.put("receiverAddress", order.getReceiverAddress());
            orderMap.put("totalAmount", order.getTotalAmount());
            orderMap.put("status", order.getStatus());
            orderMap.put("paymentTime", order.getPaymentTime());
            orderMap.put("shippingTime", order.getShippingTime());
            orderMap.put("deliveryTime", order.getDeliveryTime());
            orderMap.put("createTime", order.getCreateTime());
            orderMap.put("updateTime", order.getUpdateTime());
            
            // 获取该农户相关的订单项
            LambdaQueryWrapper<OrderItem> farmerItemWrapper = new LambdaQueryWrapper<>();
            farmerItemWrapper.eq(OrderItem::getOrderId, order.getId());
            farmerItemWrapper.eq(OrderItem::getFarmerId, farmerId);
            List<OrderItem> farmerItems = orderItemService.list(farmerItemWrapper);
            
            // 为订单项添加商品图片信息
            List<Map<String, Object>> orderItemsWithImage = new ArrayList<>();
            for (OrderItem item : farmerItems) {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("id", item.getId());
                itemMap.put("orderId", item.getOrderId());
                itemMap.put("productId", item.getProductId());
                itemMap.put("farmerId", item.getFarmerId());
                itemMap.put("productName", item.getProductName());
                itemMap.put("price", item.getPrice());
                itemMap.put("quantity", item.getQuantity());
                itemMap.put("subtotal", item.getSubtotal());
                itemMap.put("createTime", item.getCreateTime());
                
                // 获取商品图片
                LotusRootProduct product = productService.getById(item.getProductId());
                if (product != null) {
                    itemMap.put("productImage", product.getImageUrl());
                }
                
                orderItemsWithImage.add(itemMap);
            }
            
            orderMap.put("orderItems", orderItemsWithImage);
            
            orderList.add(orderMap);
        }
        
        // 构建新的分页结果
        Page<Map<String, Object>> newPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        newPage.setRecords(orderList);
        
        return ResultVO.success(newPage);
    }

    /**
     * 获取最近订单
     * @param farmerId 农户ID
     * @param limit 数量限制
     * @return 最近订单列表
     */
    @GetMapping("/orders/recent/{farmerId}")
    public ResultVO<List<OrderMain>> getRecentOrders(
            @PathVariable Long farmerId,
            @RequestParam(defaultValue = "5") Integer limit) {
        
        log.info("获取最近订单: farmerId={}, limit={}", farmerId, limit);
        
        // 查询该农户的订单项
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getFarmerId, farmerId);
        List<OrderItem> orderItems = orderItemService.list(itemWrapper);
        
        if (orderItems.isEmpty()) {
            return ResultVO.success(List.of());
        }
        
        // 获取订单ID列表
        List<Long> orderIds = orderItems.stream()
                .map(OrderItem::getOrderId)
                .distinct()
                .toList();
        
        // 查询最近的订单
        LambdaQueryWrapper<OrderMain> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(OrderMain::getId, orderIds);
        queryWrapper.orderByDesc(OrderMain::getCreateTime);
        queryWrapper.last("LIMIT " + limit);
        
        List<OrderMain> orders = orderService.list(queryWrapper);
        return ResultVO.success(orders);
    }
    
    /**
     * 获取订单详情
     * @param orderId 订单ID
     * @return 订单详情
     */
    @GetMapping("/order/{orderId}")
    public ResultVO<Map<String, Object>> getOrderDetail(@PathVariable Long orderId) {
        log.info("获取订单详情: orderId={}", orderId);
        
        // 查询订单
        OrderMain order = orderService.getById(orderId);
        if (order == null) {
            return ResultVO.notFound();
        }
        
        // 构建订单详情
        Map<String, Object> orderMap = new HashMap<>();
        orderMap.put("id", order.getId());
        orderMap.put("orderNo", order.getOrderNo());
        orderMap.put("userId", order.getUserId());
        orderMap.put("addressId", order.getAddressId());
        orderMap.put("receiverName", order.getReceiverName());
        orderMap.put("receiverPhone", order.getReceiverPhone());
        orderMap.put("receiverAddress", order.getReceiverAddress());
        orderMap.put("totalAmount", order.getTotalAmount());
        orderMap.put("status", order.getStatus());
        orderMap.put("paymentTime", order.getPaymentTime());
        orderMap.put("shippingTime", order.getShippingTime());
        orderMap.put("deliveryTime", order.getDeliveryTime());
        orderMap.put("createTime", order.getCreateTime());
        orderMap.put("updateTime", order.getUpdateTime());
        
        // 获取订单项
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> orderItems = orderItemService.list(itemWrapper);
        
        // 为订单项添加商品图片信息
        List<Map<String, Object>> orderItemsWithImage = new ArrayList<>();
        for (OrderItem item : orderItems) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("id", item.getId());
            itemMap.put("orderId", item.getOrderId());
            itemMap.put("productId", item.getProductId());
            itemMap.put("farmerId", item.getFarmerId());
            itemMap.put("productName", item.getProductName());
            itemMap.put("price", item.getPrice());
            itemMap.put("quantity", item.getQuantity());
            itemMap.put("subtotal", item.getSubtotal());
            itemMap.put("createTime", item.getCreateTime());
            
            // 获取商品图片
            LotusRootProduct product = productService.getById(item.getProductId());
            if (product != null) {
                itemMap.put("productImage", product.getImageUrl());
            }
            
            orderItemsWithImage.add(itemMap);
        }
        
        orderMap.put("orderItems", orderItemsWithImage);
        
        return ResultVO.success(orderMap);
    }
    
    /**
     * 订单发货
     * @param orderId 订单ID
     * @param request 发货信息
     * @return 发货结果
     */
    @PostMapping("/order/{orderId}/ship")
    public ResultVO<Void> shipOrder(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> request) {
        log.info("订单发货: orderId={}", orderId);
        
        // 查询订单
        OrderMain order = orderService.getById(orderId);
        if (order == null) {
            return ResultVO.notFound();
        }
        
        // 检查订单状态
        if (!"PAID".equals(order.getStatus())) {
            return ResultVO.error("只有已支付的订单才能发货");
        }
        
        // 更新订单状态
        order.setStatus("SHIPPED");
        order.setShippingTime(LocalDateTime.now());
        
        // 保存订单
        boolean success = orderService.updateById(order);
        
        if (success) {
            return ResultVO.success("发货成功");
        } else {
            return ResultVO.error("发货失败");
        }
    }

    /**
     * 发货
     * @param orderId 订单ID
     * @return 发货结果
     */
    @PutMapping("/orders/{orderId}/ship")
    public ResultVO<Void> shipOrder(@PathVariable Long orderId) {
        log.info("发货: orderId={}", orderId);
        
        OrderMain order = orderService.getById(orderId);
        if (order == null) {
            return ResultVO.notFound();
        }
        
        if (!"PAID".equals(order.getStatus())) {
            return ResultVO.error("只有已支付的订单才能发货");
        }
        
        order.setStatus("SHIPPED");
        order.setShippingTime(LocalDateTime.now());
        boolean success = orderService.updateById(order);
        
        if (success) {
            return ResultVO.success("发货成功");
        } else {
            return ResultVO.error("发货失败");
        }
    }

    /**
     * 获取溯源信息列表（分页+条件查询）
     * @param page 当前页码
     * @param size 每页条数
     * @param productId 商品ID
     * @return 分页溯源信息列表
     */
    @GetMapping("/traceability")
    public ResultVO<IPage<Map<String, Object>>> getTraceabilityList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long productId) {
        
        log.info("获取溯源信息列表: page={}, size={}, productId={}", page, size, productId);
        
        // 使用MyBatis-Plus分页插件
        Page<TraceabilityInfo> pageInfo = new Page<>(page, size);
        
        // 使用LambdaQueryWrapper进行条件查询
        LambdaQueryWrapper<TraceabilityInfo> queryWrapper = new LambdaQueryWrapper<>();
        if (productId != null) {
            queryWrapper.eq(TraceabilityInfo::getProductId, productId);
        }
        queryWrapper.orderByDesc(TraceabilityInfo::getCreateTime);
        
        IPage<TraceabilityInfo> result = traceabilityInfoService.page(pageInfo, queryWrapper);
        
        // 转换为包含图片数组的结果
        List<Map<String, Object>> records = result.getRecords().stream().map(info -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", info.getId());
            map.put("productId", info.getProductId());
            map.put("seedDate", info.getSeedDate());
            map.put("fertilizerRecord", info.getFertilizerRecord());
            map.put("pesticideRecord", info.getPesticideRecord());
            map.put("harvestDate", info.getHarvestDate());
            map.put("inspectionReport", info.getInspectionReport());
            map.put("createTime", info.getCreateTime());
            map.put("updateTime", info.getUpdateTime());
            
            // 处理图片数组
            if (info.getImages() != null) {
                try {
                    String imagesStr = info.getImages();
                    log.debug("Images string: {}", imagesStr);
                    List<String> images = JSON.parseArray(imagesStr, String.class);
                    log.debug("Parsed images: {}", images);
                    // 过滤掉null值和空字符串
                    List<String> filteredImages = images.stream()
                            .filter(img -> img != null && !img.isEmpty())
                            .collect(java.util.stream.Collectors.toList());
                    log.debug("Filtered images: {}", filteredImages);
                    map.put("images", filteredImages);
                } catch (Exception e) {
                    log.error("解析图片数组失败: {}", e.getMessage());
                    map.put("images", java.util.Collections.emptyList());
                }
            } else {
                map.put("images", java.util.Collections.emptyList());
            }
            
            return map;
        }).collect(java.util.stream.Collectors.toList());
        
        // 构建新的分页结果
        Page<Map<String, Object>> newPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        newPage.setRecords(records);
        
        return ResultVO.success(newPage);
    }

    /**
     * 添加溯源信息
     * @param info 溯源信息
     * @return 添加结果
     */
    @PostMapping("/traceability")
    public ResultVO<Void> addTraceability(@RequestBody Map<String, Object> info) {
        log.info("添加溯源信息: productId={}", info.get("productId"));
        
        try {
            TraceabilityInfo traceabilityInfo = new TraceabilityInfo();
            // 处理productId类型转换
            Object productIdObj = info.get("productId");
            if (productIdObj != null) {
                if (productIdObj instanceof String) {
                    traceabilityInfo.setProductId(Long.parseLong((String) productIdObj));
                } else if (productIdObj instanceof Number) {
                    traceabilityInfo.setProductId(((Number) productIdObj).longValue());
                }
            }
            
            // 处理日期字符串转换
            if (info.get("seedDate") != null) {
                traceabilityInfo.setSeedDate(java.time.LocalDate.parse((String) info.get("seedDate")));
            }
            
            traceabilityInfo.setFertilizerRecord((String) info.get("fertilizerRecord"));
            traceabilityInfo.setPesticideRecord((String) info.get("pesticideRecord"));
            
            // 处理日期字符串转换
            if (info.get("harvestDate") != null) {
                traceabilityInfo.setHarvestDate(java.time.LocalDate.parse((String) info.get("harvestDate")));
            }
            
            traceabilityInfo.setInspectionReport((String) info.get("inspectionReport"));
            
            // 处理图片数组
            if (info.containsKey("images")) {
                Object imagesObj = info.get("images");
                if (imagesObj instanceof List) {
                    List<String> images = (List<String>) imagesObj;
                    // 过滤掉null值
                    List<String> filteredImages = images.stream()
                            .filter(img -> img != null && !img.isEmpty())
                            .collect(java.util.stream.Collectors.toList());
                    traceabilityInfo.setImages(JSON.toJSONString(filteredImages));
                } else if (imagesObj instanceof String) {
                    // 处理字符串形式的图片数组
                    traceabilityInfo.setImages((String) imagesObj);
                }
            }
            
            boolean success = traceabilityInfoService.save(traceabilityInfo);
            if (success) {
                return ResultVO.success("溯源信息添加成功");
            } else {
                return ResultVO.error("溯源信息添加失败");
            }
        } catch (Exception e) {
            log.error("添加溯源信息失败", e);
            return ResultVO.error("溯源信息添加失败");
        }
    }

    /**
     * 更新溯源信息
     * @param id 溯源信息ID
     * @param info 溯源信息
     * @return 更新结果
     */
    @PutMapping("/traceability/{id}")
    public ResultVO<Void> updateTraceability(@PathVariable Long id, @RequestBody Map<String, Object> info) {
        log.info("更新溯源信息: id={}", id);
        
        try {
            TraceabilityInfo traceabilityInfo = traceabilityInfoService.getById(id);
            if (traceabilityInfo == null) {
                return ResultVO.notFound();
            }
            
            // 处理productId类型转换
            Object productIdObj = info.get("productId");
            if (productIdObj != null) {
                if (productIdObj instanceof String) {
                    traceabilityInfo.setProductId(Long.parseLong((String) productIdObj));
                } else if (productIdObj instanceof Number) {
                    traceabilityInfo.setProductId(((Number) productIdObj).longValue());
                }
            }
            
            // 处理日期字符串转换
            if (info.get("seedDate") != null) {
                traceabilityInfo.setSeedDate(java.time.LocalDate.parse((String) info.get("seedDate")));
            }
            
            traceabilityInfo.setFertilizerRecord((String) info.get("fertilizerRecord"));
            traceabilityInfo.setPesticideRecord((String) info.get("pesticideRecord"));
            
            // 处理日期字符串转换
            if (info.get("harvestDate") != null) {
                traceabilityInfo.setHarvestDate(java.time.LocalDate.parse((String) info.get("harvestDate")));
            }
            
            traceabilityInfo.setInspectionReport((String) info.get("inspectionReport"));
            
            // 处理图片数组
            if (info.containsKey("images")) {
                Object imagesObj = info.get("images");
                if (imagesObj instanceof List) {
                    List<String> images = (List<String>) imagesObj;
                    // 过滤掉null值
                    List<String> filteredImages = images.stream()
                            .filter(img -> img != null && !img.isEmpty())
                            .collect(java.util.stream.Collectors.toList());
                    traceabilityInfo.setImages(JSON.toJSONString(filteredImages));
                } else if (imagesObj instanceof String) {
                    // 处理字符串形式的图片数组
                    traceabilityInfo.setImages((String) imagesObj);
                }
            }
            
            boolean success = traceabilityInfoService.updateById(traceabilityInfo);
            if (success) {
                return ResultVO.success("溯源信息更新成功");
            } else {
                return ResultVO.error("溯源信息更新失败");
            }
        } catch (Exception e) {
            log.error("更新溯源信息失败", e);
            return ResultVO.error("溯源信息更新失败");
        }
    }

    /**
     * 删除溯源信息
     * @param id 溯源信息ID
     * @return 删除结果
     */
    @DeleteMapping("/traceability/{id}")
    public ResultVO<Void> deleteTraceability(@PathVariable Long id) {
        log.info("删除溯源信息: id={}", id);
        
        boolean success = traceabilityInfoService.removeById(id);
        if (success) {
            return ResultVO.success("溯源信息删除成功");
        } else {
            return ResultVO.error("溯源信息删除失败");
        }
    }

    /**
     * 上传图片
     * @param file 图片文件
     * @return 上传结果
     */
    @PostMapping("/upload")
    public Object uploadImage(@RequestParam("file") MultipartFile file) {
        log.info("上传图片: fileName={}", file.getOriginalFilename());
        
        try {
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = "traceability/" + UUID.randomUUID() + suffix;
            
            // 上传到OSS
            ossClient.putObject(ossConfig.getBucketName(), fileName, file.getInputStream());
            
            // 生成访问URL
            String url = ossConfig.getDomain() + "/" + fileName;
            log.info("图片上传成功: url={}", url);
            
            // 直接返回图片URL，与前端期望的格式一致
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("message", url);
            result.put("date", null);
            return result;
        } catch (Exception e) {
            log.error("上传图片失败", e);
            // 抛出异常，让前端的upload-error事件处理
            throw new RuntimeException("上传图片失败");
        }
    }
    
    /**
     * 获取农户销售趋势统计
     * @param farmerId 农户ID
     * @param days 天数
     * @return 销售趋势数据
     */
    @GetMapping("/statistics/sales")
    public ResultVO<Map<String, Object>> getFarmerSalesStatistics(
            @RequestParam Long farmerId,
            @RequestParam(defaultValue = "30") Integer days) {
        log.info("获取农户销售趋势统计: farmerId={}, days={}", farmerId, days);
        
        // 从数据库查询真实销售数据
        List<String> dates = new ArrayList<>();
        List<BigDecimal> amounts = new ArrayList<>();
        List<Long> orderCounts = new ArrayList<>();
        
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDateTime.now().minusDays(i).toLocalDate();
            dates.add(date.toString());
            
            // 查询当天的销售额
            LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.eq(OrderItem::getFarmerId, farmerId);
            itemWrapper.ge(OrderItem::getCreateTime, date.atStartOfDay());
            itemWrapper.lt(OrderItem::getCreateTime, date.plusDays(1).atStartOfDay());
            
            List<OrderItem> items = orderItemService.list(itemWrapper);
            BigDecimal dayAmount = BigDecimal.ZERO;
            for (OrderItem item : items) {
                dayAmount = dayAmount.add(item.getSubtotal());
            }
            amounts.add(dayAmount);
            
            // 统计订单数
            Set<Long> orderIds = new HashSet<>();
            for (OrderItem item : items) {
                orderIds.add(item.getOrderId());
            }
            orderCounts.add((long) orderIds.size());
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("dates", dates);
        result.put("amounts", amounts);
        result.put("orderCounts", orderCounts);
        
        return ResultVO.success(result);
    }
    
    /**
     * 获取农户商品销量排行
     * @param farmerId 农户ID
     * @param limit 数量限制
     * @return 商品销量排行数据
     */
    @GetMapping("/statistics/product-sales")
    public ResultVO<Map<String, Object>> getFarmerProductSales(
            @RequestParam Long farmerId,
            @RequestParam(defaultValue = "10") Integer limit) {
        log.info("获取农户商品销量排行: farmerId={}, limit={}", farmerId, limit);
        
        // 查询农户的所有订单项
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getFarmerId, farmerId);
        List<OrderItem> items = orderItemService.list(itemWrapper);
        
        // 按商品ID分组统计销量
        Map<Long, Long> productSalesMap = new HashMap<>();
        Map<Long, String> productNameMap = new HashMap<>();
        
        for (OrderItem item : items) {
            Long productId = item.getProductId();
            productSalesMap.put(productId, productSalesMap.getOrDefault(productId, 0L) + item.getQuantity());
            productNameMap.put(productId, item.getProductName());
        }
        
        // 按销量排序
        List<Map.Entry<Long, Long>> sortedEntries = productSalesMap.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toList());
        
        List<String> productNames = new ArrayList<>();
        List<Long> sales = new ArrayList<>();
        
        for (Map.Entry<Long, Long> entry : sortedEntries) {
            productNames.add(productNameMap.getOrDefault(entry.getKey(), "未知商品"));
            sales.add(entry.getValue());
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("productNames", productNames);
        result.put("sales", sales);
        
        return ResultVO.success(result);
    }

    /**
     * AI智能问答
     * @param question 问题
     * @return 回答
     */
    @PostMapping("/ai/chat")
    public ResultVO<String> aiChat(@RequestBody Map<String, String> request) {
        String question = request.get("question");
        log.info("AI智能问答: question={}", question);
        
        if (question == null || question.isEmpty()) {
            return ResultVO.error("请输入问题");
        }
        
        String answer = aiService.chat(question);
        return ResultVO.success(answer);
    }

    /**
     * AI智能问答（流式）
     * @param question 问题
     * @return 流式回答
     */
    @PostMapping("/ai/chat/stream")
    public void aiChatStream(@RequestBody Map<String, String> request, jakarta.servlet.http.HttpServletResponse response) {
        String question = request.get("question");
        log.info("AI智能问答（流式）: question={}", question);
        
        if (question == null || question.isEmpty()) {
            try {
                response.setContentType("text/event-stream");
                response.setCharacterEncoding("UTF-8");
                response.setHeader("Cache-Control", "no-cache");
                response.setHeader("Connection", "keep-alive");
                java.io.PrintWriter writer = response.getWriter();
                writer.write("data: 请输入问题\n\n");
                writer.flush();
                writer.close();
            } catch (Exception e) {
                log.error("Error writing response: {}", e.getMessage());
            }
            return;
        }
        
        try {
            response.setContentType("text/event-stream");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Connection", "keep-alive");
            final java.io.PrintWriter writer = response.getWriter();
            
            aiService.doubaoStreamChat(question, chunk -> {
                try {
                    writer.write("data: " + chunk + "\n\n");
                    writer.flush();
                } catch (Exception e) {
                    log.error("Error writing chunk: {}", e.getMessage());
                }
            });
            
            writer.write("data: [DONE]\n\n");
            writer.flush();
            writer.close();
        } catch (Exception e) {
            log.error("Error in streaming response: {}", e.getMessage());
        }
    }

    /**
     * AI种植建议
     * @param request 请求参数
     * @return 种植建议
     */
    @PostMapping("/ai/planting-suggestion")
    public ResultVO<String> aiPlantingSuggestion(@RequestBody Map<String, String> request) {
        String crop = request.get("crop");
        String soilType = request.get("soilType");
        String season = request.get("season");
        
        log.info("AI种植建议: crop={}, soilType={}, season={}", crop, soilType, season);
        
        StringBuilder question = new StringBuilder("请为我提供关于");
        if (crop != null && !crop.isEmpty()) {
            question.append(crop);
        } else {
            question.append("莲藕");
        }
        question.append("的种植建议");
        if (soilType != null && !soilType.isEmpty()) {
            question.append("，土壤类型是").append(soilType);
        }
        if (season != null && !season.isEmpty()) {
            question.append("，季节是").append(season);
        }
        question.append("。请包括种植时间、土壤准备、种植方法、施肥管理、病虫害防治等方面的详细建议。");
        
        String answer = aiService.chat(question.toString());
        return ResultVO.success(answer);
    }

    /**
     * AI种植建议（流式）
     * @param request 请求参数
     * @return 流式种植建议
     */
    @PostMapping("/ai/planting-suggestion/stream")
    public void aiPlantingSuggestionStream(@RequestBody Map<String, String> request, jakarta.servlet.http.HttpServletResponse response) {
        String crop = request.get("crop");
        String soilType = request.get("soilType");
        String season = request.get("season");
        
        log.info("AI种植建议（流式）: crop={}, soilType={}, season={}", crop, soilType, season);
        
        StringBuilder question = new StringBuilder("请为我提供关于");
        if (crop != null && !crop.isEmpty()) {
            question.append(crop);
        } else {
            question.append("莲藕");
        }
        question.append("的种植建议");
        if (soilType != null && !soilType.isEmpty()) {
            question.append("，土壤类型是").append(soilType);
        }
        if (season != null && !season.isEmpty()) {
            question.append("，季节是").append(season);
        }
        question.append("。请包括种植时间、土壤准备、种植方法、施肥管理、病虫害防治等方面的详细建议。");
        
        try {
            response.setContentType("text/event-stream");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Connection", "keep-alive");
            final java.io.PrintWriter writer = response.getWriter();
            
            aiService.doubaoStreamChat(question.toString(), chunk -> {
                try {
                    writer.write("data: " + chunk + "\n\n");
                    writer.flush();
                } catch (Exception e) {
                    log.error("Error writing chunk: {}", e.getMessage());
                }
            });
            
            writer.write("data: [DONE]\n\n");
            writer.flush();
            writer.close();
        } catch (Exception e) {
            log.error("Error in streaming response: {}", e.getMessage());
        }
    }

    /**
     * AI销售建议
     * @param request 请求参数
     * @return 销售建议
     */
    @PostMapping("/ai/sales-suggestion")
    public ResultVO<String> aiSalesSuggestion(@RequestBody Map<String, String> request) {
        String product = request.get("product");
        String targetMarket = request.get("targetMarket");
        
        log.info("AI销售建议: product={}, targetMarket={}", product, targetMarket);
        
        StringBuilder question = new StringBuilder("请为我提供关于");
        if (product != null && !product.isEmpty()) {
            question.append(product);
        } else {
            question.append("莲藕产品");
        }
        question.append("的销售建议");
        if (targetMarket != null && !targetMarket.isEmpty()) {
            question.append("，目标市场是").append(targetMarket);
        }
        question.append("。请包括定价策略、促销活动、销售渠道、品牌建设等方面的详细建议。");
        
        String answer = aiService.chat(question.toString());
        return ResultVO.success(answer);
    }

    /**
     * AI销售建议（流式）
     * @param request 请求参数
     * @return 流式销售建议
     */
    @PostMapping("/ai/sales-suggestion/stream")
    public void aiSalesSuggestionStream(@RequestBody Map<String, String> request, jakarta.servlet.http.HttpServletResponse response) {
        String product = request.get("product");
        String targetMarket = request.get("targetMarket");
        
        log.info("AI销售建议（流式）: product={}, targetMarket={}", product, targetMarket);
        
        StringBuilder question = new StringBuilder("请为我提供关于");
        if (product != null && !product.isEmpty()) {
            question.append(product);
        } else {
            question.append("莲藕产品");
        }
        question.append("的销售建议");
        if (targetMarket != null && !targetMarket.isEmpty()) {
            question.append("，目标市场是").append(targetMarket);
        }
        question.append("。请包括定价策略、促销活动、销售渠道、品牌建设等方面的详细建议。");
        
        try {
            response.setContentType("text/event-stream");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Connection", "keep-alive");
            final java.io.PrintWriter writer = response.getWriter();
            
            aiService.doubaoStreamChat(question.toString(), chunk -> {
                try {
                    writer.write("data: " + chunk + "\n\n");
                    writer.flush();
                } catch (Exception e) {
                    log.error("Error writing chunk: {}", e.getMessage());
                }
            });
            
            writer.write("data: [DONE]\n\n");
            writer.flush();
            writer.close();
        } catch (Exception e) {
            log.error("Error in streaming response: {}", e.getMessage());
        }
    }
}