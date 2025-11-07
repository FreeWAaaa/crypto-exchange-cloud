package com.cex.wallet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cex.wallet.domain.entity.WalletBalance;
import com.cex.wallet.domain.entity.WalletDeposit;
import com.cex.wallet.domain.entity.WalletWithdraw;

import java.math.BigDecimal;
import java.util.List;

/**
 * 钱包服务接口
 * 
 * @author cex
 */
public interface WalletService extends IService<WalletBalance> {

    /**
     * 获取用户资产列表
     */
    List<WalletBalance> getUserBalances(Long userId);

    /**
     * 获取用户指定币种资产
     */
    WalletBalance getUserBalance(Long userId, String coin);

    /**
     * 冻结资产
     */
    void freezeBalance(Long userId, String coin, BigDecimal amount);

    /**
     * 解冻资产
     */
    void unfreezeBalance(Long userId, String coin, BigDecimal amount);

    /**
     * 增加可用余额
     */
    void addAvailableBalance(Long userId, String coin, BigDecimal amount);

    /**
     * 减少可用余额
     */
    void subtractAvailableBalance(Long userId, String coin, BigDecimal amount);

    /**
     * 生成充值地址
     */
    String generateDepositAddress(Long userId, String coin);

    /**
     * 申请提现
     */
    String applyWithdraw(Long userId, String coin, String address, BigDecimal amount, String tradePassword);

    /**
     * 获取充值记录
     */
    List<WalletDeposit> getDepositRecords(Long userId, String coin, Integer page, Integer size);

    /**
     * 获取提现记录
     */
    List<WalletWithdraw> getWithdrawRecords(Long userId, String coin, Integer page, Integer size);

    /**
     * 处理充值确认
     */
    void processDepositConfirm(String txHash, BigDecimal amount, Integer confirmations);

    /**
     * 处理提现
     */
    void processWithdraw(String withdrawNo);
}
