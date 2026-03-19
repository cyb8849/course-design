package com.lotus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lotus.entity.ShoppingCart;
import com.lotus.mapper.ShoppingCartMapper;
import com.lotus.service.ShoppingCartService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {

    @Override
    public List<ShoppingCart> getCartItems(Long userId) {
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        return list(queryWrapper);
    }

    @Override
    public boolean addToCart(ShoppingCart cart) {
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, cart.getUserId());
        queryWrapper.eq(ShoppingCart::getProductId, cart.getProductId());
        ShoppingCart existingItem = getOne(queryWrapper);
        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + cart.getQuantity());
            return updateById(existingItem);
        } else {
            return save(cart);
        }
    }

    @Override
    public boolean updateCartItem(Long id, Integer quantity) {
        ShoppingCart cart = getById(id);
        if (cart != null) {
            cart.setQuantity(quantity);
            return updateById(cart);
        }
        return false;
    }

    @Override
    public boolean removeCartItem(Long id) {
        return removeById(id);
    }

    @Override
    public boolean clearCart(Long userId) {
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        return remove(queryWrapper);
    }
}