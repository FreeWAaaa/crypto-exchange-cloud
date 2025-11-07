package com.cex.notification.controller;

import com.cex.notification.domain.entity.Notification;
import com.cex.notification.service.NotificationService;
import com.cex.common.core.domain.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知控制器
 * 
 * @author cex
 */
@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 发送通知
     */
    @PostMapping("/send")
    public Result<Void> sendNotification(@RequestParam Long userId,
                                         @RequestParam String title,
                                         @RequestParam String content,
                                         @RequestParam Integer type) {
        return notificationService.sendNotification(userId, title, content, type);
    }

    /**
     * 获取用户通知列表
     */
    @GetMapping("/user/{userId}")
    public Result<List<Notification>> getUserNotifications(@PathVariable Long userId,
                                                           @RequestParam(required = false) Integer type,
                                                           @RequestParam(required = false) Integer status) {
        return notificationService.getUserNotifications(userId, type, status);
    }

    /**
     * 标记通知为已读
     */
    @PostMapping("/read/{notificationId}")
    public Result<Void> markAsRead(@PathVariable Long notificationId) {
        return notificationService.markAsRead(notificationId);
    }

    /**
     * 批量标记为已读
     */
    @PostMapping("/readAll/{userId}")
    public Result<Void> markAllAsRead(@PathVariable Long userId) {
        return notificationService.markAllAsRead(userId);
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/{notificationId}")
    public Result<Void> deleteNotification(@PathVariable Long notificationId) {
        return notificationService.deleteNotification(notificationId);
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/unreadCount/{userId}")
    public Result<Integer> getUnreadCount(@PathVariable Long userId) {
        return notificationService.getUnreadCount(userId);
    }
}
