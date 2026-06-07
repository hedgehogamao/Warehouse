package com.autoparts;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 汽车配件门店管理系统 - 启动类
 */
@SpringBootApplication
@MapperScan("com.autoparts.repository")
public class AutoPartsApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutoPartsApplication.class, args);
    }
}
