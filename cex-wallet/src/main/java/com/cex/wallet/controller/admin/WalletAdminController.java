package com.cex.wallet.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cex.common.core.domain.Result;
import com.cex.wallet.domain.entity.WalletBalance;
import com.cex.wallet.domain.entity.WalletDeposit;
import com.cex.wallet.domain.entity.WalletTransaction;
import com.cex.wallet.domain.entity.WalletWithdraw;
import com.cex.wallet.mapper.WalletBalanceMapper;
import com.cex.wallet.mapper.WalletDepositMapper;
import com.cex.wallet.mapper.WalletTransactionMapper;
import com.cex.wallet.mapper.WalletWithdrawMapper;
import com.cex.wallet.service.WalletWithdrawService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 钱包管理后台接口
 * 
 * @author cex
 */
@Slf4j
@RestController
@RequestMapping("/api/wallet/admin")
@RequiredArgsConstructor
public class WalletAdminController {
    
    private final WalletBalanceMapper balanceMapper;
    private final WalletDepositMapper depositMapper;
    private final WalletWithdrawMapper withdrawMapper;
    private final WalletTransactionMapper transactionMapper;
    private final WalletWithdrawService withdrawService;
    
    /**
     * 查询用户钱包列表
     */
    @GetMapping("/balance/user/{userId}")
    @ApiOperation("查询用户钱包列表")
    public Result<Object> getUserWallets(@PathVariable Long userId) {
        log.info("查询用户钱包列表，userId：{}", userId);
        
        LambdaQueryWrapper<WalletBalance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WalletBalance::getUserId, userId);
        wrapper.orderByDesc(WalletBalance::getCreateTime);
        
