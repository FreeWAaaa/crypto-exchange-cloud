package com.cex.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cex.notification.domain.entity.SysMessage;
import com.cex.notification.mapper.SysMessageMapper;
import com.cex.notification.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 站内消息服务实现
 * 
 * @author cex
 */
@Slf4j
@Service
public class MessageServiceImpl implements MessageService {
    
    @Autowired
    private SysMessageMapper messageMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendMessage(Long userId, String title, String content, Integer messageType) {
        log.info("发送站内消息：userId={}, title={}", userId, title);
        
        SysMessage message = new SysMessage();
        message.setUserId(userId);
        message.setTitle(title);
        message.setContent(content);
        message.setMessageType(messageType);
        message.setIsRead(0);  // 未读
        
        messageMapper.insert(message);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendSystemMessage(String title, String content) {
        log.info("发送系统消息：title={}", title);
        
        // 系统消息 userId 为 0
        SysMessage message = new SysMessage();
        message.setUserId(0L);
        message.setTitle(title);
        message.setContent(content);
        message.setMessageType(1);  // 系统通知
        message.setIsRead(0);
        
        messageMapper.insert(message);
    }
    
    @Override
    public List<SysMessage> getUserMessages(Long userId) {
        LambdaQueryWrapper<SysMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMessage::getUserId, userId);
        wrapper.orderByDesc(SysMessage::getCreateTime);
        
        return messageMapper.selectList(wrapper);
    }
    
    @Override
    public List<SysMessage> getUnreadMessages(Long userId) {
        return messageMapper.selectUnreadByUserId(userId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long messageId) {
        messageMapper.markAsRead(messageId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead(Long userId) {
        messageMapper.markAllAsRead(userId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMessage(Long messageId) {
        messageMapper.deleteById(messageId);
    }
}

