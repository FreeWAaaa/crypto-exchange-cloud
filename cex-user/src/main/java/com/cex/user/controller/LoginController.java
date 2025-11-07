package com.cex.user.controller;

import com.cex.common.core.domain.Result;
import com.cex.user.domain.dto.LoginResponseDTO;
import com.cex.user.domain.dto.UserLoginDTO;
import com.cex.user.domain.entity.User;
import com.cex.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * 用户登录Controller
 * 
 * @author cex
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
public class LoginController {

    @Autowired
    private UserService userService;

    @Value("${app.promote.prefix:http://localhost:8080/#/register?code=}")
    private String promotePrefix;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResponseDTO> login(
            @Validated @RequestBody UserLoginDTO dto,
            HttpServletRequest request) {
        
        log.info("用户登录请求：username={}", dto.getUsername());
        
        // 调用登录服务
        String token = userService.login(dto);
        
        // 查询用户信息
        User user = userService.getUserByUsername(dto.getUsername());
        if (user == null) {
            user = userService.getUserByMobile(dto.getUsername());
        }
        if (user == null) {
            user = userService.getUserByEmail(dto.getUsername());
        }
        
        // 构建响应
        LoginResponseDTO response = LoginResponseDTO.fromUser(user, token, promotePrefix);
        
        // 保存到Session（可选）
        if (dto.getRememberMe() != null && dto.getRememberMe()) {
            HttpSession session = request.getSession();
            session.setAttribute("userId", user.getId());
            session.setAttribute("username", user.getUsername());
        }
        
        log.info("用户登录成功：userId={}, username={}", user.getId(), user.getUsername());
        
        return Result.success(response, "登录成功");
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        
        log.info("用户登出成功");
        return Result.success(null, "登出成功");
    }

    /**
     * 检查登录状态
     */
    @GetMapping("/check-login")
    public Result<Boolean> checkLogin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        boolean loggedIn = session != null && session.getAttribute("userId") != null;
        
        return Result.success(loggedIn, loggedIn ? "已登录" : "未登录");
    }

    /**
     * 刷新Token
     */
    @PostMapping("/refresh-token")
    public Result<String> refreshToken(@RequestHeader("Authorization") String oldToken) {
        // TODO: 实现Token刷新逻辑
        // 1. 验证旧Token
        // 2. 生成新Token
        // 3. 返回新Token
        
        log.info("Token刷新请求");
        return Result.success(oldToken, "Token刷新成功");
    }
}

