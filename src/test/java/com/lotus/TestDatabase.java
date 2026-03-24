package com.lotus;

import com.lotus.entity.SysUser;
import com.lotus.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestDatabase {

    @Autowired
    private UserMapper userMapper;

    @Test
    public void testUserMapper() {
        System.out.println("Testing UserMapper...");
        SysUser user = userMapper.selectById(1L);
        System.out.println("User: " + user);
        System.out.println("Test completed successfully!");
    }
}
