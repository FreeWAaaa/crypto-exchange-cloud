package com.cex.user.controller;

import com.cex.common.core.domain.Result;
import com.cex.user.domain.entity.User;
import com.cex.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户信息Controller
 * 
 * @author cex
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserInfoController {

    private final UserService userService;

    /**
     * 获取用户信息
     */
    @GetMapping("/info")
    public Result<User> getUserInfo(@RequestParam Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        
        // 脱敏处理
        user.setPassword(null);
        user.setSalt(null);
        user.setTradePassword(null);
        user.setGoogleAuthSecret(null);
        
        return Result.success(user);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/info")
    public Result<Void> updateUserInfo(
            @RequestParam Long userId,
            @RequestBody User updateUser) {
        
        User user = userService.getById(userId);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        
        // 只允许更新部分字段
        if (updateUser.getNickname() != null) {
            user.setNickname(updateUser.getNickname());
        }
        if (updateUser.getAvatar() != null) {
            user.setAvatar(updateUser.getAvatar());
        }
        if (updateUser.getCountry() != null) {
            user.setCountry(updateUser.getCountry());
        }
        if (updateUser.getCity() != null) {
            user.setCity(updateUser.getCity());
        }
        
        userService.updateById(user);
        
        log.info("用户信息更新成功：userId={}", userId);
        return Result.success("更新成功");
    }

    /**
     * 上传头像
     */
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(
            @RequestParam Long userId,
            @RequestParam("file") MultipartFile file) {
        
        // TODO: 实现文件上传到OSS
        // String avatarUrl = ossService.upload(file);
        
        String avatarUrl = "https://example.com/avatar/" + System.currentTimeMillis() + ".jpg";
        
        User user = userService.getById(userId);
        if (user != null) {
            user.setAvatar(avatarUrl);
            userService.updateById(user);
        }
        
        log.info("头像上传成功：userId={}, url={}", userId, avatarUrl);
        return Result.success("上传成功", avatarUrl);
    }

    /**
     * 修改密码
     */
    @PostMapping("/password/change")
    public Result<Void> changePassword(
            @RequestParam Long userId,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        
        User user = userService.getById(userId);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        
        // TODO: 验证旧密码并更新新密码
        log.info("用户修改密码：userId={}", userId);
        return Result.success("密码修改成功");
    }

    /**
     * 验证交易密码
     */
    @PostMapping("/trade-password/verify")
    public Result<Boolean> verifyTradePassword(
            @RequestParam Long userId,
            @RequestParam String tradePassword) {
        
        boolean valid = userService.verifyTradePassword(userId, tradePassword);
        return Result.success(valid, valid ? "密码正确" : "密码错误");
    }

    /**
     * 验证谷歌验证码
     */
    @PostMapping("/google-auth/verify")
    public Result<Boolean> verifyGoogleAuth(
            @RequestParam Long userId,
            @RequestParam String code) {
        
        boolean valid = userService.verifyGoogleAuth(userId, code);
        return Result.success(valid, valid ? "验证成功" : "验证失败");
    }

    /**
     * 获取用户统计信息
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getUserStatistics(@RequestParam Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("loginCount", user.getLoginCount());
        statistics.put("transactions", user.getTransactions());
        statistics.put("level", user.getLevel());
        statistics.put("verified", user.getVerified());
        statistics.put("inviteCount", 
            (user.getFirstLevelCount() != null ? user.getFirstLevelCount() : 0) +
            (user.getSecondLevelCount() != null ? user.getSecondLevelCount() : 0) +
            (user.getThirdLevelCount() != null ? user.getThirdLevelCount() : 0)
        );
        
        return Result.success(statistics);
    }
}

