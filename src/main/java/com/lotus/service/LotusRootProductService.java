package com.lotus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lotus.entity.LotusRootProduct;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.Map;

public interface LotusRootProductService extends IService<LotusRootProduct> {
    IPage<LotusRootProduct> getFarmerProducts(Page<LotusRootProduct> page, Long farmerId);
    Map<String, Integer> getFarmerProductStats(Long farmerId);
    boolean updateProductStatus(Long productId, String status);
    IPage<LotusRootProduct> getPendingProducts(Page<LotusRootProduct> page);
}