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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.alibaba.fastjson.JSON;

/**
 * 客户控制器
 * 处理客户相关的商品浏览、购物车、订单管理等请求
 */
@Slf4j
@RestController
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final LotusRootProductService productService;
    private final ProductCategoryService categoryService;
    private final ShoppingCartService cartService;
    private final UserAddressService addressService;
    private final OrderMainService orderService;
    private final OrderItemService orderItemService;
    private final TraceabilityInfoService traceabilityService;

    /**
     * 获取商品列表（分页+条件查询）
     * @param page 当前页码
     * @param size 每页条数
     * @param categoryId 分类ID
     * @param keyword 关键词
     * @return 分页商品列表
     */
    @GetMapping("/products")
    public ResultVO<IPage<LotusRootProduct>> getProducts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "default") String sortBy) {
        
        log.info("获取商品列表: page={}, size={}, categoryId={}, keyword={}, sortBy={}", page, size, categoryId, keyword, sortBy);
        
        // 使用MyBatis-Plus分页插件
        Page<LotusRootProduct> pageInfo = new Page<>(page, size);
        
        // 使用LambdaQueryWrapper进行条件查询
        LambdaQueryWrapper<LotusRootProduct> queryWrapper = new LambdaQueryWrapper<>();
        // 暂时移除状态过滤，测试商品数据
        // queryWrapper.eq(LotusRootProduct::getStatus, "ON_SALE");
        
        if (categoryId != null && categoryId != 0) {
            queryWrapper.eq(LotusRootProduct::getCategoryId, categoryId);
        }
        
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.and(w -> w
                    .like(LotusRootProduct::getProductName, keyword)
                    .or()
                    .like(LotusRootProduct::getOrigin, keyword)
            );
        }
        
        // 根据排序参数设置排序
        if ("price_asc".equals(sortBy)) {
            queryWrapper.orderByAsc(LotusRootProduct::getPrice);
        } else if ("price_desc".equals(sortBy)) {
            queryWrapper.orderByDesc(LotusRootProduct::getPrice);
        } else {
            // 默认按创建时间倒序
            queryWrapper.orderByDesc(LotusRootProduct::getCreateTime);
        }
        
        IPage<LotusRootProduct> result = productService.page(pageInfo, queryWrapper);
        
        // 销量已在数据库中存储，直接返回
        
        return ResultVO.success(result);
    }

    /**
     * 获取商品详情
     * @param id 商品ID
     * @return 商品详情
     */
    @GetMapping("/products/{id}")
    public ResultVO<LotusRootProduct> getProductDetail(@PathVariable Long id) {
        log.info("获取商品详情: id={}", id);
        
        LotusRootProduct product = productService.getById(id);
        if (product == null) {
            return ResultVO.notFound();
        }
        
        // 打印商品详情，检查是否包含种植信息、营养成分、烹饪建议
        log.info("商品详情: id={}, name={}, plantingInfo={}, nutritionInfo={}, cookingSuggestion={}", 
                product.getId(), product.getProductName(), product.getPlantingInfo(), 
                product.getNutritionInfo(), product.getCookingSuggestion());
        
        // 销量已在数据库中存储，直接返回
        
        return ResultVO.success(product);
    }

    /**
     * 获取商品分类列表
     * @return 分类列表
     */
    @GetMapping("/categories")
    public ResultVO<List<ProductCategory>> getCategories() {
        log.info("获取商品分类列表");
        
        LambdaQueryWrapper<ProductCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(ProductCategory::getSortOrder);
        
        List<ProductCategory> categories = categoryService.list(queryWrapper);
        
        return ResultVO.success(categories);
    }

    /**
     * 获取购物车列表
     * @param userId 用户ID
     * @return 购物车列表
     */
    @GetMapping("/cart/{userId}")
    public ResultVO<List<Map<String, Object>>> getCartItems(@PathVariable Long userId) {
        log.info("获取购物车列表: userId={}", userId);
        
        // 使用LambdaQueryWrapper进行条件查询
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        
        List<ShoppingCart> cartItems = cartService.list(queryWrapper);
        
        // 转换为包含商品详细信息的列表
        List<Map<String, Object>> result = new ArrayList<>();
        for (ShoppingCart cart : cartItems) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", cart.getId());
            item.put("productId", cart.getProductId());
            item.put("quantity", cart.getQuantity());
            
            // 获取商品详情
            LotusRootProduct product = productService.getById(cart.getProductId());
            if (product != null) {
                item.put("productName", product.getProductName());
                item.put("productImage", product.getImageUrl());
                item.put("price", product.getPrice());
                item.put("stock", product.getStock());
            }
            
            result.add(item);
        }
        
        return ResultVO.success(result);
    }

    /**
     * 添加商品到购物车
     * @param cart 购物车项
     * @return 添加结果
     */
    @PostMapping("/cart")
    public ResultVO<Void> addToCart(@RequestBody ShoppingCart cart) {
        log.info("添加商品到购物车: userId={}, productId={}, quantity={}", 
                cart.getUserId(), cart.getProductId(), cart.getQuantity());
        
        // 检查商品是否存在且上架
        LotusRootProduct product = productService.getById(cart.getProductId());
        if (product == null) {
            return ResultVO.notFound();
        }
        
        if (!"ON_SALE".equals(product.getStatus()) && !"APPROVED".equals(product.getStatus())) {
            return ResultVO.error("商品已下架");
        }
        
        // 检查库存是否充足
        if (product.getStock() < cart.getQuantity()) {
            return ResultVO.error("库存不足");
        }
        
        // 检查购物车是否已有该商品
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, cart.getUserId());
        queryWrapper.eq(ShoppingCart::getProductId, cart.getProductId());
        
        ShoppingCart existingCart = cartService.getOne(queryWrapper);
        
        if (existingCart != null) {
            // 更新数量
            existingCart.setQuantity(existingCart.getQuantity() + cart.getQuantity());
            cartService.updateById(existingCart);
        } else {
            // 新增购物车项
            cartService.save(cart);
        }
        
        return ResultVO.success("添加成功");
    }

    /**
     * 更新购物车商品数量
     * @param id 购物车ID
     * @param quantity 数量
     * @return 更新结果
     */
    @PutMapping("/cart/{id}")
    public ResultVO<Void> updateCartQuantity(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        log.info("更新购物车数量: id={}, quantity={}", id, quantity);
        
        ShoppingCart cart = cartService.getById(id);
        if (cart == null) {
            return ResultVO.notFound();
        }
        
        // 检查库存
        LotusRootProduct product = productService.getById(cart.getProductId());
        if (product.getStock() < quantity) {
            return ResultVO.error("库存不足");
        }
        
        cart.setQuantity(quantity);
        cartService.updateById(cart);
        
        return ResultVO.success("更新成功");
    }

    /**
     * 删除购物车项
     * @param id 购物车ID
     * @return 删除结果
     */
    @DeleteMapping("/cart/{id}")
    public ResultVO<Void> removeFromCart(@PathVariable Long id) {
        log.info("删除购物车项: id={}", id);
        
        boolean success = cartService.removeById(id);
        
        if (success) {
            return ResultVO.success("删除成功");
        } else {
            return ResultVO.error("删除失败");
        }
    }

    /**
     * 清空购物车
     * @param userId 用户ID
     * @return 清空结果
     */
    @DeleteMapping("/cart/clear/{userId}")
    public ResultVO<Void> clearCart(@PathVariable Long userId) {
        log.info("清空购物车: userId={}", userId);
        
        // 使用LambdaQueryWrapper删除
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        
        boolean success = cartService.remove(queryWrapper);
        
        if (success) {
            return ResultVO.success("清空成功");
        } else {
            return ResultVO.error("清空失败");
        }
    }

    /**
     * 获取收货地址列表
     * @param userId 用户ID
     * @return 地址列表
     */
    @GetMapping("/addresses/{userId}")
    public ResultVO<List<UserAddress>> getAddresses(@PathVariable Long userId) {
        log.info("获取收货地址列表: userId={}", userId);
        
        // 使用LambdaQueryWrapper进行条件查询
        LambdaQueryWrapper<UserAddress> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserAddress::getUserId, userId);
        queryWrapper.orderByDesc(UserAddress::getIsDefault);
        
        List<UserAddress> addresses = addressService.list(queryWrapper);
        
        return ResultVO.success(addresses);
    }

    /**
     * 添加收货地址
     * @param address 地址信息
     * @return 添加结果
     */
    @PostMapping("/addresses")
    public ResultVO<Void> addAddress(@RequestBody UserAddress address) {
        log.info("添加收货地址: userId={}", address.getUserId());
        
        // 如果设置为默认地址，先取消其他默认地址
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            LambdaQueryWrapper<UserAddress> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserAddress::getUserId, address.getUserId());
            queryWrapper.eq(UserAddress::getIsDefault, 1);
            
            List<UserAddress> defaultAddresses = addressService.list(queryWrapper);
            for (UserAddress addr : defaultAddresses) {
                addr.setIsDefault(0);
                addressService.updateById(addr);
            }
        }
        
        boolean success = addressService.save(address);
        
        if (success) {
            return ResultVO.success("添加成功");
        } else {
            return ResultVO.error("添加失败");
        }
    }

    /**
     * 更新收货地址
     * @param id 地址ID
     * @param address 地址信息
     * @return 更新结果
     */
    @PutMapping("/addresses/{id}")
    public ResultVO<Void> updateAddress(@PathVariable Long id, @RequestBody UserAddress address) {
        log.info("更新收货地址: id={}", id);
        
        // 如果设置为默认地址，先取消其他默认地址
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            LambdaQueryWrapper<UserAddress> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserAddress::getUserId, address.getUserId());
            queryWrapper.eq(UserAddress::getIsDefault, 1);
            queryWrapper.ne(UserAddress::getId, id);
            
            List<UserAddress> defaultAddresses = addressService.list(queryWrapper);
            for (UserAddress addr : defaultAddresses) {
                addr.setIsDefault(0);
                addressService.updateById(addr);
            }
        }
        
        address.setId(id);
        boolean success = addressService.updateById(address);
        
        if (success) {
            return ResultVO.success("更新成功");
        } else {
            return ResultVO.error("更新失败");
        }
    }

    /**
     * 删除收货地址
     * @param id 地址ID
     * @return 删除结果
     */
    @DeleteMapping("/addresses/{id}")
    public ResultVO<Void> deleteAddress(@PathVariable Long id) {
        log.info("删除收货地址: id={}", id);
        
        boolean success = addressService.removeById(id);
        
        if (success) {
            return ResultVO.success("删除成功");
        } else {
            return ResultVO.error("删除失败");
        }
    }

    /**
     * 创建订单
     * @param orderMain 订单信息
     * @return 订单创建结果
     */
    @PostMapping("/orders")
    @Transactional(rollbackFor = Exception.class)
    public ResultVO<OrderMain> createOrder(@RequestBody OrderMain orderMain) {
        log.info("创建订单: userId={}, addressId={}", orderMain.getUserId(), orderMain.getAddressId());
        
        // 获取购物车项
        LambdaQueryWrapper<ShoppingCart> cartWrapper = new LambdaQueryWrapper<>();
        cartWrapper.eq(ShoppingCart::getUserId, orderMain.getUserId());
        List<ShoppingCart> cartItems = cartService.list(cartWrapper);
        
        if (cartItems.isEmpty()) {
            return ResultVO.error("购物车为空");
        }
        
        // 获取收货地址
        UserAddress address = addressService.getById(orderMain.getAddressId());
        if (address == null) {
            return ResultVO.notFound();
        }
        
        // 创建订单
        String orderNo = "LR" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8);
        orderMain.setOrderNo(orderNo);
        orderMain.setStatus("PENDING");
        
        // 填充收货信息
        orderMain.setReceiverName(address.getConsignee());
        orderMain.setReceiverPhone(address.getPhone());
        orderMain.setReceiverAddress(address.getProvince() + address.getCity() + address.getDistrict() + address.getDetailAddress());
        
        // 计算订单金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        
        for (ShoppingCart cart : cartItems) {
            LotusRootProduct product = productService.getById(cart.getProductId());
                    
            if (product == null || !"ON_SALE".equals(product.getStatus())) {
                continue;
            }
                    
            if (product.getStock() < cart.getQuantity()) {
                return ResultVO.error("商品库存不足：" + product.getProductName());
            }
                    
            // 创建订单项
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getProductName());
            // 使用 String 构造函数避免精度问题
            orderItem.setPrice(new BigDecimal(product.getPrice().toString()));
            orderItem.setQuantity(cart.getQuantity());
            orderItem.setFarmerId(product.getFarmerId());
            orderItem.setSubtotal(new BigDecimal(product.getPrice().toString()).multiply(new BigDecimal(cart.getQuantity().toString())));
                    
            orderItems.add(orderItem);
                    
            // 累加订单金额
            totalAmount = totalAmount.add(orderItem.getSubtotal());
                    
            // 扣减库存
            product.setStock(product.getStock() - cart.getQuantity());
            // 增加销量
            if (product.getSales() == null) {
                product.setSales(0);
            }
            product.setSales(product.getSales() + cart.getQuantity());
            productService.updateById(product);
        }
                
        orderMain.setTotalAmount(totalAmount);
        boolean orderSaved = orderService.save(orderMain);
        log.info("订单主表保存结果：{}, 订单 ID: {}", orderSaved, orderMain.getId());
                
        // 保存订单项
        int savedCount = 0;
        for (OrderItem item : orderItems) {
            item.setOrderId(orderMain.getId());
            boolean saved = orderItemService.save(item);
            if (saved) {
                savedCount++;
            }
            log.info("订单项保存结果：{}, 商品：{}, 订单 ID: {}", saved, item.getProductName(), orderMain.getId());
        }
        log.info("成功保存 {} 个订单项", savedCount);
        
        // 清空购物车
        LambdaQueryWrapper<ShoppingCart> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(ShoppingCart::getUserId, orderMain.getUserId());
        cartService.remove(deleteWrapper);
        
        log.info("订单创建成功: orderNo={}, totalAmount={}", orderNo, totalAmount);
        
        return ResultVO.success("订单创建成功", orderMain);
    }

    /**
     * 获取订单列表
     * @param userId 用户ID
     * @param page 当前页码
     * @param size 每页条数
     * @param status 订单状态
     * @return 分页订单列表
     */
    @GetMapping("/orders")
    public ResultVO<IPage<Map<String, Object>>> getOrders(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status) {
        
        log.info("获取订单列表: userId={}, page={}, size={}, status={}", userId, page, size, status);
        
        // 使用MyBatis-Plus分页插件
        Page<OrderMain> pageInfo = new Page<>(page, size);
        
        // 使用LambdaQueryWrapper进行条件查询
        LambdaQueryWrapper<OrderMain> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderMain::getUserId, userId);
        
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
            
            // 获取订单项
            LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.eq(OrderItem::getOrderId, order.getId());
            List<OrderItem> items = orderItemService.list(itemWrapper);
            
            // 为订单项添加商品图片信息
            List<Map<String, Object>> orderItemsWithImage = new ArrayList<>();
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
        List<Map<String, Object>> orderItemsWithImage = new ArrayList<>();
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
            
            orderItemsWithImage.add(itemMap);
        }
        
        // 获取收货地址
        UserAddress address = addressService.getById(order.getAddressId());
        
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("order", order);
        result.put("items", orderItemsWithImage);
        result.put("address", address);
        
        return ResultVO.success(result);
    }
    
    /**
     * 根据订单号查询订单
     * @param orderNo 订单号
     * @return 订单详情
     */
    @GetMapping("/order/by-order-no")
    public ResultVO<OrderMain> getOrderByOrderNo(@RequestParam String orderNo) {
        log.info("根据订单号查询订单: orderNo={}", orderNo);
        
        LambdaQueryWrapper<OrderMain> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderMain::getOrderNo, orderNo);
        
        OrderMain order = orderService.getOne(queryWrapper);
        if (order == null) {
            return ResultVO.notFound();
        }
        
        return ResultVO.success(order);
    }

    /**
     * 模拟支付
     * @param orderId 订单ID
     * @return 支付结果
     */
    @PutMapping("/orders/{orderId}/pay")
    public ResultVO<Void> payOrder(@PathVariable Long orderId) {
        log.info("模拟支付: orderId={}", orderId);
        
        OrderMain order = orderService.getById(orderId);
        if (order == null) {
            return ResultVO.notFound();
        }
        
        if (!"PENDING".equals(order.getStatus())) {
            return ResultVO.error("订单状态不正确");
        }
        
        order.setStatus("PAID");
        order.setPaymentTime(LocalDateTime.now());
        
        boolean success = orderService.updateById(order);
        
        if (success) {
            return ResultVO.success("支付成功");
        } else {
            return ResultVO.error("支付失败");
        }
    }

    /**
     * 取消订单
     * @param orderId 订单ID
     * @return 取消结果
     */
    @PutMapping("/orders/{orderId}/cancel")
    @Transactional(rollbackFor = Exception.class)
    public ResultVO<Void> cancelOrder(@PathVariable Long orderId) {
        log.info("取消订单: orderId={}", orderId);
        
        OrderMain order = orderService.getById(orderId);
        if (order == null) {
            return ResultVO.notFound();
        }
        
        if (!"PENDING".equals(order.getStatus())) {
            return ResultVO.error("只有待支付的订单才能取消");
        }
        
        // 恢复库存
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> items = orderItemService.list(itemWrapper);
        
        for (OrderItem item : items) {
            LotusRootProduct product = productService.getById(item.getProductId());
            if (product != null) {
                // 恢复库存
                product.setStock(product.getStock() + item.getQuantity());
                // 减少销量
                if (product.getSales() != null && product.getSales() >= item.getQuantity()) {
                    product.setSales(product.getSales() - item.getQuantity());
                }
                productService.updateById(product);
            }
        }
        
        order.setStatus("CANCELLED");
        boolean success = orderService.updateById(order);
        
        if (success) {
            return ResultVO.success("取消成功");
        } else {
            return ResultVO.error("取消失败");
        }
    }

    /**
     * 确认收货
     * @param orderId 订单ID
     * @return 确认结果
     */
    @PutMapping("/orders/{orderId}/confirm")
    public ResultVO<Void> confirmReceipt(@PathVariable Long orderId) {
        log.info("确认收货: orderId={}", orderId);
        
        OrderMain order = orderService.getById(orderId);
        if (order == null) {
            return ResultVO.notFound();
        }
        
        if (!"SHIPPED".equals(order.getStatus())) {
            return ResultVO.error("订单状态不正确");
        }
        
        order.setStatus("DELIVERED");
        order.setDeliveryTime(LocalDateTime.now());
        
        boolean success = orderService.updateById(order);
        
        if (success) {
            return ResultVO.success("确认收货成功");
        } else {
            return ResultVO.error("确认收货失败");
        }
    }

    /**
     * 删除订单
     * @param orderId 订单ID
     * @return 删除结果
     */
    @DeleteMapping("/orders/{orderId}")
    public ResultVO<Void> deleteOrder(@PathVariable Long orderId) {
        log.info("删除订单: orderId={}", orderId);
        
        OrderMain order = orderService.getById(orderId);
        if (order == null) {
            return ResultVO.notFound();
        }
        
        if (!"CANCELLED".equals(order.getStatus())) {
            return ResultVO.error("只有已取消的订单才能删除");
        }
        
        boolean success = orderService.removeById(orderId);
        
        if (success) {
            return ResultVO.success("删除成功");
        } else {
            return ResultVO.error("删除失败");
        }
    }

    /**
     * 溯源查询
     * @param productId 商品ID
     * @return 溯源信息
     */
    @GetMapping("/traceability/{productId}")
    public ResultVO<List<Map<String, Object>>> getTraceability(@PathVariable Long productId) {
        log.info("溯源查询: productId={}", productId);
        
        // 使用LambdaQueryWrapper进行条件查询
        LambdaQueryWrapper<TraceabilityInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TraceabilityInfo::getProductId, productId);
        queryWrapper.orderByAsc(TraceabilityInfo::getCreateTime);
        
        List<TraceabilityInfo> traceabilityList = traceabilityService.list(queryWrapper);
        
        // 转换为包含图片数组的结果
        List<Map<String, Object>> records = traceabilityList.stream().map(info -> {
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
        
        return ResultVO.success(records);
    }
}