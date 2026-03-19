package com.lotus;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.lotus.mapper")
public class LotusShopApplication {
   public static void main(String[] args) {
        SpringApplication.run(LotusShopApplication.class, args);
    }
}
