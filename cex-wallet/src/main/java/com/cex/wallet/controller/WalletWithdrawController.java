package com.cex.wallet.controller;

import com.cex.common.core.domain.Result;
import com.cex.wallet.domain.entity.WalletWithdraw;
import com.cex.wallet.service.WalletWithdrawService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 提现Controller
 * 
 * @author cex
 */
@Slf4j
@RestController
@RequestMapping("/api/wallet/withdraw")
public class WalletWithdrawController {

    @Autowired
    private WalletWithdrawService withdrawService;

    /**
     * 申请提现
     */
    @PostMapping("/apply")
    public Result<String> applyWithdraw(
            @RequestParam Long userId,
            @RequestParam String coin,
            @RequestParam String address,
            @RequestParam BigDecimal amount,
            @RequestParam String tradePassword) {
        
        log.info("用户申请提现：userId={}, coin={}, address={}, amount={}", 
                 userId, coin, address, amount);
        
        String withdrawNo = withdrawService.applyWithdraw(userId, coin, address, amount, tradePassword);
        
        return Result.success(withdrawNo, "提现申请已提交");
    }

    /**
     * 撤销提现
     */
    @PostMapping("/cancel")
    public Result<Void> cancelWithdraw(
            @RequestParam Long userId,
            @RequestParam String withdrawNo) {
        
        withdrawService.cancelWithdraw(userId, withdrawNo);
        
        return Result.success("提现已撤销");
    }

    /**
     * 查询用户提现记录
     */
    @GetMapping("/list")
    public Result<List<WalletWithdraw>> getWithdrawList(@RequestParam Long userId) {
        List<WalletWithdraw> withdraws = withdrawService.getWithdrawList(userId);
        return Result.success(withdraws, "查询成功");
    }

    /**
     * 查询提现详情
     */
    @GetMapping("/{withdrawNo}")
    public Result<WalletWithdraw> getWithdrawDetail(@PathVariable String withdrawNo) {
        WalletWithdraw withdraw = withdrawService.getByWithdrawNo(withdrawNo);
        if (withdraw == null) {
            return Result.fail("提现记录不存在");
        }
        return Result.success(withdraw, "查询成功");
    }

    /**
     * 审核提现（管理后台）
     */
    @PostMapping("/audit")
    public Result<Void> auditWithdraw(
            @RequestParam String withdrawNo,
            @RequestParam Boolean passed,
            @RequestParam Long auditorId,
            @RequestParam String auditor,
            @RequestParam(required = false) String remark) {
        
        log.info("审核提现：withdrawNo={}, passed={}, auditor={}", withdrawNo, passed, auditor);
        
        withdrawService.auditWithdraw(withdrawNo, passed, auditorId, auditor, remark);
        
        return Result.success(passed ? "审核通过" : "审核拒绝");
    }

    /**
     * 发送提现到区块链（管理后台）
     */
    @PostMapping("/send")
    public Result<String> sendWithdraw(@RequestParam String withdrawNo) {
        String txHash = withdrawService.sendWithdraw(withdrawNo);
        return Result.success(txHash, "提现已发送");
    }

    /**
     * 查询待审核的提现（管理后台）
     */
    @GetMapping("/pending")
    public Result<List<WalletWithdraw>> getPendingWithdraws() {
        List<WalletWithdraw> withdraws = withdrawService.getPendingWithdraws();
        return Result.success(withdraws, "查询成功");
    }
}

