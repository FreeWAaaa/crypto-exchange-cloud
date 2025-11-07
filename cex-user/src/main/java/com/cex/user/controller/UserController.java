package com.cex.user.controller;

import com.cex.common.core.domain.Result;
import com.cex.user.domain.dto.UserLoginDTO;
import com.cex.user.domain.dto.UserRegisterDTO;
import com.cex.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 用户控制器
 * 
 * @author cex
 */
@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody UserRegisterDTO registerDTO) {
        userService.register(registerDTO);
        return Result.success();
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<String> login(@Valid @RequestBody UserLoginDTO loginDTO) {
        String token = userService.login(loginDTO);
        return Result.success("登录成功", token);
    }

    /**
     * 发送短信验证码
     */
    @PostMapping("/sms/send")
    public Result<Void> sendSmsCode(@RequestParam String mobile) {
        userService.sendSmsCode(mobile);
        return Result.success();
    }

    /**
     * 用户实名认证
     */
    @PostMapping("/verify")
    public Result<Void> verifyRealName(@RequestParam Long userId,
                                      @RequestParam String realName,
                                      @RequestParam String idCard) {
        userService.verifyRealName(userId, realName, idCard);
        return Result.success();
    }

    /**
     * 设置交易密码
     */
    @PostMapping("/trade-password/set")
    public Result<Void> setTradePassword(@RequestParam Long userId,
                                        @RequestParam String tradePassword) {
        userService.setTradePassword(userId, tradePassword);
        return Result.success();
    }

    /**
     * 启用谷歌验证器
     */
    @PostMapping("/google-auth/enable")
    public Result<String> enableGoogleAuth(@RequestParam Long userId) {
        String secret = userService.enableGoogleAuth(userId);
        return Result.success("谷歌验证器启用成功", secret);
    }
}
