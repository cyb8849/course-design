package com.lotus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lotus.entity.OrderMain;
import com.lotus.entity.OrderSub;
import com.lotus.mapper.OrderSubMapper;
import com.lotus.service.OrderMainService;
import com.lotus.service.OrderSubService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderSubServiceImpl extends ServiceImpl<OrderSubMapper, OrderSub> implements OrderSubService {

    private final OrderMainService orderMainService;

    @Override
    public List<OrderSub> getByOrderId(Long orderId) {
        LambdaQueryWrapper<OrderSub> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderSub::getOrderId, orderId);
        return list(queryWrapper);
    }

    @Override
    public List<OrderSub> getByFarmerId(Long farmerId) {
        LambdaQueryWrapper<OrderSub> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderSub::getFarmerId, farmerId);
        return list(queryWrapper);
    }

    @Override
    public boolean shipOrderSub(Long subId, Long logisticsId) {
        OrderSub orderSub = getById(subId);
        if (orderSub == null) {
            log.warn("子订单不存在: subId={}", subId);
            return false;
        }

        if (!"PAID".equals(orderSub.getStatus())) {
            log.warn("子订单状态不是已支付: subId={}, status={}", subId, orderSub.getStatus());
            return false;
        }

        // 更新子订单状态
        orderSub.setStatus("SHIPPED");
        orderSub.setShippingTime(LocalDateTime.now());
        orderSub.setLogisticsId(logisticsId);
        boolean success = updateById(orderSub);

        if (success) {
            // 计算并更新主订单状态
            String mainStatus = calculateMainOrderStatus(orderSub.getOrderId());
            OrderMain orderMain = orderMainService.getById(orderSub.getOrderId());
            if (orderMain != null) {
                orderMain.setStatus(mainStatus);
                if ("SHIPPED".equals(mainStatus)) {
                    orderMain.setShippingTime(LocalDateTime.now());
                }
                orderMainService.updateById(orderMain);
            }
        }

        return success;
    }

    @Override
    public String calculateMainOrderStatus(Long orderId) {
        List<OrderSub> orderSubs = getByOrderId(orderId);
        if (orderSubs.isEmpty()) {
            return "PENDING";
        }

        boolean allShipped = true;
        boolean allDelivered = true;

        for (OrderSub sub : orderSubs) {
            String status = sub.getStatus();
            if (!"SHIPPED".equals(status) && !"DELIVERED".equals(status)) {
                allShipped = false;
            }
            if (!"DELIVERED".equals(status)) {
                allDelivered = false;
            }
        }

        if (allDelivered) {
            return "DELIVERED";
        } else if (allShipped) {
            return "SHIPPED";
        } else {
            return "PAID";
        }
    }
}