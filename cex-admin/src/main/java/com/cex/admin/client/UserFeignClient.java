package com.cex.admin.client;

import com.cex.common.core.domain.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户服务 Feign 客户端
 * 
 * @author cex
 */
@FeignClient(name = "cex-user", path = "/api/user")
public interface UserFeignClient {
    
    /**
     * 查询用户列表
     */
    @GetMapping("/admin/list")
    Result<Object> getUserList(@RequestParam Map<String, Object> params);
    
    /**
     * 查询用户详情
     */
    @GetMapping("/admin/detail/{userId}")
    Result<Object> getUserDetail(@PathVariable("userId") Long userId);
    
    /**
     * 更新用户状态
     */
    @PostMapping("/admin/status")
    Result<Void> updateUserStatus(@RequestParam("userId") Long userId, 
                                   @RequestParam("status") Integer status);
    
    /**
     * 查询实名认证列表
     */
    @GetMapping("/admin/verification/list")
    Result<Object> getVerificationList(@RequestParam Map<String, Object> params);
    
    /**
     * 审核实名认证
     */
    @PostMapping("/admin/verification/audit")
    Result<Void> auditVerification(@RequestParam("id") Long id,
                                    @RequestParam("status") Integer status,
                                    @RequestParam(value = "remark", required = false) String remark);
    
    /**
     * 查询邀请统计
     */
    @GetMapping("/admin/invite/stats")
    Result<Object> getInviteStats(@RequestParam("userId") Long userId);
}