        List<WalletBalance> wallets = balanceMapper.selectList(wrapper);
        return Result.success(wallets);
    }
    
    /**
     * 查询钱包余额列表
     */
    @GetMapping("/balance/list")
    @ApiOperation("查询钱包余额列表")
    public Result<Object> getBalanceList(@RequestParam(required = false) Map<String, Object> params) {
        log.info("查询钱包余额列表，参数：{}", params);
        
        // 分页参数
        Integer pageNum = params != null && params.get("pageNum") != null ? 
                Integer.parseInt(params.get("pageNum").toString()) : 1;
        Integer pageSize = params != null && params.get("pageSize") != null ? 
                Integer.parseInt(params.get("pageSize").toString()) : 10;
        
        // 查询条件
        LambdaQueryWrapper<WalletBalance> wrapper = new LambdaQueryWrapper<>();
        
        if (params != null) {
            // 用户ID
            if (params.get("userId") != null) {
                wrapper.eq(WalletBalance::getUserId, Long.parseLong(params.get("userId").toString()));
            }
            // 币种
            if (params.get("coin") != null) {
                wrapper.eq(WalletBalance::getCoin, params.get("coin").toString());
            }
        }
        
        wrapper.orderByDesc(WalletBalance::getCreateTime);
        
        Page<WalletBalance> page = new Page<>(pageNum, pageSize);
        Page<WalletBalance> result = balanceMapper.selectPage(page, wrapper);
        
        return Result.success(result);
    }
    
    /**
     * 查询充值记录列表
     */
    @GetMapping("/deposit/list")
    @ApiOperation("查询充值记录列表")
    public Result<Object> getDepositList(@RequestParam(required = false) Map<String, Object> params) {
        log.info("查询充值记录列表，参数：{}", params);
        
        // 分页参数
        Integer pageNum = params != null && params.get("pageNum") != null ? 
                Integer.parseInt(params.get("pageNum").toString()) : 1;
        Integer pageSize = params != null && params.get("pageSize") != null ? 
                Integer.parseInt(params.get("pageSize").toString()) : 10;
        
        // 查询条件
        LambdaQueryWrapper<WalletDeposit> wrapper = new LambdaQueryWrapper<>();
        
        if (params != null) {
            // 用户ID
            if (params.get("userId") != null) {
                wrapper.eq(WalletDeposit::getUserId, Long.parseLong(params.get("userId").toString()));
            }
            // 币种
            if (params.get("coin") != null) {
                wrapper.eq(WalletDeposit::getCoin, params.get("coin").toString());
            }
            // 状态
            if (params.get("status") != null) {
                wrapper.eq(WalletDeposit::getStatus, Integer.parseInt(params.get("status").toString()));
            }
        }
        
        wrapper.orderByDesc(WalletDeposit::getCreateTime);
        
        Page<WalletDeposit> page = new Page<>(pageNum, pageSize);
        Page<WalletDeposit> result = depositMapper.selectPage(page, wrapper);
        
        return Result.success(result);
    }
    
    /**
     * 查询充值详情
     */
    @GetMapping("/deposit/detail/{id}")
    @ApiOperation("查询充值详情")
    public Result<Object> getDepositDetail(@PathVariable Long id) {
        log.info("查询充值详情，id：{}", id);
        
        WalletDeposit deposit = depositMapper.selectById(id);
        if (deposit == null) {
            return Result.error("充值记录不存在");
        }
        
        return Result.success(deposit);
    }
    
    /**
     * 查询提现记录列表
     */
    @GetMapping("/withdraw/list")
    @ApiOperation("查询提现记录列表")
    public Result<Object> getWithdrawList(@RequestParam(required = false) Map<String, Object> params) {
        log.info("查询提现记录列表，参数：{}", params);
        
        // 分页参数
        Integer pageNum = params != null && params.get("pageNum") != null ? 
                Integer.parseInt(params.get("pageNum").toString()) : 1;
        Integer pageSize = params != null && params.get("pageSize") != null ? 
                Integer.parseInt(params.get("pageSize").toString()) : 10;
        
        // 查询条件
        LambdaQueryWrapper<WalletWithdraw> wrapper = new LambdaQueryWrapper<>();
        
        if (params != null) {
            // 用户ID
            if (params.get("userId") != null) {
                wrapper.eq(WalletWithdraw::getUserId, Long.parseLong(params.get("userId").toString()));
            }
            // 币种
            if (params.get("coin") != null) {
                wrapper.eq(WalletWithdraw::getCoin, params.get("coin").toString());
            }
            // 状态
            if (params.get("status") != null) {
                wrapper.eq(WalletWithdraw::getStatus, Integer.parseInt(params.get("status").toString()));
            }
        }
        
        wrapper.orderByDesc(WalletWithdraw::getCreateTime);
        
        Page<WalletWithdraw> page = new Page<>(pageNum, pageSize);
        Page<WalletWithdraw> result = withdrawMapper.selectPage(page, wrapper);
        
        return Result.success(result);
    }
    
    /**
     * 查询提现详情
     */
    @GetMapping("/withdraw/detail/{id}")
    @ApiOperation("查询提现详情")
    public Result<Object> getWithdrawDetail(@PathVariable Long id) {
        log.info("查询提现详情，id：{}", id);
        
        WalletWithdraw withdraw = withdrawMapper.selectById(id);
        if (withdraw == null) {
            return Result.error("提现记录不存在");
        }
        
        return Result.success(withdraw);
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
        log.info("审核提现申请，id：{}，status：{}，txHash：{}，remark：{}", 
                 id, status, txHash, remark);
        
        try {
            if (status == 1) {  // 通过
                withdrawService.auditPass(id, txHash != null ? txHash : "");
            } else if (status == 2) {  // 拒绝
                withdrawService.auditReject(id, remark != null ? remark : "审核不通过");
            } else {
                return Result.error("状态参数错误");
            }
            
            return Result.success();
        } catch (Exception e) {
            log.error("审核提现申请失败", e);
            return Result.error("审核失败：" + e.getMessage());
        }
    }
    
    /**
     * 查询资产流水列表
     */
    @GetMapping("/transaction/list")
    @ApiOperation("查询资产流水列表")
    public Result<Object> getTransactionList(@RequestParam(required = false) Map<String, Object> params) {
        log.info("查询资产流水列表，参数：{}", params);
        
        // 分页参数
        Integer pageNum = params != null && params.get("pageNum") != null ? 
                Integer.parseInt(params.get("pageNum").toString()) : 1;
        Integer pageSize = params != null && params.get("pageSize") != null ? 
                Integer.parseInt(params.get("pageSize").toString()) : 10;
        
        // 查询条件
        LambdaQueryWrapper<WalletTransaction> wrapper = new LambdaQueryWrapper<>();
        
        if (params != null) {
            // 用户ID
            if (params.get("userId") != null) {
                wrapper.eq(WalletTransaction::getUserId, Long.parseLong(params.get("userId").toString()));
            }
            // 币种
            if (params.get("coin") != null) {
                wrapper.eq(WalletTransaction::getCoin, params.get("coin").toString());
            }
            // 流水类型
            if (params.get("flowType") != null) {
                wrapper.eq(WalletTransaction::getFlowType, Integer.parseInt(params.get("flowType").toString()));
            }
        }
        
        wrapper.orderByDesc(WalletTransaction::getCreateTime);
        
        Page<WalletTransaction> page = new Page<>(pageNum, pageSize);
        Page<WalletTransaction> result = transactionMapper.selectPage(page, wrapper);
        
        return Result.success(result);
    }
}

