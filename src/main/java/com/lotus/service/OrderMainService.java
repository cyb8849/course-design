package com.lotus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lotus.entity.OrderMain;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

public interface OrderMainService extends IService<OrderMain> {
    IPage<OrderMain> getFarmerOrders(Page<OrderMain> page, Long farmerId);
    List<OrderMain> getRecentFarmerOrders(Long farmerId, int limit);
    boolean updateOrderStatus(Long orderId, String status);
    IPage<OrderMain> getAllOrders(Page<OrderMain> page);
}