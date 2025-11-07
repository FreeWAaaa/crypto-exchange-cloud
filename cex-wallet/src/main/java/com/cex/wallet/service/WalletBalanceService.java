package com.cex.wallet.service;

import com.cex.wallet.domain.entity.WalletBalance;

import java.math.BigDecimal;
import java.util.List;

/**
 * 钱包余额服务接口
 * 
 * @author cex
 */
public interface WalletBalanceService {

    /**
     * 创建用户钱包（注册时调用）
     * 
     * @param userId 用户ID
     * @param coins 币种列表
     */
    void createUserWallets(Long userId, List<String> coins);

    /**
     * 获取用户钱包
     * 
     * @param userId 用户ID
     * @param coin 币种
     * @return 钱包对象
     */
    WalletBalance getWallet(Long userId, String coin);

    /**
     * 获取用户所有钱包
     * 
     * @param userId 用户ID
     * @return 钱包列表
     */
    List<WalletBalance> getUserWallets(Long userId);

    /**
     * 增加可用余额
     * 
     * @param userId 用户ID
     * @param coin 币种
     * @param amount 金额
     * @param relatedId 关联ID（充值单号等）
     * @param remark 备注
     */
    void increaseBalance(Long userId, String coin, BigDecimal amount, String relatedId, String remark);

    /**
     * 减少可用余额
     * 
     * @param userId 用户ID
     * @param coin 币种
     * @param amount 金额
     * @param relatedId 关联ID
     * @param remark 备注
     */
    void decreaseBalance(Long userId, String coin, BigDecimal amount, String relatedId, String remark);

    /**
     * 冻结余额
     * 
     * @param userId 用户ID
     * @param coin 币种
     * @param amount 金额
     * @param relatedId 关联ID
     * @param remark 备注
     */
    void freezeBalance(Long userId, String coin, BigDecimal amount, String relatedId, String remark);

    /**
     * 解冻余额
     * 
     * @param userId 用户ID
     * @param coin 币种
     * @param amount 金额
     * @param relatedId 关联ID
     * @param remark 备注
     */
    void unfreezeBalance(Long userId, String coin, BigDecimal amount, String relatedId, String remark);

    /**
     * 扣减冻结余额
     * 
     * @param userId 用户ID
     * @param coin 币种
     * @param amount 金额
     * @param relatedId 关联ID
     * @param remark 备注
     */
    void decreaseFrozen(Long userId, String coin, BigDecimal amount, String relatedId, String remark);

    /**
     * 锁定钱包
     * 
     * @param userId 用户ID
     * @param coin 币种
     */
    void lockWallet(Long userId, String coin);

    /**
     * 解锁钱包
     * 
     * @param userId 用户ID
     * @param coin 币种
     */
    void unlockWallet(Long userId, String coin);
}

