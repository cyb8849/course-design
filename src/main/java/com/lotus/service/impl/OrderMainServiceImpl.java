package com.lotus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lotus.entity.OrderMain;
import com.lotus.mapper.OrderMainMapper;
import com.lotus.service.OrderMainService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderMainServiceImpl extends ServiceImpl<OrderMainMapper, OrderMain> implements OrderMainService {

    @Override
    public IPage<OrderMain> getFarmerOrders(Page<OrderMain> page, Long farmerId) {
        // 这里需要关联查询订单项，获取农户的订单
        // 简化实现，实际应该使用关联查询
        return page(page);
    }

    @Override
    public List<OrderMain> getRecentFarmerOrders(Long farmerId, int limit) {
        LambdaQueryWrapper<OrderMain> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(OrderMain::getCreateTime);
        queryWrapper.last("LIMIT " + limit);
        return list(queryWrapper);
    }

    @Override
    public boolean updateOrderStatus(Long orderId, String status) {
        OrderMain order = getById(orderId);
        if (order != null) {
            order.setStatus(status);
            return updateById(order);
        }
        return false;
    }

    @Override
    public IPage<OrderMain> getAllOrders(Page<OrderMain> page) {
        return page(page);
    }
}