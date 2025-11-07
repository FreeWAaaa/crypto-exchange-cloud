package com.cex.wallet.service.impl;

import com.cex.wallet.domain.entity.WalletDeposit;
import com.cex.wallet.mapper.WalletDepositMapper;
import com.cex.wallet.service.WalletBalanceService;
import com.cex.wallet.service.WalletDepositService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 充值服务实现
 * 
 * @author cex
 */
@Slf4j
@Service
public class WalletDepositServiceImpl implements WalletDepositService {

    @Autowired
    private WalletDepositMapper depositMapper;

    @Autowired
    private WalletBalanceService balanceService;

    // 默认需要确认数（可从配置读取）
    private static final int DEFAULT_CONFIRMATIONS = 3;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleDeposit(Long userId, String coin, String address, BigDecimal amount, String txHash) {
        log.info("处理充值：userId={}, coin={}, amount={}, txHash={}", userId, coin, amount, txHash);

        // 1. 防止重复充值
        WalletDeposit existDeposit = depositMapper.selectByTxHash(txHash);
        if (existDeposit != null) {
            log.warn("充值记录已存在，忽略：txHash={}", txHash);
            return;
        }

        // 2. 创建充值记录
        WalletDeposit deposit = new WalletDeposit();
        deposit.setDepositNo("D" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        deposit.setUserId(userId);
        deposit.setCoin(coin);
        deposit.setAddress(address);
        deposit.setAmount(amount);
        deposit.setConfirmAmount(amount);
        deposit.setConfirmations(0);
        deposit.setNeedConfirmations(DEFAULT_CONFIRMATIONS);
        deposit.setTxHash(txHash);
        deposit.setStatus(0); // 待确认
        depositMapper.insert(deposit);

        log.info("充值记录创建成功：depositNo={}", deposit.getDepositNo());

        // 3. 如果确认数达标，直接到账（模拟，实际应该等待区块链确认）
        // TODO: 实际应该由区块链监控服务推送确认消息
        confirmDeposit(deposit.getDepositNo(), DEFAULT_CONFIRMATIONS);
    }

    @Override
    public List<WalletDeposit> getDepositList(Long userId) {
        return depositMapper.selectByUserId(userId);
    }

    @Override
    public List<WalletDeposit> getDepositListByCoin(Long userId, String coin) {
        return depositMapper.selectByUserIdAndCoin(userId, coin);
    }

    @Override
    public WalletDeposit getByDepositNo(String depositNo) {
        return depositMapper.selectByDepositNo(depositNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConfirmations(String depositNo, Integer confirmations) {
        WalletDeposit deposit = depositMapper.selectByDepositNo(depositNo);
        if (deposit == null) {
            log.warn("充值记录不存在：depositNo={}", depositNo);
            return;
        }

        deposit.setConfirmations(confirmations);

        // 如果确认数达标，自动到账
        if (confirmations >= deposit.getNeedConfirmations() && deposit.getStatus() == 0) {
            confirmDeposit(depositNo, confirmations);
        } else {
            depositMapper.updateById(deposit);
        }
    }

    /**
     * 确认充值到账
     */
    private void confirmDeposit(String depositNo, Integer confirmations) {
        WalletDeposit deposit = depositMapper.selectByDepositNo(depositNo);
        if (deposit == null || deposit.getStatus() == 1) {
            return;
        }

        // 更新充值状态
        deposit.setStatus(1); // 已确认
        deposit.setConfirmations(confirmations);
        deposit.setConfirmTime(new Date());
        depositMapper.updateById(deposit);

        // 增加用户余额
        balanceService.increaseBalance(
            deposit.getUserId(), 
            deposit.getCoin(), 
            deposit.getAmount(), 
            deposit.getDepositNo(), 
            "充值到账"
        );

        log.info("充值到账成功：depositNo={}, userId={}, amount={}", 
                 depositNo, deposit.getUserId(), deposit.getAmount());
    }
}

