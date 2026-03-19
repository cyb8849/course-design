package com.lotus.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * 环境变量配置类
 * 用于加载.env文件中的环境变量
 */
@Configuration
public class DotenvConfig {

    @PostConstruct
    public void loadDotenv() {
        // 加载.env文件
        Dotenv dotenv = Dotenv.configure()
                .directory(".")
                .load();
        
        // 将.env文件中的环境变量设置到系统环境变量中
        dotenv.entries().forEach(entry -> {
            if (System.getenv(entry.getKey()) == null) {
                System.setProperty(entry.getKey(), entry.getValue());
            }
        });
    }
}
