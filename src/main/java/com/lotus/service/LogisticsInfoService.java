package com.lotus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lotus.entity.LogisticsInfo;

import java.util.List;
import java.util.Map;

/**
 * 物流信息服务接口
 */
public interface LogisticsInfoService extends IService<LogisticsInfo> {
    
    /**
     * 根据订单ID查询物流信息
     * @param orderId 订单ID
     * @return 物流信息
     */
    LogisticsInfo getByOrderId(Long orderId);
    
    /**
     * 创建物流信息
     * @param orderId 订单ID
     * @param expressCompany 快递公司代码
     * @param trackingNo 快递单号
     * @return 创建的物流信息
     */
    LogisticsInfo createLogistics(Long orderId, String expressCompany, String trackingNo);
    
    /**
     * 更新物流信息
     * @param logisticsInfo 物流信息
     * @return 是否成功
     */
    boolean updateLogistics(LogisticsInfo logisticsInfo);
    
    /**
     * 查询物流轨迹
     * @param trackingNo 快递单号
     * @param expressCompany 快递公司代码
     * @return 物流轨迹列表
     */
    List<Map<String, Object>> queryTracking(String trackingNo, String expressCompany);
    
    /**
     * 订阅物流推送
     * @param trackingNo 快递单号
     * @param expressCompany 快递公司代码
     * @return 是否订阅成功
     */
    boolean subscribeTracking(String trackingNo, String expressCompany);
}
