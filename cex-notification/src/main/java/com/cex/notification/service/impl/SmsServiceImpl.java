package com.cex.notification.service.impl;

import com.cex.notification.domain.entity.SmsRecord;
import com.cex.notification.mapper.SmsRecordMapper;
import com.cex.notification.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 短信服务实现（模拟实现，实际需接入第三方短信平台）
 * 
 * @author cex
 */
@Slf4j
@Service
public class SmsServiceImpl implements SmsService {
    
    @Autowired
    private SmsRecordMapper smsRecordMapper;
    
    @Value("${sms.enabled:false}")
    private boolean smsEnabled;
    
    @Override
    public boolean sendVerifyCode(String mobile, String code) {
        log.info("发送验证码短信：mobile={}, code={}", mobile, code);
        
        String content = String.format("您的验证码是：%s，10分钟内有效。", code);
        
        // 记录短信发送
        SmsRecord record = new SmsRecord();
        record.setMobile(mobile);
        record.setContent(content);
        record.setSmsType(1);  // 验证码
        record.setSendTime(new Date());
        record.setRetryCount(0);
        
        if (smsEnabled) {
            // TODO: 实际对接第三方短信平台（阿里云、腾讯云等）
            // 例如：aliyunSmsClient.send(mobile, templateCode, params);
            record.setStatus(1);  // 已发送
            record.setResponseCode("000000");
            record.setResponseMessage("发送成功");
            log.info("短信已发送（模拟）");
        } else {
            // 开发环境，仅记录日志
            record.setStatus(0);  // 未发送
            log.warn("短信功能未启用，验证码：{}", code);
        }
        
        smsRecordMapper.insert(record);
        
        // 开发环境返回 true 便于测试
        return true;
    }
    
    @Override
    public boolean sendNotification(String mobile, String content) {
        log.info("发送通知短信：mobile={}, content={}", mobile, content);
        
        SmsRecord record = new SmsRecord();
        record.setMobile(mobile);
        record.setContent(content);
        record.setSmsType(2);  // 通知
        record.setSendTime(new Date());
        record.setRetryCount(0);
        
        if (smsEnabled) {
            // TODO: 对接第三方短信平台
            record.setStatus(1);
            record.setResponseCode("000000");
            record.setResponseMessage("发送成功");
        } else {
            record.setStatus(0);
            log.warn("短信功能未启用");
        }
        
        smsRecordMapper.insert(record);
        return true;
    }
    
    @Override
    public boolean sendCustomMessage(String mobile, String content) {
        log.info("发送自定义短信：mobile={}, content={}", mobile, content);
        
        SmsRecord record = new SmsRecord();
        record.setMobile(mobile);
        record.setContent(content);
        record.setSmsType(3);  // 自定义
        record.setSendTime(new Date());
        record.setRetryCount(0);
        
        if (smsEnabled) {
            // TODO: 对接第三方短信平台
            record.setStatus(1);
            record.setResponseCode("000000");
            record.setResponseMessage("发送成功");
        } else {
            record.setStatus(0);
            log.warn("短信功能未启用");
        }
        
        smsRecordMapper.insert(record);
        return true;
    }
}

