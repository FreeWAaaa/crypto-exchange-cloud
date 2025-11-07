package com.cex.user.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cex.common.core.domain.Result;
import com.cex.user.domain.entity.User;
import com.cex.user.domain.entity.UserInviteRelation;
import com.cex.user.mapper.UserInviteRelationMapper;
import com.cex.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户管理后台接口
 * 
 * @author cex
 */
@Slf4j
@RestController
@RequestMapping("/api/user/admin")
@RequiredArgsConstructor
public class UserAdminController {
    
    private final UserMapper userMapper;
    private final UserInviteRelationMapper inviteRelationMapper;

    /**
     * 查询用户列表
     */
    @GetMapping("/list")
    public Result<Object> getUserList(@RequestParam(required = false) Map<String, Object> params) {
        log.info("查询用户列表，参数：{}", params);
        
        // 分页参数
        Integer pageNum = params != null && params.get("pageNum") != null ? 
                Integer.parseInt(params.get("pageNum").toString()) : 1;
        Integer pageSize = params != null && params.get("pageSize") != null ? 
                Integer.parseInt(params.get("pageSize").toString()) : 10;
        
        // 查询条件
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        if (params != null) {
            // 用户名模糊查询
            if (params.get("username") != null) {
                wrapper.like(User::getUsername, params.get("username").toString());
            }
            // 手机号
            if (params.get("mobile") != null) {
                wrapper.like(User::getMobile, params.get("mobile").toString());
            }
            // 邮箱
            if (params.get("email") != null) {
                wrapper.like(User::getEmail, params.get("email").toString());
            }
            // 状态
            if (params.get("status") != null) {
                wrapper.eq(User::getStatus, Integer.parseInt(params.get("status").toString()));
            }
            // 等级
            if (params.get("level") != null) {
                wrapper.eq(User::getLevel, Integer.parseInt(params.get("level").toString()));
            }
        }
        
        wrapper.orderByDesc(User::getCreateTime);
        
        Page<User> page = new Page<>(pageNum, pageSize);
        Page<User> result = userMapper.selectPage(page, wrapper);
        
        return Result.success(result);
    }
    
    /**
     * 查询用户详情
     */
    @GetMapping("/detail/{userId}")
    public Result<Object> getUserDetail(@PathVariable Long userId) {
        log.info("查询用户详情，userId：{}", userId);
        
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        return Result.success(user);
    }
    
    /**
     * 更新用户状态
     */
    @PostMapping("/status")
    public Result<Void> updateUserStatus(@RequestParam Long userId, 
                                          @RequestParam Integer status) {
        log.info("更新用户状态，userId：{}，status：{}", userId, status);
        
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        user.setStatus(status);
        userMapper.updateById(user);
        
        return Result.success();
    }
    
    /**
     * 查询邀请统计
     */
    @GetMapping("/invite/stats")
    public Result<Object> getInviteStats(@RequestParam Long userId) {
        log.info("查询用户邀请统计，userId：{}", userId);
        
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        // 统计邀请数据
        Map<String, Object> stats = new HashMap<>();
        stats.put("userId", userId);
        stats.put("username", user.getUsername());
        stats.put("totalInvites", user.getFirstLevelCount() + user.getSecondLevelCount() + user.getThirdLevelCount());
        stats.put("firstLevelInvites", user.getFirstLevelCount());
        stats.put("secondLevelInvites", user.getSecondLevelCount());
        stats.put("thirdLevelInvites", user.getThirdLevelCount());
        
        // 查询邀请详情
        LambdaQueryWrapper<UserInviteRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserInviteRelation::getInviterId, userId);
        wrapper.orderByDesc(UserInviteRelation::getCreateTime);
        
        List<UserInviteRelation> invites = inviteRelationMapper.selectList(wrapper);
        stats.put("inviteList", invites);
        
        return Result.success(stats);
    }
}

