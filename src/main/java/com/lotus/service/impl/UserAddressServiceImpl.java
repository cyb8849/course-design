package com.lotus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lotus.entity.UserAddress;
import com.lotus.mapper.UserAddressMapper;
import com.lotus.service.UserAddressService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserAddressServiceImpl extends ServiceImpl<UserAddressMapper, UserAddress> implements UserAddressService {

    @Override
    public List<UserAddress> getUserAddresses(Long userId) {
        LambdaQueryWrapper<UserAddress> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserAddress::getUserId, userId);
        return list(queryWrapper);
    }

    @Override
    public UserAddress getDefaultAddress(Long userId) {
        LambdaQueryWrapper<UserAddress> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserAddress::getUserId, userId);
        queryWrapper.eq(UserAddress::getIsDefault, 1);
        return getOne(queryWrapper);
    }

    @Override
    public boolean setDefaultAddress(Long id, Long userId) {
        // 先将所有地址设置为非默认
        LambdaQueryWrapper<UserAddress> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserAddress::getUserId, userId);
        List<UserAddress> addresses = list(queryWrapper);
        for (UserAddress address : addresses) {
            address.setIsDefault(0);
            updateById(address);
        }
        // 再将指定地址设置为默认
        UserAddress address = getById(id);
        if (address != null && address.getUserId().equals(userId)) {
            address.setIsDefault(1);
            return updateById(address);
        }
        return false;
    }
}