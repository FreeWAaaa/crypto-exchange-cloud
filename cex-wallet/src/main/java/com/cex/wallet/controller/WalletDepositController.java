package com.cex.wallet.controller;

import com.cex.common.core.domain.Result;
import com.cex.wallet.domain.entity.WalletDeposit;
import com.cex.wallet.service.WalletDepositService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 充值Controller
 * 
 * @author cex
 */
@Slf4j
@RestController
@RequestMapping("/api/wallet/deposit")
public class WalletDepositController {

    @Autowired
    private WalletDepositService depositService;

    /**
     * 查询用户充值记录
     */
    @GetMapping("/list")
    public Result<List<WalletDeposit>> getDepositList(@RequestParam Long userId) {
        List<WalletDeposit> deposits = depositService.getDepositList(userId);
        return Result.success(deposits, "查询成功");
    }

    /**
     * 查询用户某币种充值记录
     */
    @GetMapping("/list/{coin}")
    public Result<List<WalletDeposit>> getDepositListByCoin(
            @RequestParam Long userId,
            @PathVariable String coin) {
        
        List<WalletDeposit> deposits = depositService.getDepositListByCoin(userId, coin);
        return Result.success(deposits, "查询成功");
    }

    /**
     * 查询充值详情
     */
    @GetMapping("/{depositNo}")
    public Result<WalletDeposit> getDepositDetail(@PathVariable String depositNo) {
        WalletDeposit deposit = depositService.getByDepositNo(depositNo);
        if (deposit == null) {
            return Result.fail("充值记录不存在");
        }
        return Result.success(deposit, "查询成功");
    }
}

