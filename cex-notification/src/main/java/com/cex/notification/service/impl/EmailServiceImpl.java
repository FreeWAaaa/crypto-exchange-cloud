package com.cex.notification.service.impl;

import com.cex.notification.domain.entity.EmailRecord;
import com.cex.notification.mapper.EmailRecordMapper;
import com.cex.notification.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;
import java.util.Date;

/**
 * 邮件服务实现
 * 
 * @author cex
 */
@Slf4j
@Service
public class EmailServiceImpl implements EmailService {
    
    @Autowired(required = false)
    private JavaMailSender mailSender;
    
    @Autowired
    private EmailRecordMapper emailRecordMapper;
    
    @Value("${spring.mail.username:}")
    private String fromEmail;
    
    @Value("${email.enabled:false}")
    private boolean emailEnabled;
    
    @Override
    public boolean sendVerifyCode(String toEmail, String code) {
        log.info("发送验证码邮件：toEmail={}, code={}", toEmail, code);
        
        String subject = "【CEX】邮箱验证码";
        String content = String.format("您的验证码是：%s，10分钟内有效。如非本人操作，请忽略此邮件。", code);
        
        return sendNotification(toEmail, subject, content);
    }
    
    @Override
    public boolean sendNotification(String toEmail, String subject, String content) {
        log.info("发送通知邮件：toEmail={}, subject={}", toEmail, subject);
        
        EmailRecord record = new EmailRecord();
        record.setToEmail(toEmail);
        record.setSubject(subject);
        record.setContent(content);
        record.setEmailType(2);  // 通知
        record.setSendTime(new Date());
        record.setRetryCount(0);
        
        if (emailEnabled && mailSender != null) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(toEmail);
                message.setSubject(subject);
                message.setText(content);
                
                mailSender.send(message);
                
                record.setStatus(1);  // 已发送
                log.info("邮件已发送");
            } catch (Exception e) {
                record.setStatus(2);  // 发送失败
                record.setErrorMessage(e.getMessage());
                log.error("邮件发送失败", e);
            }
        } else {
            record.setStatus(0);  // 未发送
            log.warn("邮件功能未启用");
        }
        
        emailRecordMapper.insert(record);
        return record.getStatus() == 1;
    }
    
    @Override
    public boolean sendHtmlEmail(String toEmail, String subject, String htmlContent) {
        log.info("发送HTML邮件：toEmail={}, subject={}", toEmail, subject);
        
        EmailRecord record = new EmailRecord();
        record.setToEmail(toEmail);
        record.setSubject(subject);
        record.setContent(htmlContent);
        record.setEmailType(1);  // HTML邮件
        record.setSendTime(new Date());
        record.setRetryCount(0);
        
        if (emailEnabled && mailSender != null) {
            try {
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                helper.setFrom(fromEmail);
                helper.setTo(toEmail);
                helper.setSubject(subject);
                helper.setText(htmlContent, true);  // true 表示 HTML
                
                mailSender.send(mimeMessage);
                
                record.setStatus(1);
                log.info("HTML邮件已发送");
            } catch (Exception e) {
                record.setStatus(2);
                record.setErrorMessage(e.getMessage());
                log.error("HTML邮件发送失败", e);
            }
        } else {
            record.setStatus(0);
            log.warn("邮件功能未启用");
        }
        
        emailRecordMapper.insert(record);
        return record.getStatus() == 1;
    }
}

