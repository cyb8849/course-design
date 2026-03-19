package com.lotus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lotus.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService extends IService<ShoppingCart> {
    List<ShoppingCart> getCartItems(Long userId);
    boolean addToCart(ShoppingCart cart);
    boolean updateCartItem(Long id, Integer quantity);
    boolean removeCartItem(Long id);
    boolean clearCart(Long userId);
}