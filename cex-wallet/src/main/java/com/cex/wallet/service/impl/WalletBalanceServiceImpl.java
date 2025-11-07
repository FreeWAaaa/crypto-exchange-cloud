package com.cex.wallet.service.impl;

import com.cex.wallet.domain.entity.WalletBalance;
import com.cex.wallet.domain.entity.WalletTransaction;
import com.cex.wallet.mapper.WalletBalanceMapper;
import com.cex.wallet.mapper.WalletTransactionMapper;
import com.cex.wallet.service.WalletBalanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 钱包余额服务实现
 * 
 * @author cex
 */
@Slf4j
@Service
public class WalletBalanceServiceImpl implements WalletBalanceService {

    @Autowired
    private WalletBalanceMapper walletBalanceMapper;

    @Autowired
    private WalletTransactionMapper transactionMapper;

    private static final int MAX_RETRY = 3;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createUserWallets(Long userId, List<String> coins) {
        log.info("开始为用户创建钱包：userId={}, coins={}", userId, coins);
        
        for (String coin : coins) {
            // 检查是否已存在
            WalletBalance existWallet = walletBalanceMapper.selectByUserIdAndCoin(userId, coin);
            if (existWallet != null) {
                log.warn("钱包已存在，跳过：userId={}, coin={}", userId, coin);
                continue;
            }

            // 创建新钱包
            WalletBalance wallet = new WalletBalance();
            wallet.setUserId(userId);
            wallet.setCoin(coin);
            wallet.setAvailableBalance(BigDecimal.ZERO);
            wallet.setFrozenBalance(BigDecimal.ZERO);
            wallet.setTotalBalance(BigDecimal.ZERO);
            wallet.setToReleased(BigDecimal.ZERO);
            wallet.setIsLock(0);
            wallet.setVersion(0);
            walletBalanceMapper.insert(wallet);
            
            log.info("钱包创建成功：userId={}, coin={}, walletId={}", userId, coin, wallet.getId());
        }
    }

    @Override
    public WalletBalance getWallet(Long userId, String coin) {
        return walletBalanceMapper.selectByUserIdAndCoin(userId, coin);
    }

