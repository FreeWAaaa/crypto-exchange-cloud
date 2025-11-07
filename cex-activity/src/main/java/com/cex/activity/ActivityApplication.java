package com.cex.activity;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 活动服务启动类
 * 
 * @author cex
 */
@SpringBootApplication(scanBasePackages = {"com.cex.activity", "com.cex.common"})
@EnableDiscoveryClient
@MapperScan("com.cex.activity.mapper")
public class ActivityApplication {

    public static void main(String[] args) {
        SpringApplication.run(ActivityApplication.class, args);
    }
}
