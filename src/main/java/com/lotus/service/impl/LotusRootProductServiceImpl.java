package com.lotus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lotus.entity.LotusRootProduct;
import com.lotus.mapper.LotusRootProductMapper;
import com.lotus.service.LotusRootProductService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class LotusRootProductServiceImpl extends ServiceImpl<LotusRootProductMapper, LotusRootProduct> implements LotusRootProductService {

    @Override
    public IPage<LotusRootProduct> getFarmerProducts(Page<LotusRootProduct> page, Long farmerId) {
        LambdaQueryWrapper<LotusRootProduct> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LotusRootProduct::getFarmerId, farmerId);
        return page(page, queryWrapper);
    }

    @Override
    public Map<String, Integer> getFarmerProductStats(Long farmerId) {
        LambdaQueryWrapper<LotusRootProduct> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LotusRootProduct::getFarmerId, farmerId);
        long total = count(queryWrapper);

        queryWrapper.eq(LotusRootProduct::getStatus, "ON_SALE");
        long onSale = count(queryWrapper);

        queryWrapper.eq(LotusRootProduct::getStatus, "PENDING");
        long pending = count(queryWrapper);

        Map<String, Integer> stats = new HashMap<>();
        stats.put("total", (int) total);
        stats.put("onSale", (int) onSale);
        stats.put("pending", (int) pending);
        return stats;
    }

    @Override
    public boolean updateProductStatus(Long productId, String status) {
        LotusRootProduct product = getById(productId);
        if (product != null) {
            product.setStatus(status);
            return updateById(product);
        }
        return false;
    }

    @Override
    public IPage<LotusRootProduct> getPendingProducts(Page<LotusRootProduct> page) {
        LambdaQueryWrapper<LotusRootProduct> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LotusRootProduct::getStatus, "PENDING");
        return page(page, queryWrapper);
    }
}