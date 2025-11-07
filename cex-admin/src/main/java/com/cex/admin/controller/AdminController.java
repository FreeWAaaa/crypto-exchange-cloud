package com.cex.admin.controller;

import com.cex.admin.domain.entity.SysConfig;
import com.cex.admin.service.AdminService;
import com.cex.common.core.domain.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理后台控制器
 * 
 * @author cex
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * 获取系统配置列表
     */
    @GetMapping("/config/list")
    public Result<List<SysConfig>> getConfigList(@RequestParam(required = false) Integer configType) {
        return adminService.getConfigList(configType);
    }

    /**
     * 更新系统配置
     */
    @PostMapping("/config/update")
    public Result<Void> updateConfig(@RequestParam String configKey,
                                     @RequestParam String configValue) {
        return adminService.updateConfig(configKey, configValue);
    }

    /**
     * 获取系统统计信息
     */
    @GetMapping("/stats/system")
    public Result<Object> getSystemStats() {
        return adminService.getSystemStats();
    }

    /**
     * 获取用户统计信息
     */
    @GetMapping("/stats/user")
    public Result<Object> getUserStats() {
        return adminService.getUserStats();
    }

    /**
     * 获取交易统计信息
     */
    @GetMapping("/stats/trade")
    public Result<Object> getTradeStats() {
        return adminService.getTradeStats();
    }

    /**
     * 获取钱包统计信息
     */
    @GetMapping("/stats/wallet")
    public Result<Object> getWalletStats() {
        return adminService.getWalletStats();
    }
}
