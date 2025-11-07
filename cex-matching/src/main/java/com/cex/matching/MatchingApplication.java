package com.cex.matching;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 撮合引擎服务启动类
 * 
 * @author cex
 */
@SpringBootApplication(scanBasePackages = {"com.cex.matching", "com.cex.common"})
@EnableDiscoveryClient
public class MatchingApplication {

    public static void main(String[] args) {
        SpringApplication.run(MatchingApplication.class, args);
    }
}
