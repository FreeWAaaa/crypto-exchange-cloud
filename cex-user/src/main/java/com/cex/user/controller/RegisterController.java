package com.cex.user.controller;

import com.cex.common.core.domain.Result;
import com.cex.user.domain.dto.LoginResponseDTO;
import com.cex.user.domain.dto.UserRegisterByEmailDTO;
import com.cex.user.domain.dto.UserRegisterByPhoneDTO;
import com.cex.user.service.UserRegisterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户注册Controller
 * 
 * @author cex
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
public class RegisterController {

    @Autowired
    private UserRegisterService registerService;

    /**
     * 手机号注册
     */
    @PostMapping("/register/phone")
    public Result<LoginResponseDTO> registerByPhone(
            @Validated @RequestBody UserRegisterByPhoneDTO dto,
            HttpServletRequest request) {
        
        log.info("用户注册请求：username={}, mobile={}", dto.getUsername(), dto.getMobile());
        
        String clientIp = getClientIp(request);
        LoginResponseDTO response = registerService.registerByPhone(dto, clientIp);
        
        return Result.success(response, "注册成功");
    }

    /**
     * 邮箱注册
     */
    @PostMapping("/register/email")
    public Result<LoginResponseDTO> registerByEmail(
            @Validated @RequestBody UserRegisterByEmailDTO dto,
            HttpServletRequest request) {
        
        log.info("用户注册请求（邮箱）：username={}, email={}", dto.getUsername(), dto.getEmail());
        
        String clientIp = getClientIp(request);
        LoginResponseDTO response = registerService.registerByEmail(dto, clientIp);
        
        return Result.success(response, "注册成功");
    }

    /**
     * 检查用户名是否存在
     */
    @GetMapping("/check/username")
    public Result<Boolean> checkUsername(@RequestParam String username) {
        boolean exists = registerService.isUsernameExist(username);
        return Result.success(exists, exists ? "用户名已存在" : "用户名可用");
    }

    /**
     * 检查手机号是否存在
     */
    @GetMapping("/check/mobile")
    public Result<Boolean> checkMobile(@RequestParam String mobile) {
        boolean exists = registerService.isMobileExist(mobile);
        return Result.success(exists, exists ? "手机号已注册" : "手机号可用");
    }

    /**
     * 检查邮箱是否存在
     */
    @GetMapping("/check/email")
    public Result<Boolean> checkEmail(@RequestParam String email) {
        boolean exists = registerService.isEmailExist(email);
        return Result.success(exists, exists ? "邮箱已注册" : "邮箱可用");
    }

    /**
     * 验证邀请码
     */
    @GetMapping("/check/invite-code")
    public Result<Boolean> checkInviteCode(@RequestParam String inviteCode) {
        boolean valid = registerService.isInviteCodeValid(inviteCode);
        return Result.success(valid, valid ? "邀请码有效" : "邀请码无效");
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}

