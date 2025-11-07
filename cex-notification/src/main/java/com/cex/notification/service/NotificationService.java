package com.cex.notification.service;

import com.cex.notification.domain.entity.Notification;
import com.cex.common.core.domain.Result;

import java.util.List;

/**
 * 通知服务接口
 * 
 * @author cex
 */
public interface NotificationService {

    /**
     * 发送通知
     */
    Result<Void> sendNotification(Long userId, String title, String content, Integer type);

    /**
     * 获取用户通知列表
     */
    Result<List<Notification>> getUserNotifications(Long userId, Integer type, Integer status);

    /**
     * 标记通知为已读
     */
    Result<Void> markAsRead(Long notificationId);

    /**
     * 批量标记为已读
     */
    Result<Void> markAllAsRead(Long userId);

    /**
     * 删除通知
     */
    Result<Void> deleteNotification(Long notificationId);

    /**
     * 获取未读通知数量
     */
    Result<Integer> getUnreadCount(Long userId);
}
