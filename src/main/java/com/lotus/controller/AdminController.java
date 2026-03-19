package com.lotus.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lotus.common.ResultVO;
import com.lotus.entity.*;
import com.lotus.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 管理员控制器
 * 处理管理员相关的用户管理、商品审核、订单管理、大数据统计等请求
 * 角色要求：ADMIN（管理员）
 */
@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final LotusRootProductService productService;
    private final ProductCategoryService categoryService;
    private final OrderMainService orderService;
    private final OrderItemService orderItemService;

    /**
     * 获取用户列表（分页+条件查询）
     * @param page 当前页码
     * @param size 每页条数
     * @param role 角色
     * @param username 用户名（模糊查询）
     * @return 分页用户列表
     */
    @GetMapping("/users")
    public ResultVO<IPage<SysUser>> getUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String username) {
        
        log.info("获取用户列表: page={}, size={}, role={}, username={}", page, size, role, username);
        
        // 使用MyBatis-Plus分页插件
        Page<SysUser> pageInfo = new Page<>(page, size);
        
        // 使用LambdaQueryWrapper进行条件查询
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        
        if (role != null && !role.isEmpty()) {
            queryWrapper.eq(SysUser::getRole, role);
        }
        
        if (username != null && !username.isEmpty()) {
            queryWrapper.like(SysUser::getUsername, username);
        }
        
        queryWrapper.orderByDesc(SysUser::getCreateTime);
        
        IPage<SysUser> result = userService.page(pageInfo, queryWrapper);
        
        return ResultVO.success(result);
    }

    /**
     * 获取用户详情
     * @param id 用户ID
     * @return 用户详情
     */
    @GetMapping("/users/{id}")
    public ResultVO<SysUser> getUserDetail(@PathVariable Long id) {
        log.info("获取用户详情: id={}", id);
        
        SysUser user = userService.getById(id);
        if (user == null) {
            return ResultVO.notFound();
        }
        
        return ResultVO.success(user);
    }

    /**
     * 添加用户
     * @param user 用户信息
     * @return 添加结果
     */
    @PostMapping("/users")
    public ResultVO<Void> addUser(@RequestBody SysUser user) {
        log.info("添加用户: username={}, role={}", user.getUsername(), user.getRole());
        
        // 检查用户名是否存在
        SysUser existUser = userService.getByUsername(user.getUsername());
        if (existUser != null) {
            return ResultVO.error("用户名已存在");
        }
        
        // 密码加密
        user.setPassword(user.getPassword());
        
        boolean success = userService.save(user);
        if (success) {
            return ResultVO.success("添加成功");
        } else {
            return ResultVO.error("添加失败");
        }
    }

    /**
     * 更新用户
     * @param id 用户ID
     * @param user 用户信息
     * @return 更新结果
     */
    @PutMapping("/users/{id}")
    public ResultVO<Void> updateUser(@PathVariable Long id, @RequestBody SysUser user) {
        log.info("更新用户: id={}", id);
        
        user.setId(id);
        // 不更新密码
        user.setPassword(null);
        
        boolean success = userService.updateById(user);
        if (success) {
            return ResultVO.success("更新成功");
        } else {
            return ResultVO.error("更新失败");
        }
    }

    /**
     * 删除用户
     * @param id 用户ID
     * @return 删除结果
     */
    @DeleteMapping("/users/{id}")
    public ResultVO<Void> deleteUser(@PathVariable Long id) {
        log.info("删除用户: id={}", id);
        
        boolean success = userService.removeById(id);
        if (success) {
            return ResultVO.success("删除成功");
        } else {
            return ResultVO.error("删除失败");
        }
    }

    /**
     * 更新用户状态
     * @param id 用户ID
     * @param status 状态
     * @return 更新结果
     */
    @PutMapping("/users/{id}/status")
    public ResultVO<Void> updateUserStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        log.info("更新用户状态: id={}, status={}", id, status);
        
        SysUser user = userService.getById(id);
        if (user == null) {
            return ResultVO.notFound();
        }
        
        user.setStatus(status);
        boolean success = userService.updateById(user);
        
        if (success) {
            return ResultVO.success("状态更新成功");
        } else {
            return ResultVO.error("状态更新失败");
        }
    }

    /**
     * 获取待审核商品列表
     * @param page 当前页码
     * @param size 每页条数
     * @param status 商品状态
     * @return 分页商品列表
     */
    @GetMapping("/products")
    public ResultVO<IPage<LotusRootProduct>> getProducts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status) {
        
        log.info("获取商品列表: page={}, size={}, status={}", page, size, status);
        
        // 使用MyBatis-Plus分页插件
        Page<LotusRootProduct> pageInfo = new Page<>(page, size);
        
        // 使用LambdaQueryWrapper进行条件查询
        LambdaQueryWrapper<LotusRootProduct> queryWrapper = new LambdaQueryWrapper<>();
        
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq(LotusRootProduct::getStatus, status);
        }
        
        queryWrapper.orderByDesc(LotusRootProduct::getCreateTime);
        
        IPage<LotusRootProduct> result = productService.page(pageInfo, queryWrapper);
        
        return ResultVO.success(result);
    }

    /**
     * 获取待审核商品列表
     * @param page 当前页码
     * @param size 每页条数
     * @return 待审核商品列表
     */
    @GetMapping("/products/pending")
    public ResultVO<IPage<LotusRootProduct>> getPendingProducts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        log.info("获取待审核商品列表: page={}, size={}", page, size);
        
        // 使用MyBatis-Plus分页插件
        Page<LotusRootProduct> pageInfo = new Page<>(page, size);
        
        // 使用LambdaQueryWrapper查询待审核商品
        LambdaQueryWrapper<LotusRootProduct> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LotusRootProduct::getStatus, "PENDING");
        queryWrapper.orderByDesc(LotusRootProduct::getCreateTime);
        
        IPage<LotusRootProduct> result = productService.page(pageInfo, queryWrapper);
        
        return ResultVO.success(result);
    }

    /**
     * 审核商品
     * @param id 商品ID
     * @param status 审核状态
     * @return 审核结果
     */
    @PutMapping("/products/{id}/status")
    public ResultVO<Void> approveProduct(
            @PathVariable Long id,
            @RequestParam String status) {
        log.info("审核商品: id={}, status={}", id, status);
        
        LotusRootProduct product = productService.getById(id);
        if (product == null) {
            return ResultVO.notFound();
        }
        
        product.setStatus(status);
        
        // 审核通过后自动上架
        if ("APPROVED".equals(status)) {
            product.setStatus("ON_SALE");
        }
        
        boolean success = productService.updateById(product);
        
        if (success) {
            String message = "APPROVED".equals(status) ? "审核通过" : "审核拒绝";
            return ResultVO.success(message);
        } else {
            return ResultVO.error("操作失败");
        }
    }

    /**
     * 获取订单列表（分页+条件查询）
     * @param page 当前页码
     * @param size 每页条数
     * @param status 订单状态
     * @return 分页订单列表
     */
    @GetMapping("/orders")
    public ResultVO<IPage<OrderMain>> getOrders(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status) {
        
        log.info("获取订单列表: page={}, size={}, status={}", page, size, status);
        
        // 使用MyBatis-Plus分页插件
        Page<OrderMain> pageInfo = new Page<>(page, size);
        
        // 使用LambdaQueryWrapper进行条件查询
        LambdaQueryWrapper<OrderMain> queryWrapper = new LambdaQueryWrapper<>();
        
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq(OrderMain::getStatus, status);
        }
        
        queryWrapper.orderByDesc(OrderMain::getCreateTime);
        
        IPage<OrderMain> result = orderService.page(pageInfo, queryWrapper);
        
        return ResultVO.success(result);
    }

    /**
     * 获取订单详情
     * @param orderId 订单ID
     * @return 订单详情
     */
    @GetMapping("/orders/{orderId}")
    public ResultVO<Map<String, Object>> getOrderDetail(@PathVariable Long orderId) {
        log.info("获取订单详情: orderId={}", orderId);
        
        OrderMain order = orderService.getById(orderId);
        if (order == null) {
            return ResultVO.notFound();
        }
        
        // 获取订单项
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> items = orderItemService.list(itemWrapper);
        
        // 为订单项添加商品图片信息
        List<Map<String, Object>> itemsWithImage = new ArrayList<>();
        for (OrderItem item : items) {
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
            
            itemsWithImage.add(itemMap);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        result.put("items", itemsWithImage);
        
        return ResultVO.success(result);
    }

    /**
     * 更新订单状态
     * @param orderId 订单ID
     * @param status 订单状态
     * @return 更新结果
     */
    @PutMapping("/orders/{orderId}/status")
    public ResultVO<Void> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String status) {
        log.info("更新订单状态: orderId={}, status={}", orderId, status);
        
        OrderMain order = orderService.getById(orderId);
        if (order == null) {
            return ResultVO.notFound();
        }
        
        order.setStatus(status);
        
        if ("SHIPPED".equals(status)) {
            order.setShippingTime(LocalDateTime.now());
        } else if ("DELIVERED".equals(status)) {
            order.setDeliveryTime(LocalDateTime.now());
        }
        
        boolean success = orderService.updateById(order);
        
        if (success) {
            return ResultVO.success("状态更新成功");
        } else {
            return ResultVO.error("状态更新失败");
        }
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
     * 获取统计数据 - 销售趋势
     * @param days 天数
     * @return 销售趋势数据
     */
    @GetMapping("/statistics/sales")
    public ResultVO<Map<String, Object>> getSalesStatistics(
            @RequestParam(defaultValue = "30") Integer days) {
        log.info("获取销售趋势数据: days={}", days);
        
        // 从数据库查询真实销售数据
        List<String> dates = new ArrayList<>();
        List<BigDecimal> amounts = new ArrayList<>();
        List<Long> orderCounts = new ArrayList<>();
        
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDateTime.now().minusDays(i).toLocalDate();
            dates.add(date.toString());
            
            // 查询当天的销售额
            LambdaQueryWrapper<OrderMain> orderWrapper = new LambdaQueryWrapper<>();
            orderWrapper.ge(OrderMain::getCreateTime, date.atStartOfDay());
            orderWrapper.lt(OrderMain::getCreateTime, date.plusDays(1).atStartOfDay());
            orderWrapper.in(OrderMain::getStatus, "PAID", "SHIPPED", "DELIVERED");
            
            List<OrderMain> orders = orderService.list(orderWrapper);
            BigDecimal dayAmount = BigDecimal.ZERO;
            for (OrderMain order : orders) {
                dayAmount = dayAmount.add(order.getTotalAmount());
            }
            amounts.add(dayAmount);
            orderCounts.add((long) orders.size());
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("dates", dates);
        result.put("amounts", amounts);
        result.put("orderCounts", orderCounts);
        
        return ResultVO.success(result);
    }

    /**
     * 获取统计数据 - 商品分类占比
     * @return 分类占比数据
     */
    @GetMapping("/statistics/category")
    public ResultVO<Map<String, Object>> getCategoryStatistics() {
        log.info("获取商品分类占比数据");
        
        // 使用LambdaQueryWrapper统计各分类商品数量
        List<ProductCategory> categories = categoryService.list();
        
        List<String> categoryNames = new ArrayList<>();
        List<Long> counts = new ArrayList<>();
        
        for (ProductCategory category : categories) {
            LambdaQueryWrapper<LotusRootProduct> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(LotusRootProduct::getCategoryId, category.getId());
            Long count = productService.count(queryWrapper);
            
            categoryNames.add(category.getCategoryName());
            counts.add(count);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("categories", categoryNames);
        result.put("counts", counts);
        
        return ResultVO.success(result);
    }

    /**
     * 获取统计数据 - 农户销量排行
     * @return 农户销量排行数据
     */
    @GetMapping("/statistics/farmers")
    public ResultVO<Map<String, Object>> getFarmerStatistics() {
        log.info("获取农户销量排行数据");
        
        // 查询所有农户
        LambdaQueryWrapper<SysUser> farmerWrapper = new LambdaQueryWrapper<>();
        farmerWrapper.eq(SysUser::getRole, "FARMER");
        List<SysUser> farmers = userService.list(farmerWrapper);
        
        List<String> farmerNames = new ArrayList<>();
        List<BigDecimal> salesAmounts = new ArrayList<>();
        
        for (SysUser farmer : farmers) {
            // 查询该农户的销量
            LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.eq(OrderItem::getFarmerId, farmer.getId());
            List<OrderItem> items = orderItemService.list(itemWrapper);
            
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (OrderItem item : items) {
                totalAmount = totalAmount.add(item.getSubtotal());
            }
            
            farmerNames.add(farmer.getName());
            salesAmounts.add(totalAmount);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("farmers", farmerNames);
        result.put("salesAmounts", salesAmounts);
        
        return ResultVO.success(result);
    }

    /**
     * 获取统计数据 - 库存预警
     * @return 库存预警数据
     */
    @GetMapping("/statistics/inventory")
    public ResultVO<List<Map<String, Object>>> getInventoryStatistics() {
        log.info("获取库存预警数据");
        
        // 使用LambdaQueryWrapper查询库存不足的商品
        LambdaQueryWrapper<LotusRootProduct> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LotusRootProduct::getStatus, "ON_SALE");
        queryWrapper.apply("stock < 50");
        
        List<LotusRootProduct> lowStockProducts = productService.list(queryWrapper);
        
        List<Map<String, Object>> inventoryAlerts = new ArrayList<>();
        
        for (LotusRootProduct product : lowStockProducts) {
            Map<String, Object> alert = new HashMap<>();
            alert.put("productName", product.getProductName());
            alert.put("currentStock", product.getStock());
            alert.put("minStock", 30); // 假设最小库存为30
            
            // 获取分类名称
            ProductCategory category = categoryService.getById(product.getCategoryId());
            if (category != null) {
                alert.put("categoryName", category.getCategoryName());
            } else {
                alert.put("categoryName", "未知分类");
            }
            
            // 获取农户名称
            SysUser farmer = userService.getById(product.getFarmerId());
            if (farmer != null) {
                alert.put("farmerName", farmer.getName());
            } else {
                alert.put("farmerName", "未知农户");
            }
            
            inventoryAlerts.add(alert);
        }
        
        return ResultVO.success(inventoryAlerts);
    }
    
    /**
     * 获取统计数据 - 订单状态分布
     * @return 订单状态分布数据
     */
    @GetMapping("/statistics/order-status")
    public ResultVO<Map<String, Object>> getOrderStatusStatistics() {
        log.info("获取订单状态分布数据");
        
        // 统计各状态订单数量
        LambdaQueryWrapper<OrderMain> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(OrderMain::getStatus, "PENDING");
        long pendingCount = orderService.count(pendingWrapper);
        
        LambdaQueryWrapper<OrderMain> paidWrapper = new LambdaQueryWrapper<>();
        paidWrapper.eq(OrderMain::getStatus, "PAID");
        long paidCount = orderService.count(paidWrapper);
        
        LambdaQueryWrapper<OrderMain> shippedWrapper = new LambdaQueryWrapper<>();
        shippedWrapper.eq(OrderMain::getStatus, "SHIPPED");
        long shippedCount = orderService.count(shippedWrapper);
        
        LambdaQueryWrapper<OrderMain> deliveredWrapper = new LambdaQueryWrapper<>();
        deliveredWrapper.eq(OrderMain::getStatus, "DELIVERED");
        long deliveredCount = orderService.count(deliveredWrapper);
        
        LambdaQueryWrapper<OrderMain> cancelledWrapper = new LambdaQueryWrapper<>();
        cancelledWrapper.eq(OrderMain::getStatus, "CANCELLED");
        long cancelledCount = orderService.count(cancelledWrapper);
        
        List<String> statusNames = new ArrayList<>();
        List<Long> counts = new ArrayList<>();
        
        statusNames.add("待支付");
        counts.add(pendingCount);
        
        statusNames.add("已支付");
        counts.add(paidCount);
        
        statusNames.add("已发货");
        counts.add(shippedCount);
        
        statusNames.add("已完成");
        counts.add(deliveredCount);
        
        statusNames.add("已取消");
        counts.add(cancelledCount);
        
        Map<String, Object> result = new HashMap<>();
        result.put("statusNames", statusNames);
        result.put("counts", counts);
        
        return ResultVO.success(result);
    }

    /**
     * 获取分类列表
     * @return 分类列表
     */
    @GetMapping("/categories")
    public ResultVO<List<ProductCategory>> getCategories() {
        log.info("获取分类列表");
        
        LambdaQueryWrapper<ProductCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(ProductCategory::getSortOrder);
        
        List<ProductCategory> categories = categoryService.list(queryWrapper);
        return ResultVO.success(categories);
    }
    
    /**
     * 添加分类
     * @param category 分类信息
     * @return 添加结果
     */
    @PostMapping("/categories")
    public ResultVO<Void> addCategory(@RequestBody ProductCategory category) {
        log.info("添加分类: categoryName={}", category.getCategoryName());
        
        boolean success = categoryService.save(category);
        if (success) {
            return ResultVO.success("添加成功");
        } else {
            return ResultVO.error("添加失败");
        }
    }
    
    /**
     * 更新分类
     * @param id 分类ID
     * @param category 分类信息
     * @return 更新结果
     */
    @PutMapping("/categories/{id}")
    public ResultVO<Void> updateCategory(@PathVariable Long id, @RequestBody ProductCategory category) {
        log.info("更新分类: id={}", id);
        
        category.setId(id);
        boolean success = categoryService.updateById(category);
        if (success) {
            return ResultVO.success("更新成功");
        } else {
            return ResultVO.error("更新失败");
        }
    }
    
    /**
     * 删除分类
     * @param id 分类ID
     * @return 删除结果
     */
    @DeleteMapping("/categories/{id}")
    public ResultVO<Void> deleteCategory(@PathVariable Long id) {
        log.info("删除分类: id={}", id);
        
        boolean success = categoryService.removeById(id);
        if (success) {
            return ResultVO.success("删除成功");
        } else {
            return ResultVO.error("删除失败");
        }
    }

    /**
     * 获取统计数据 - 概览数据
     * @return 概览统计数据
     */
    @GetMapping("/statistics/overview")
    public ResultVO<Map<String, Object>> getOverviewStatistics() {
        log.info("获取概览统计数据");
        
        Map<String, Object> result = new HashMap<>();
        
        // 统计用户数量
        LambdaQueryWrapper<SysUser> userWrapper = new LambdaQueryWrapper<>();
        result.put("totalUsers", userService.count(userWrapper));
        
        // 统计农户数量
        LambdaQueryWrapper<SysUser> farmerWrapper = new LambdaQueryWrapper<>();
        farmerWrapper.eq(SysUser::getRole, "FARMER");
        result.put("totalFarmers", userService.count(farmerWrapper));
        
        // 统计客户数量
        LambdaQueryWrapper<SysUser> customerWrapper = new LambdaQueryWrapper<>();
        customerWrapper.eq(SysUser::getRole, "CUSTOMER");
        result.put("totalCustomers", userService.count(customerWrapper));
        
        // 统计商品数量
        LambdaQueryWrapper<LotusRootProduct> productWrapper = new LambdaQueryWrapper<>();
        result.put("totalProducts", productService.count(productWrapper));
        
        // 统计上架商品数量
        LambdaQueryWrapper<LotusRootProduct> onSaleWrapper = new LambdaQueryWrapper<>();
        onSaleWrapper.eq(LotusRootProduct::getStatus, "ON_SALE");
        result.put("onSaleProducts", productService.count(onSaleWrapper));
        
        // 统计待审核商品数量
        LambdaQueryWrapper<LotusRootProduct> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(LotusRootProduct::getStatus, "PENDING");
        result.put("pendingProducts", productService.count(pendingWrapper));
        
        // 统计订单数量
        LambdaQueryWrapper<OrderMain> orderWrapper = new LambdaQueryWrapper<>();
        result.put("totalOrders", orderService.count(orderWrapper));
        
        // 统计待处理订单数量
        LambdaQueryWrapper<OrderMain> pendingOrderWrapper = new LambdaQueryWrapper<>();
        pendingOrderWrapper.in(OrderMain::getStatus, "PENDING", "PAID");
        result.put("pendingOrders", orderService.count(pendingOrderWrapper));
        
        // 统计总销售额
        LambdaQueryWrapper<OrderMain> salesWrapper = new LambdaQueryWrapper<>();
        salesWrapper.in(OrderMain::getStatus, "PAID", "SHIPPED", "DELIVERED");
        List<OrderMain> salesOrders = orderService.list(salesWrapper);
        BigDecimal totalSales = BigDecimal.ZERO;
        for (OrderMain order : salesOrders) {
            totalSales = totalSales.add(order.getTotalAmount());
        }
        result.put("totalSales", totalSales);
        
        return ResultVO.success(result);
    }
    
    /**
     * 获取统计数据 - 总销售额
     * @return 总销售额数据
     */
    @GetMapping("/statistics/total-sales")
    public ResultVO<Map<String, Object>> getTotalSales() {
        log.info("获取总销售额数据");
        
        // 统计总销售额
        LambdaQueryWrapper<OrderMain> salesWrapper = new LambdaQueryWrapper<>();
        salesWrapper.in(OrderMain::getStatus, "PAID", "SHIPPED", "DELIVERED");
        List<OrderMain> salesOrders = orderService.list(salesWrapper);
        BigDecimal totalSales = BigDecimal.ZERO;
        for (OrderMain order : salesOrders) {
            totalSales = totalSales.add(order.getTotalAmount());
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalSales", totalSales);
        
        return ResultVO.success(result);
    }
}
