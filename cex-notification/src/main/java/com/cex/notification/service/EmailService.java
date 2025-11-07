package com.cex.notification.service;

/**
 * 邮件服务接口
 * 
 * @author cex
 */
public interface EmailService {
    
    /**
     * 发送验证码邮件
     */
    boolean sendVerifyCode(String toEmail, String code);
    
    /**
     * 发送通知邮件
     */
    boolean sendNotification(String toEmail, String subject, String content);
    
    /**
     * 发送HTML邮件
     */
    boolean sendHtmlEmail(String toEmail, String subject, String htmlContent);
}

