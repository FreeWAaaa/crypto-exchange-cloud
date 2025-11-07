package com.cex.wallet.controller;

import com.cex.common.core.domain.Result;
import com.cex.wallet.domain.entity.WalletBalance;
import com.cex.wallet.service.WalletBalanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 钱包余额Controller
 * 
 * @author cex
 */
@Slf4j
@RestController
@RequestMapping("/api/wallet/balance")
public class WalletBalanceController {

    @Autowired
    private WalletBalanceService balanceService;

    /**
     * 查询用户所有钱包
     */
    @GetMapping("/list")
    public Result<List<WalletBalance>> getBalanceList(@RequestParam Long userId) {
        List<WalletBalance> wallets = balanceService.getUserWallets(userId);
        return Result.success(wallets, "查询成功");
    }

    /**
     * 查询指定币种余额
     */
    @GetMapping("/{coin}")
    public Result<WalletBalance> getBalance(
            @RequestParam Long userId,
            @PathVariable String coin) {
        
        WalletBalance wallet = balanceService.getWallet(userId, coin);
        if (wallet == null) {
            return Result.fail("钱包不存在");
        }
        
        return Result.success(wallet, "查询成功");
    }

    /**
     * 资产汇总（所有币种总价值，需要汇率转换）
     */
    @GetMapping("/summary")
    public Result<Map<String, Object>> getBalanceSummary(@RequestParam Long userId) {
        List<WalletBalance> wallets = balanceService.getUserWallets(userId);
        
        BigDecimal totalValue = BigDecimal.ZERO;
        int coinCount = wallets.size();
        int nonZeroCount = 0;
        
        for (WalletBalance wallet : wallets) {
            if (wallet.getTotalBalance().compareTo(BigDecimal.ZERO) > 0) {
                nonZeroCount++;
            }
            // TODO: 根据汇率计算总价值（需要汇率服务）
            // totalValue = totalValue.add(wallet.getTotalBalance().multiply(rate));
        }
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalValue", totalValue); // 总价值（USDT计价）
        summary.put("coinCount", coinCount);   // 币种数量
        summary.put("nonZeroCount", nonZeroCount); // 非零币种数量
        summary.put("wallets", wallets);
        
        return Result.success(summary, "查询成功");
    }

    /**
     * 创建用户钱包（管理后台使用）
     */
    @PostMapping("/create")
    public Result<Void> createWallet(
            @RequestParam Long userId,
            @RequestBody List<String> coins) {
        
        balanceService.createUserWallets(userId, coins);
        return Result.success("创建成功");
    }

    /**
     * 锁定钱包（管理后台使用）
     */
    @PostMapping("/lock")
    public Result<Void> lockWallet(
            @RequestParam Long userId,
            @RequestParam String coin) {
        
        balanceService.lockWallet(userId, coin);
        log.info("钱包已锁定：userId={}, coin={}", userId, coin);
        return Result.success("钱包已锁定");
    }

    /**
     * 解锁钱包（管理后台使用）
     */
    @PostMapping("/unlock")
    public Result<Void> unlockWallet(
            @RequestParam Long userId,
            @RequestParam String coin) {
        
        balanceService.unlockWallet(userId, coin);
        log.info("钱包已解锁：userId={}, coin={}", userId, coin);
        return Result.success("钱包已解锁");
    }
}

