package com.cex.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.cex.notification.domain.entity.Notification;
import com.cex.notification.mapper.NotificationMapper;
import com.cex.notification.service.NotificationService;
import com.cex.common.core.domain.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 通知服务实现类
 * 
 * @author cex
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;

    @Override
    public Result<Void> sendNotification(Long userId, String title, String content, Integer type) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setStatus(0);
        notification.setIsPush(1);
        notification.setPushTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        notificationMapper.insert(notification);
        
        log.info("发送通知给用户{}，标题：{}", userId, title);
        
        return Result.success();
    }

    @Override
    public Result<List<Notification>> getUserNotifications(Long userId, Integer type, Integer status) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId);
        
        if (type != null) {
            wrapper.eq(Notification::getType, type);
        }
        if (status != null) {
            wrapper.eq(Notification::getStatus, status);
        }
        
        wrapper.orderByDesc(Notification::getCreateTime);
        
        List<Notification> notifications = notificationMapper.selectList(wrapper);
        return Result.success(notifications);
    }

    @Override
    public Result<Void> markAsRead(Long notificationId) {
        LambdaUpdateWrapper<Notification> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Notification::getId, notificationId);
        wrapper.set(Notification::getStatus, 1);
        wrapper.set(Notification::getReadTime, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        notificationMapper.update(null, wrapper);
        
        log.info("标记通知{}为已读", notificationId);
        
        return Result.success();
    }

    @Override
    public Result<Void> markAllAsRead(Long userId) {
        LambdaUpdateWrapper<Notification> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Notification::getUserId, userId);
        wrapper.eq(Notification::getStatus, 0);
        wrapper.set(Notification::getStatus, 1);
        wrapper.set(Notification::getReadTime, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        notificationMapper.update(null, wrapper);
        
        log.info("标记用户{}所有通知为已读", userId);
        
        return Result.success();
    }

    @Override
    public Result<Void> deleteNotification(Long notificationId) {
        notificationMapper.deleteById(notificationId);
        
        log.info("删除通知{}", notificationId);
        
        return Result.success();
    }

    @Override
    public Result<Integer> getUnreadCount(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId);
        wrapper.eq(Notification::getStatus, 0);
        
        Long count = notificationMapper.selectCount(wrapper);
        
        return Result.success(count.intValue());
    }
}
