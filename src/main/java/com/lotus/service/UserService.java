package com.lotus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lotus.entity.SysUser;

public interface UserService extends IService<SysUser> {
    SysUser login(String username, String password);
    void register(SysUser user);
    SysUser getByUsername(String username);
}