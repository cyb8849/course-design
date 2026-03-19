package com.lotus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lotus.entity.UserAddress;

import java.util.List;

public interface UserAddressService extends IService<UserAddress> {
    List<UserAddress> getUserAddresses(Long userId);
    UserAddress getDefaultAddress(Long userId);
    boolean setDefaultAddress(Long id, Long userId);
}