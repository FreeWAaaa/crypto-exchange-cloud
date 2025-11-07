package com.cex.notification.controller;

import com.cex.common.core.domain.Result;
import com.cex.notification.domain.entity.SysMessage;
import com.cex.notification.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 站内消息控制器
 * 
 * @author cex
 */
@Slf4j
@RestController
@RequestMapping("/api/notification/message")
@RequiredArgsConstructor
public class MessageController {
    
    private final MessageService messageService;
    
    /**
     * 查询用户消息列表
     */
    @GetMapping("/list")
    public Result<List<SysMessage>> getMessageList(@RequestParam Long userId) {
        List<SysMessage> messages = messageService.getUserMessages(userId);
        return Result.success(messages);
    }
    
    /**
     * 查询未读消息
     */
    @GetMapping("/unread")
    public Result<List<SysMessage>> getUnreadMessages(@RequestParam Long userId) {
        List<SysMessage> messages = messageService.getUnreadMessages(userId);
        return Result.success(messages);
    }
    
    /**
     * 标记为已读
     */
    @PostMapping("/read/{messageId}")
    public Result<Void> markAsRead(@PathVariable Long messageId) {
        messageService.markAsRead(messageId);
        return Result.success();
    }
    
    /**
     * 全部标记为已读
     */
    @PostMapping("/read/all")
    public Result<Void> markAllAsRead(@RequestParam Long userId) {
        messageService.markAllAsRead(userId);
        return Result.success();
    }
    
    /**
     * 删除消息
     */
    @DeleteMapping("/{messageId}")
    public Result<Void> deleteMessage(@PathVariable Long messageId) {
        messageService.deleteMessage(messageId);
        return Result.success();
    }
    
    /**
     * 发送消息（内部接口，供其他服务调用）
     */
    @PostMapping("/send")
    public Result<Void> sendMessage(@RequestParam Long userId,
                                     @RequestParam String title,
                                     @RequestParam String content,
                                     @RequestParam Integer messageType) {
        messageService.sendMessage(userId, title, content, messageType);
        return Result.success();
    }
}

