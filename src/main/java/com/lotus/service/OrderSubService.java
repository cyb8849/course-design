package com.lotus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lotus.entity.OrderSub;

import java.util.List;

public interface OrderSubService extends IService<OrderSub> {
    /**
     * 根据订单ID获取子订单列表
     * @param orderId 订单ID
     * @return 子订单列表
     */
    List<OrderSub> getByOrderId(Long orderId);

    /**
     * 根据农户ID获取子订单列表
     * @param farmerId 农户ID
     * @return 子订单列表
     */
    List<OrderSub> getByFarmerId(Long farmerId);

    /**
     * 发货子订单
     * @param subId 子订单ID
     * @param logisticsId 物流信息ID
     * @return 是否成功
     */
    boolean shipOrderSub(Long subId, Long logisticsId);

    /**
     * 根据订单ID计算主订单状态
     * @param orderId 订单ID
     * @return 主订单状态
     */
    String calculateMainOrderStatus(Long orderId);
}