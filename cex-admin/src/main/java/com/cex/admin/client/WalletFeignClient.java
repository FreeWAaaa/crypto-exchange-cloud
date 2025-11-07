package com.cex.admin.client;

import com.cex.common.core.domain.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 钱包服务 Feign 客户端
 * 
 * @author cex
 */
@FeignClient(name = "cex-wallet", path = "/api/wallet")
public interface WalletFeignClient {
    
    /**
     * 查询用户钱包列表
     */
    @GetMapping("/admin/balance/user/{userId}")
    Result<Object> getUserWallets(@PathVariable("userId") Long userId);
    
    /**
     * 查询所有钱包余额
     */
    @GetMapping("/admin/balance/list")
    Result<Object> getBalanceList(@RequestParam Map<String, Object> params);
    
    /**
     * 查询充值记录列表
     */
    @GetMapping("/admin/deposit/list")
    Result<Object> getDepositList(@RequestParam Map<String, Object> params);
    
    /**
     * 查询充值详情
     */
    @GetMapping("/admin/deposit/detail/{id}")
    Result<Object> getDepositDetail(@PathVariable("id") Long id);
    
    /**
     * 查询提现记录列表
     */
    @GetMapping("/admin/withdraw/list")
    Result<Object> getWithdrawList(@RequestParam Map<String, Object> params);
    
    /**
     * 查询提现详情
     */
    @GetMapping("/admin/withdraw/detail/{id}")
    Result<Object> getWithdrawDetail(@PathVariable("id") Long id);
    
    /**
     * 审核提现申请
     */
    @PostMapping("/admin/withdraw/audit")
    Result<Void> auditWithdraw(@RequestParam("id") Long id,
                                @RequestParam("status") Integer status,
                                @RequestParam(value = "txHash", required = false) String txHash,
                                @RequestParam(value = "remark", required = false) String remark);
    
    /**
     * 查询资产流水
     */
    @GetMapping("/admin/transaction/list")
    Result<Object> getTransactionList(@RequestParam Map<String, Object> params);
}

