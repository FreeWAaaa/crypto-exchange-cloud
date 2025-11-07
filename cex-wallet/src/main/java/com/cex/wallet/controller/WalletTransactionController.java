package com.cex.wallet.controller;

import com.cex.common.core.domain.Result;
import com.cex.wallet.domain.entity.WalletTransaction;
import com.cex.wallet.mapper.WalletTransactionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 钱包流水Controller
 * 
 * @author cex
 */
@Slf4j
@RestController
@RequestMapping("/api/wallet/transaction")
public class WalletTransactionController {

    @Autowired
    private WalletTransactionMapper transactionMapper;

    /**
     * 查询用户流水记录
     */
    @GetMapping("/list")
    public Result<List<WalletTransaction>> getTransactionList(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "100") Integer limit) {
        
        List<WalletTransaction> transactions = transactionMapper.selectByUserId(userId, limit);
        return Result.success(transactions, "查询成功");
    }

    /**
     * 查询用户某币种流水
     */
    @GetMapping("/list/{coin}")
    public Result<List<WalletTransaction>> getTransactionListByCoin(
            @RequestParam Long userId,
            @PathVariable String coin,
            @RequestParam(defaultValue = "100") Integer limit) {
        
        List<WalletTransaction> transactions = transactionMapper.selectByUserIdAndCoin(userId, coin, limit);
        return Result.success(transactions, "查询成功");
    }

    /**
     * 查询用户某类型流水
     */
    @GetMapping("/list/type/{flowType}")
    public Result<List<WalletTransaction>> getTransactionListByType(
            @RequestParam Long userId,
            @PathVariable Integer flowType) {
        
        List<WalletTransaction> transactions = transactionMapper.selectByUserIdAndType(userId, flowType);
        return Result.success(transactions, "查询成功");
    }
}