    @Override
    public List<WalletBalance> getUserWallets(Long userId) {
        return walletBalanceMapper.selectByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void increaseBalance(Long userId, String coin, BigDecimal amount, String relatedId, String remark) {
        // 带重试的乐观锁操作
        for (int i = 0; i < MAX_RETRY; i++) {
            WalletBalance wallet = getWallet(userId, coin);
            if (wallet == null) {
                throw new RuntimeException("钱包不存在：userId=" + userId + ", coin=" + coin);
            }

            BigDecimal balanceBefore = wallet.getAvailableBalance();
            BigDecimal balanceAfter = balanceBefore.add(amount);

            int rows = walletBalanceMapper.increaseBalance(wallet.getId(), amount, wallet.getVersion());
            if (rows > 0) {
                // 成功，记录流水
                recordTransaction(userId, coin, 1, amount, balanceBefore, balanceAfter, relatedId, remark);
                log.info("增加余额成功：userId={}, coin={}, amount={}", userId, coin, amount);
                return;
            }
            
            log.warn("增加余额失败（版本冲突），第{}次重试", i + 1);
        }
        
        throw new RuntimeException("增加余额失败（并发冲突）");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void decreaseBalance(Long userId, String coin, BigDecimal amount, String relatedId, String remark) {
        for (int i = 0; i < MAX_RETRY; i++) {
            WalletBalance wallet = getWallet(userId, coin);
            if (wallet == null) {
                throw new RuntimeException("钱包不存在");
            }

            BigDecimal balanceBefore = wallet.getAvailableBalance();
            if (balanceBefore.compareTo(amount) < 0) {
                throw new RuntimeException("余额不足：可用余额=" + balanceBefore + ", 需要=" + amount);
            }

            BigDecimal balanceAfter = balanceBefore.subtract(amount);

            int rows = walletBalanceMapper.decreaseBalance(wallet.getId(), amount, wallet.getVersion());
            if (rows > 0) {
                recordTransaction(userId, coin, 2, amount.negate(), balanceBefore, balanceAfter, relatedId, remark);
                log.info("减少余额成功：userId={}, coin={}, amount={}", userId, coin, amount);
                return;
            }
            
            log.warn("减少余额失败（版本冲突），第{}次重试", i + 1);
        }
        
        throw new RuntimeException("减少余额失败（并发冲突）");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void freezeBalance(Long userId, String coin, BigDecimal amount, String relatedId, String remark) {
        for (int i = 0; i < MAX_RETRY; i++) {
            WalletBalance wallet = getWallet(userId, coin);
            if (wallet == null) {
                throw new RuntimeException("钱包不存在");
            }

            if (wallet.getAvailableBalance().compareTo(amount) < 0) {
                throw new RuntimeException("可用余额不足：可用=" + wallet.getAvailableBalance() + ", 需要=" + amount);
            }

            int rows = walletBalanceMapper.freezeBalance(wallet.getId(), amount, wallet.getVersion());
            if (rows > 0) {
                log.info("冻结余额成功：userId={}, coin={}, amount={}", userId, coin, amount);
                return;
            }
            
            log.warn("冻结余额失败（版本冲突），第{}次重试", i + 1);
        }
        
        throw new RuntimeException("冻结余额失败（并发冲突）");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfreezeBalance(Long userId, String coin, BigDecimal amount, String relatedId, String remark) {
        for (int i = 0; i < MAX_RETRY; i++) {
            WalletBalance wallet = getWallet(userId, coin);
            if (wallet == null) {
                throw new RuntimeException("钱包不存在");
            }

            if (wallet.getFrozenBalance().compareTo(amount) < 0) {
                throw new RuntimeException("冻结余额不足");
            }

            int rows = walletBalanceMapper.unfreezeBalance(wallet.getId(), amount, wallet.getVersion());
            if (rows > 0) {
                log.info("解冻余额成功：userId={}, coin={}, amount={}", userId, coin, amount);
                return;
            }
            
            log.warn("解冻余额失败（版本冲突），第{}次重试", i + 1);
        }
        
        throw new RuntimeException("解冻余额失败（并发冲突）");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void decreaseFrozen(Long userId, String coin, BigDecimal amount, String relatedId, String remark) {
        for (int i = 0; i < MAX_RETRY; i++) {
            WalletBalance wallet = getWallet(userId, coin);
            if (wallet == null) {
                throw new RuntimeException("钱包不存在");
            }

            if (wallet.getFrozenBalance().compareTo(amount) < 0) {
                throw new RuntimeException("冻结余额不足");
            }

            BigDecimal balanceBefore = wallet.getTotalBalance();
            BigDecimal balanceAfter = balanceBefore.subtract(amount);

            int rows = walletBalanceMapper.decreaseFrozen(wallet.getId(), amount, wallet.getVersion());
            if (rows > 0) {
                recordTransaction(userId, coin, 2, amount.negate(), balanceBefore, balanceAfter, relatedId, remark);
                log.info("扣减冻结余额成功：userId={}, coin={}, amount={}", userId, coin, amount);
                return;
            }
            
            log.warn("扣减冻结余额失败（版本冲突），第{}次重试", i + 1);
        }
        
        throw new RuntimeException("扣减冻结余额失败（并发冲突）");
    }

    @Override
    public void lockWallet(Long userId, String coin) {
        WalletBalance wallet = getWallet(userId, coin);
        if (wallet != null) {
            walletBalanceMapper.lockWallet(wallet.getId());
            log.info("钱包已锁定：userId={}, coin={}", userId, coin);
        }
    }

    @Override
    public void unlockWallet(Long userId, String coin) {
        WalletBalance wallet = getWallet(userId, coin);
        if (wallet != null) {
            walletBalanceMapper.unlockWallet(wallet.getId());
            log.info("钱包已解锁：userId={}, coin={}", userId, coin);
        }
    }

    /**
     * 记录流水
     */
    private void recordTransaction(Long userId, String coin, Integer flowType, 
                                   BigDecimal amount, BigDecimal balanceBefore, 
                                   BigDecimal balanceAfter, String relatedId, String remark) {
        WalletTransaction transaction = new WalletTransaction();
        transaction.setUserId(userId);
        transaction.setCoin(coin);
        transaction.setFlowType(flowType);
        transaction.setAmount(amount);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setRelatedId(relatedId);
        transaction.setRemark(remark);
        transaction.setStatus(1); // 已确认
        transactionMapper.insert(transaction);
    }
}

