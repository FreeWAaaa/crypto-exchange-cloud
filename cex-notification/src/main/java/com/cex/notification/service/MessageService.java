package com.cex.notification.service;

import com.cex.notification.domain.entity.SysMessage;

import java.util.List;

/**
 * 站内消息服务接口
 * 
 * @author cex
 */
public interface MessageService {
    
    /**
     * 发送站内消息
     */
    void sendMessage(Long userId, String title, String content, Integer messageType);
    
    /**
     * 发送系统消息（所有用户）
     */
    void sendSystemMessage(String title, String content);
    
    /**
     * 查询用户消息列表
     */
    List<SysMessage> getUserMessages(Long userId);
    
    /**
     * 查询用户未读消息
     */
    List<SysMessage> getUnreadMessages(Long userId);
    
    /**
     * 标记消息为已读
     */
    void markAsRead(Long messageId);
    
    /**
     * 标记所有消息为已读
     */
    void markAllAsRead(Long userId);
    
    /**
     * 删除消息
     */
    void deleteMessage(Long messageId);
}

