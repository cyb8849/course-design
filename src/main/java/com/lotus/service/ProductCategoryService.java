package com.lotus.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lotus.entity.ProductCategory;

import java.util.List;

public interface ProductCategoryService extends IService<ProductCategory> {
    List<ProductCategory> listCategories(LambdaQueryWrapper<ProductCategory> queryWrapper);
}