package com.cex.trade;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 交易服务启动类
 * 
 * @author cex
 */
@SpringBootApplication(scanBasePackages = {"com.cex.trade", "com.cex.common"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.cex.trade.client")
public class TradeApplication {

    public static void main(String[] args) {
        SpringApplication.run(TradeApplication.class, args);
    }
}
