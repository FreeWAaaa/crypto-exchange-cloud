package com.cex.admin.controller;

import com.cex.admin.client.WalletFeignClient;
import com.cex.common.core.domain.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 钱包管理控制器
 * 
 * @author cex
 */
@Slf4j
@RestController
@RequestMapping("/admin/wallet")
@RequiredArgsConstructor
@Api(tags = "钱包管理")
public class WalletManageController {
    
    private final WalletFeignClient walletFeignClient;
    
    /**
     * 用户钱包列表
     */
    @GetMapping("/balance/user/{userId}")
    @ApiOperation("查询用户钱包列表")
    public Result<Object> getUserWallets(@PathVariable Long userId) {
        log.info("管理员查询用户钱包列表，userId：{}", userId);
        return walletFeignClient.getUserWallets(userId);
    }
    
    /**
     * 钱包余额列表
     */
    @GetMapping("/balance/list")
    @ApiOperation("查询钱包余额列表")
    public Result<Object> getBalanceList(@RequestParam(required = false) Map<String, Object> params) {
        log.info("管理员查询钱包余额列表，参数：{}", params);
        return walletFeignClient.getBalanceList(params);
    }
    
    /**
     * 充值记录列表
     */
    @GetMapping("/deposit/list")
    @ApiOperation("查询充值记录列表")
    public Result<Object> getDepositList(@RequestParam(required = false) Map<String, Object> params) {
        log.info("管理员查询充值记录列表，参数：{}", params);
        return walletFeignClient.getDepositList(params);
    }
    
    /**
     * 充值详情
     */
    @GetMapping("/deposit/detail/{id}")
    @ApiOperation("查询充值详情")
    public Result<Object> getDepositDetail(@PathVariable Long id) {
        log.info("管理员查询充值详情，id：{}", id);
        return walletFeignClient.getDepositDetail(id);
    }
    
    /**
     * 提现记录列表
     */
    @GetMapping("/withdraw/list")
    @ApiOperation("查询提现记录列表")
    public Result<Object> getWithdrawList(@RequestParam(required = false) Map<String, Object> params) {
        log.info("管理员查询提现记录列表，参数：{}", params);
        return walletFeignClient.getWithdrawList(params);
    }
    
    /**
     * 提现详情
     */
    @GetMapping("/withdraw/detail/{id}")
    @ApiOperation("查询提现详情")
    public Result<Object> getWithdrawDetail(@PathVariable Long id) {
        log.info("管理员查询提现详情，id：{}", id);
        return walletFeignClient.getWithdrawDetail(id);
    }
    
    /**
     * 审核提现申请
     */
    @PostMapping("/withdraw/audit")
    @ApiOperation("审核提现申请")
    public Result<Void> auditWithdraw(@RequestParam Long id,
                                       @RequestParam Integer status,
                                       @RequestParam(required = false) String txHash,
                                       @RequestParam(required = false) String remark) {
        log.info("管理员审核提现申请，id：{}，status：{}，txHash：{}，remark：{}", 
                 id, status, txHash, remark);
        return walletFeignClient.auditWithdraw(id, status, txHash, remark);
    }
    
    /**
     * 资产流水列表
     */
    @GetMapping("/transaction/list")
    @ApiOperation("查询资产流水列表")
    public Result<Object> getTransactionList(@RequestParam(required = false) Map<String, Object> params) {
        log.info("管理员查询资产流水列表，参数：{}", params);
        return walletFeignClient.getTransactionList(params);
    }
}

