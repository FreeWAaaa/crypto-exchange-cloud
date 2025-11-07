package com.cex.user.controller;

import com.cex.common.core.domain.Result;
import com.cex.user.domain.dto.UserVerificationDTO;
import com.cex.user.domain.entity.User;
import com.cex.user.service.UserVerificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户实名认证Controller
 * 
 * @author cex
 */
@Slf4j
@RestController
@RequestMapping("/api/user/verification")
public class VerificationController {

    @Autowired
    private UserVerificationService verificationService;

    /**
     * 提交实名认证
     */
    @PostMapping("/submit")
    public Result<Void> submitVerification(
            @RequestParam Long userId,
            @Validated @RequestBody UserVerificationDTO dto) {
        
        log.info("用户提交实名认证：userId={}, realName={}", userId, dto.getRealName());
        
        verificationService.submitVerification(userId, dto);
        
        return Result.success(null, "认证申请已提交，请等待审核");
    }

    /**
     * 查询认证状态
     */
    @GetMapping("/status")
    public Result<User> getVerificationStatus(@RequestParam Long userId) {
        User user = verificationService.getVerificationStatus(userId);
        return Result.success(user, "查询成功");
    }

    /**
     * 审核实名认证（管理后台使用）
     */
    @PostMapping("/audit")
    public Result<Void> auditVerification(
            @RequestParam Long userId,
            @RequestParam Boolean passed,
            @RequestParam(required = false) String reason) {
        
        log.info("审核实名认证：userId={}, passed={}, reason={}", userId, passed, reason);
        
        verificationService.auditVerification(userId, passed, reason);
        
        return Result.success(null, passed ? "认证审核通过" : "认证审核拒绝");
    }
}

