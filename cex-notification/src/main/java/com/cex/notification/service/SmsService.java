package com.cex.notification.service;

/**
 * 短信服务接口
 * 
 * @author cex
 */
public interface SmsService {
    
    /**
     * 发送验证码短信
     */
    boolean sendVerifyCode(String mobile, String code);
    
    /**
     * 发送通知短信
     */
    boolean sendNotification(String mobile, String content);
    
    /**
     * 发送自定义短信
     */
    boolean sendCustomMessage(String mobile, String content);
}

