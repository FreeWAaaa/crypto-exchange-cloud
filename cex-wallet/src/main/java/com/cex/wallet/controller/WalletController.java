package com.cex.wallet.controller;

import com.cex.common.core.domain.Result;
import com.cex.wallet.domain.entity.WalletBalance;
import com.cex.wallet.domain.entity.WalletDeposit;
import com.cex.wallet.domain.entity.WalletWithdraw;
import com.cex.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.List;

/**
 * 钱包控制器
 * 
 * @author cex
 */
@Slf4j
@RestController
@RequestMapping("/wallet")
@RequiredArgsConstructor
@Validated
public class WalletController {

    private final WalletService walletService;

    /**
     * 获取用户资产列表
     */
    @GetMapping("/balance/list")
    public Result<List<WalletBalance>> getUserBalances(@RequestParam Long userId) {
        List<WalletBalance> balances = walletService.getUserBalances(userId);
        return Result.success(balances);
    }

    /**
     * 获取用户指定币种资产
     */
    @GetMapping("/balance/detail")
    public Result<WalletBalance> getUserBalance(@RequestParam Long userId, @RequestParam String coin) {
        WalletBalance balance = walletService.getUserBalance(userId, coin);
        return Result.success(balance);
    }

    /**
     * 生成充值地址
     */
    @PostMapping("/deposit/address")
    public Result<String> generateDepositAddress(@RequestParam Long userId, @RequestParam String coin) {
        String address = walletService.generateDepositAddress(userId, coin);
        return Result.success("生成充值地址成功", address);
    }

    /**
     * 申请提现
     */
    @PostMapping("/withdraw/apply")
    public Result<String> applyWithdraw(@RequestParam Long userId,
                                       @RequestParam String coin,
                                       @RequestParam String address,
                                       @RequestParam @DecimalMin(value = "0.00000001", message = "提现数量必须大于0") BigDecimal amount,
                                       @RequestParam @NotBlank(message = "交易密码不能为空") String tradePassword) {
        String withdrawNo = walletService.applyWithdraw(userId, coin, address, amount, tradePassword);
        return Result.success("提现申请成功", withdrawNo);
    }

    /**
     * 获取充值记录
     */
    @GetMapping("/deposit/records")
    public Result<List<WalletDeposit>> getDepositRecords(@RequestParam Long userId,
                                                         @RequestParam(required = false) String coin,
                                                         @RequestParam(defaultValue = "1") Integer page,
                                                         @RequestParam(defaultValue = "20") Integer size) {
        List<WalletDeposit> records = walletService.getDepositRecords(userId, coin, page, size);
        return Result.success(records);
    }

    /**
     * 获取提现记录
     */
    @GetMapping("/withdraw/records")
    public Result<List<WalletWithdraw>> getWithdrawRecords(@RequestParam Long userId,
                                                           @RequestParam(required = false) String coin,
                                                           @RequestParam(defaultValue = "1") Integer page,
                                                           @RequestParam(defaultValue = "20") Integer size) {
        List<WalletWithdraw> records = walletService.getWithdrawRecords(userId, coin, page, size);
        return Result.success(records);
    }
}
