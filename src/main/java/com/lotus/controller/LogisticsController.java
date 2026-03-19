package com.lotus.controller;

import com.lotus.common.ResultVO;
import com.lotus.entity.LogisticsInfo;
import com.lotus.service.LogisticsInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 物流信息控制器
 * 处理物流查询、订阅等请求
 */
@Slf4j
@RestController
@RequestMapping("/logistics")
@RequiredArgsConstructor
public class LogisticsController {

    private final LogisticsInfoService logisticsInfoService;

    /**
     * 根据订单ID查询物流信息
     * @param orderId 订单ID
     * @return 物流信息
     */
    @GetMapping("/order/{orderId}")
    public ResultVO<LogisticsInfo> getLogisticsByOrderId(@PathVariable Long orderId) {
        log.info("查询订单物流信息: orderId={}", orderId);
        
        try {
            LogisticsInfo logisticsInfo = logisticsInfoService.getByOrderId(orderId);
            
            if (logisticsInfo == null) {
                return ResultVO.error("该订单暂无物流信息");
            }
            
            return ResultVO.success(logisticsInfo);
        } catch (Exception e) {
            log.error("查询物流信息失败: orderId={}", orderId, e);
            return ResultVO.error("查询物流信息失败");
        }
    }

    /**
     * 查询物流轨迹
     * @param trackingNo 快递单号
     * @param expressCompany 快递公司代码
     * @return 物流轨迹列表
     */
    @GetMapping("/tracking")
    public ResultVO<List<Map<String, Object>>> queryTracking(
            @RequestParam String trackingNo,
            @RequestParam String expressCompany) {
        log.info("查询物流轨迹: trackingNo={}, expressCompany={}", trackingNo, expressCompany);
        
        List<Map<String, Object>> trackingList = logisticsInfoService.queryTracking(trackingNo, expressCompany);
        
        return ResultVO.success(trackingList);
    }

    /**
     * 创建物流信息（农户发货时使用）
     * @param orderId 订单ID
     * @param expressCompany 快递公司代码
     * @param trackingNo 快递单号
     * @return 创建的物流信息
     */
    @PostMapping("/create")
    public ResultVO<LogisticsInfo> createLogistics(
            @RequestParam Long orderId,
            @RequestParam String expressCompany,
            @RequestParam String trackingNo) {
        log.info("创建物流信息: orderId={}, expressCompany={}, trackingNo={}", orderId, expressCompany, trackingNo);
        
        try {
            LogisticsInfo logisticsInfo = logisticsInfoService.createLogistics(orderId, expressCompany, trackingNo);
            return ResultVO.success(logisticsInfo);
        } catch (Exception e) {
            log.error("创建物流信息失败", e);
            return ResultVO.error("创建物流信息失败: " + e.getMessage());
        }
    }

    /**
     * 更新物流信息
     * @param id 物流信息ID
     * @param logisticsInfo 物流信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public ResultVO<Void> updateLogistics(@PathVariable Long id, @RequestBody LogisticsInfo logisticsInfo) {
        log.info("更新物流信息: id={}", id);
        
        logisticsInfo.setId(id);
        boolean success = logisticsInfoService.updateLogistics(logisticsInfo);
        
        if (success) {
            return ResultVO.success("物流信息更新成功");
        } else {
            return ResultVO.error("物流信息更新失败");
        }
    }

    /**
     * 快递100回调接口
     * 接收物流状态推送
     * @param callbackData 回调数据
     * @return 处理结果
     */
    @PostMapping("/callback")
    public ResultVO<String> handleCallback(@RequestBody Map<String, Object> callbackData) {
        log.info("收到物流推送: {}", callbackData);
        
        try {
            // 处理推送数据
            // 更新物流信息到数据库
            
            return ResultVO.success("{\"result\":true,\"returnCode\":\"200\",\"message\":\"成功\"}");
        } catch (Exception e) {
            log.error("处理物流推送失败", e);
            return ResultVO.success("{\"result\":false,\"returnCode\":\"500\",\"message\":\"失败\"}");
        }
    }

    /**
     * 获取支持的快递公司列表
     * @return 快递公司列表
     */
    @GetMapping("/companies")
    public ResultVO<List<Map<String, String>>> getExpressCompanies() {
        log.info("获取快递公司列表");
        
        List<Map<String, String>> companies = List.of(
                Map.of("code", "SF", "name", "顺丰速运"),
                Map.of("code", "YTO", "name", "圆通速递"),
                Map.of("code", "ZTO", "name", "中通快递"),
                Map.of("code", "YUNDA", "name", "韵达速递"),
                Map.of("code", "EMS", "name", "EMS"),
                Map.of("code", "JD", "name", "京东物流"),
                Map.of("code", "STO", "name", "申通快递"),
                Map.of("code", "HTKY", "name", "百世快递"),
                Map.of("code", "UC", "name", "邮政快递"),
                Map.of("code", "DBL", "name", "德邦物流")
        );
        
        return ResultVO.success(companies);
    }
}
