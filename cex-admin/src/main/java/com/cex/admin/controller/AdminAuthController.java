package com.cex.admin.controller;

import com.cex.admin.domain.entity.AdminUser;
import com.cex.admin.service.AdminAuthService;
import com.cex.common.core.domain.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理员认证控制器
 * 
 * @author cex
 */
@Slf4j
@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
@Api(tags = "管理员认证")
public class AdminAuthController {
    
    private final AdminAuthService adminAuthService;
    
    /**
     * 管理员登录
     */
    @PostMapping("/login")
    @ApiOperation("管理员登录")
    public Result<Map<String, Object>> login(@RequestParam String username,
                                              @RequestParam String password) {
        log.info("管理员登录：username={}", username);
        
        Map<String, Object> result = adminAuthService.login(username, password);
        return Result.success(result);
    }
    
    /**
     * 管理员登出
     */
    @PostMapping("/logout")
    @ApiOperation("管理员登出")
    public Result<Void> logout(@RequestHeader("Authorization") String token) {
        log.info("管理员登出");
        adminAuthService.logout(token);
        return Result.success();
    }
    
    /**
     * 获取当前管理员信息
     */
    @GetMapping("/info")
    @ApiOperation("获取当前管理员信息")
    public Result<AdminUser> getAdminInfo(@RequestHeader("Authorization") String token) {
        log.info("获取管理员信息");
        AdminUser admin = adminAuthService.getAdminInfo(token);
        return Result.success(admin);
    }
    
    /**
     * 修改密码
     */
    @PostMapping("/password/change")
    @ApiOperation("修改密码")
    public Result<Void> changePassword(@RequestHeader("Authorization") String token,
                                        @RequestParam String oldPassword,
                                        @RequestParam String newPassword) {
        log.info("管理员修改密码");
        adminAuthService.changePassword(token, oldPassword, newPassword);
        return Result.success();
    }
}

