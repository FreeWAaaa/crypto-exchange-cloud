package com.cex.notification.controller;

import com.cex.common.core.domain.Result;
import com.cex.notification.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 短信服务控制器
 * 
 * @author cex
 */
@Slf4j
@RestController
@RequestMapping("/api/notification/sms")
@RequiredArgsConstructor
public class SmsController {
    
    private final SmsService smsService;
    
    /**
     * 发送验证码（内部接口）
     */
    @PostMapping("/verify-code")
    public Result<Boolean> sendVerifyCode(@RequestParam String mobile,
                                           @RequestParam String code) {
        boolean success = smsService.sendVerifyCode(mobile, code);
        return Result.success(success);
    }
    
    /**
     * 发送通知短信（内部接口）
     */
    @PostMapping("/notification")
    public Result<Boolean> sendNotification(@RequestParam String mobile,
                                             @RequestParam String content) {
        boolean success = smsService.sendNotification(mobile, content);
        return Result.success(success);
    }
}

