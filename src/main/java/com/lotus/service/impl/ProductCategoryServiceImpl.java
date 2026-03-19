package com.lotus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lotus.entity.ProductCategory;
import com.lotus.mapper.ProductCategoryMapper;
import com.lotus.service.ProductCategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductCategoryServiceImpl extends ServiceImpl<ProductCategoryMapper, ProductCategory> implements ProductCategoryService {

    @Override
    public List<ProductCategory> listCategories(LambdaQueryWrapper<ProductCategory> queryWrapper) {
        return this.list(queryWrapper);
    }
}