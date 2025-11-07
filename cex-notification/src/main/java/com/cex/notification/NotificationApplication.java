package com.cex.notification;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 通知服务启动类
 * 
 * @author cex
 */
@SpringBootApplication(scanBasePackages = {"com.cex.notification", "com.cex.common"})
@EnableDiscoveryClient
@MapperScan("com.cex.notification.mapper")
public class NotificationApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationApplication.class, args);
    }
}
