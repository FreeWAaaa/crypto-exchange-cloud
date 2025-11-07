package com.cex.admin.controller;

import com.cex.admin.client.UserFeignClient;
import com.cex.common.core.domain.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户管理控制器
 * 
 * @author cex
 */
@Slf4j
@RestController
@RequestMapping("/admin/user")
@RequiredArgsConstructor
public class UserManageController {
    
    private final UserFeignClient userFeignClient;
    
    /**
     * 用户列表
     */
    @GetMapping("/list")
    public Result<Object> getUserList(@RequestParam(required = false) Map<String, Object> params) {
        log.info("管理员查询用户列表，参数：{}", params);
        return userFeignClient.getUserList(params);
    }
    
    /**
     * 用户详情
     */
    @GetMapping("/detail/{userId}")
    public Result<Object> getUserDetail(@PathVariable Long userId) {
        log.info("管理员查询用户详情，userId：{}", userId);
        return userFeignClient.getUserDetail(userId);
    }
    
    /**
     * 更新用户状态
     */
    @PostMapping("/status")
    public Result<Void> updateUserStatus(@RequestParam Long userId, 
                                          @RequestParam Integer status) {
        log.info("管理员更新用户状态，userId：{}，status：{}", userId, status);
        return userFeignClient.updateUserStatus(userId, status);
    }
    
    /**
     * 查询邀请统计
     */
    @GetMapping("/invite/stats/{userId}")
    public Result<Object> getInviteStats(@PathVariable Long userId) {
        log.info("管理员查询用户邀请统计，userId：{}", userId);
        return userFeignClient.getInviteStats(userId);
    }
}

