package com.cex.notification.controller;

import com.cex.common.core.domain.Result;
import com.cex.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 邮件服务控制器
 * 
 * @author cex
 */
@Slf4j
@RestController
@RequestMapping("/api/notification/email")
@RequiredArgsConstructor
public class EmailController {
    
    private final EmailService emailService;
    
    /**
     * 发送验证码（内部接口）
     */
    @PostMapping("/verify-code")
    public Result<Boolean> sendVerifyCode(@RequestParam String toEmail,
                                           @RequestParam String code) {
        boolean success = emailService.sendVerifyCode(toEmail, code);
        return Result.success(success);
    }
    
    /**
     * 发送通知邮件（内部接口）
     */
    @PostMapping("/notification")
    public Result<Boolean> sendNotification(@RequestParam String toEmail,
                                             @RequestParam String subject,
                                             @RequestParam String content) {
        boolean success = emailService.sendNotification(toEmail, subject, content);
        return Result.success(success);
    }
}

