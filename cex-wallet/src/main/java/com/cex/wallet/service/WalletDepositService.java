package com.cex.wallet.service;

import com.cex.wallet.domain.entity.WalletDeposit;

import java.math.BigDecimal;
import java.util.List;

/**
 * 充值服务接口
 * 
 * @author cex
 */
public interface WalletDepositService {

    /**
     * 处理充值（从消息队列监听到）
     * 
     * @param userId 用户ID
     * @param coin 币种
     * @param address 充值地址
     * @param amount 充值金额
     * @param txHash 交易哈希
     */
    void handleDeposit(Long userId, String coin, String address, BigDecimal amount, String txHash);

    /**
     * 查询用户充值记录
     * 
     * @param userId 用户ID
     * @return 充值记录列表
     */
    List<WalletDeposit> getDepositList(Long userId);

    /**
     * 查询用户某币种充值记录
     * 
     * @param userId 用户ID
     * @param coin 币种
     * @return 充值记录列表
     */
    List<WalletDeposit> getDepositListByCoin(Long userId, String coin);

    /**
     * 根据充值单号查询
     * 
     * @param depositNo 充值单号
     * @return 充值记录
     */
    WalletDeposit getByDepositNo(String depositNo);

    /**
     * 更新充值确认数
     * 
     * @param depositNo 充值单号
     * @param confirmations 确认数
     */
    void updateConfirmations(String depositNo, Integer confirmations);
}

