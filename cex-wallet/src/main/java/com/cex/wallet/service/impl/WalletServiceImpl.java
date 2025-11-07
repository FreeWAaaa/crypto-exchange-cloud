package com.cex.wallet.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cex.common.core.exception.BusinessException;
import com.cex.wallet.domain.entity.WalletBalance;
import com.cex.wallet.domain.entity.WalletDeposit;
import com.cex.wallet.domain.entity.WalletWithdraw;
import com.cex.wallet.mapper.WalletBalanceMapper;
import com.cex.wallet.mapper.WalletDepositMapper;
import com.cex.wallet.mapper.WalletWithdrawMapper;
import com.cex.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 钱包服务实现类
 * 
 * @author cex
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl extends ServiceImpl<WalletBalanceMapper, WalletBalance> implements WalletService {

    private final WalletDepositMapper walletDepositMapper;
    private final WalletWithdrawMapper walletWithdrawMapper;

    @Override
    public List<WalletBalance> getUserBalances(Long userId) {
        return list(new LambdaQueryWrapper<WalletBalance>()
                .eq(WalletBalance::getUserId, userId)
                .eq(WalletBalance::getDeleted, 0)
                .gt(WalletBalance::getTotalBalance, BigDecimal.ZERO)
                .orderByDesc(WalletBalance::getTotalBalance));
    }

    @Override
    public WalletBalance getUserBalance(Long userId, String coin) {
        WalletBalance balance = getOne(new LambdaQueryWrapper<WalletBalance>()
                .eq(WalletBalance::getUserId, userId)
                .eq(WalletBalance::getCoin, coin)
                .eq(WalletBalance::getDeleted, 0));

        if (balance == null) {
            // 创建新的资产记录
            balance = new WalletBalance();
            balance.setUserId(userId);
            balance.setCoin(coin);
            balance.setAvailableBalance(BigDecimal.ZERO);
            balance.setFrozenBalance(BigDecimal.ZERO);
            balance.setTotalBalance(BigDecimal.ZERO);
            balance.setVersion(0);
            save(balance);
        }

        return balance;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void freezeBalance(Long userId, String coin, BigDecimal amount) {
        WalletBalance balance = getUserBalance(userId, coin);
        
        if (balance.getAvailableBalance().compareTo(amount) < 0) {
            throw new BusinessException("可用余额不足");
        }

        // 使用乐观锁更新
        boolean updated = update(new LambdaUpdateWrapper<WalletBalance>()
                .eq(WalletBalance::getId, balance.getId())
                .eq(WalletBalance::getVersion, balance.getVersion())
                .set(WalletBalance::getAvailableBalance, balance.getAvailableBalance().subtract(amount))
                .set(WalletBalance::getFrozenBalance, balance.getFrozenBalance().add(amount))
                .set(WalletBalance::getVersion, balance.getVersion() + 1));

        if (!updated) {
            throw new BusinessException("资产更新失败，请重试");
        }

        log.info("冻结资产成功: userId={}, coin={}, amount={}", userId, coin, amount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfreezeBalance(Long userId, String coin, BigDecimal amount) {
        WalletBalance balance = getUserBalance(userId, coin);
        
        if (balance.getFrozenBalance().compareTo(amount) < 0) {
            throw new BusinessException("冻结余额不足");
        }

        // 使用乐观锁更新
        boolean updated = update(new LambdaUpdateWrapper<WalletBalance>()
                .eq(WalletBalance::getId, balance.getId())
                .eq(WalletBalance::getVersion, balance.getVersion())
                .set(WalletBalance::getAvailableBalance, balance.getAvailableBalance().add(amount))
                .set(WalletBalance::getFrozenBalance, balance.getFrozenBalance().subtract(amount))
                .set(WalletBalance::getVersion, balance.getVersion() + 1));

        if (!updated) {
            throw new BusinessException("资产更新失败，请重试");
        }

        log.info("解冻资产成功: userId={}, coin={}, amount={}", userId, coin, amount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addAvailableBalance(Long userId, String coin, BigDecimal amount) {
        WalletBalance balance = getUserBalance(userId, coin);

        // 使用乐观锁更新
        boolean updated = update(new LambdaUpdateWrapper<WalletBalance>()
                .eq(WalletBalance::getId, balance.getId())
                .eq(WalletBalance::getVersion, balance.getVersion())
                .set(WalletBalance::getAvailableBalance, balance.getAvailableBalance().add(amount))
                .set(WalletBalance::getTotalBalance, balance.getTotalBalance().add(amount))
                .set(WalletBalance::getVersion, balance.getVersion() + 1));

        if (!updated) {
            throw new BusinessException("资产更新失败，请重试");
        }

        log.info("增加可用余额成功: userId={}, coin={}, amount={}", userId, coin, amount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void subtractAvailableBalance(Long userId, String coin, BigDecimal amount) {
        WalletBalance balance = getUserBalance(userId, coin);
        
        if (balance.getAvailableBalance().compareTo(amount) < 0) {
            throw new BusinessException("可用余额不足");
        }

        // 使用乐观锁更新
        boolean updated = update(new LambdaUpdateWrapper<WalletBalance>()
                .eq(WalletBalance::getId, balance.getId())
                .eq(WalletBalance::getVersion, balance.getVersion())
                .set(WalletBalance::getAvailableBalance, balance.getAvailableBalance().subtract(amount))
                .set(WalletBalance::getTotalBalance, balance.getTotalBalance().subtract(amount))
                .set(WalletBalance::getVersion, balance.getVersion() + 1));

        if (!updated) {
            throw new BusinessException("资产更新失败，请重试");
        }

        log.info("减少可用余额成功: userId={}, coin={}, amount={}", userId, coin, amount);
    }

    @Override
    public String generateDepositAddress(Long userId, String coin) {
        // TODO: 调用区块链服务生成充值地址
        String address = "TEMP_ADDRESS_" + IdUtil.getSnowflakeNextIdStr();
        
        log.info("生成充值地址: userId={}, coin={}, address={}", userId, coin, address);
        return address;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String applyWithdraw(Long userId, String coin, String address, BigDecimal amount, String tradePassword) {
        // TODO: 验证交易密码
        // TODO: 验证提现地址
        // TODO: 验证提现限额

        WalletBalance balance = getUserBalance(userId, coin);
        if (balance.getAvailableBalance().compareTo(amount) < 0) {
            throw new BusinessException("可用余额不足");
        }

        // 创建提现记录
        WalletWithdraw withdraw = new WalletWithdraw();
        withdraw.setWithdrawNo(IdUtil.getSnowflakeNextIdStr());
        withdraw.setUserId(userId);
        withdraw.setCoin(coin);
        withdraw.setAddress(address);
        withdraw.setAmount(amount);
        withdraw.setFee(BigDecimal.ZERO); // TODO: 计算手续费
        withdraw.setActualAmount(amount.subtract(withdraw.getFee()));
        withdraw.setStatus(1); // 待审核
        walletWithdrawMapper.insert(withdraw);

        // 冻结资产
        freezeBalance(userId, coin, amount);

        log.info("申请提现成功: userId={}, coin={}, amount={}, withdrawNo={}", 
                userId, coin, amount, withdraw.getWithdrawNo());

        return withdraw.getWithdrawNo();
    }

    @Override
    public List<WalletDeposit> getDepositRecords(Long userId, String coin, Integer page, Integer size) {
        LambdaQueryWrapper<WalletDeposit> wrapper = new LambdaQueryWrapper<WalletDeposit>()
                .eq(WalletDeposit::getUserId, userId)
                .eq(WalletDeposit::getDeleted, 0)
                .orderByDesc(WalletDeposit::getCreateTime);

        if (coin != null) {
            wrapper.eq(WalletDeposit::getCoin, coin);
        }

        Page<WalletDeposit> pageParam = new Page<>(page, size);
        return walletDepositMapper.selectPage(pageParam, wrapper).getRecords();
    }

    @Override
    public List<WalletWithdraw> getWithdrawRecords(Long userId, String coin, Integer page, Integer size) {
        LambdaQueryWrapper<WalletWithdraw> wrapper = new LambdaQueryWrapper<WalletWithdraw>()
                .eq(WalletWithdraw::getUserId, userId)
                .eq(WalletWithdraw::getDeleted, 0)
                .orderByDesc(WalletWithdraw::getCreateTime);

        if (coin != null) {
            wrapper.eq(WalletWithdraw::getCoin, coin);
        }

        Page<WalletWithdraw> pageParam = new Page<>(page, size);
        return walletWithdrawMapper.selectPage(pageParam, wrapper).getRecords();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processDepositConfirm(String txHash, BigDecimal amount, Integer confirmations) {
        WalletDeposit deposit = walletDepositMapper.selectOne(new LambdaQueryWrapper<WalletDeposit>()
                .eq(WalletDeposit::getTxHash, txHash));

        if (deposit == null) {
            log.warn("未找到充值记录: txHash={}", txHash);
            return;
        }

        if (deposit.getStatus() == 2) {
            log.info("充值已确认: txHash={}", txHash);
            return;
        }

        // 更新确认信息
        deposit.setConfirmAmount(amount);
        deposit.setConfirmations(confirmations);
        
        // 达到确认次数后更新状态
        if (confirmations >= 6) { // TODO: 从配置中获取确认次数
            deposit.setStatus(2); // 已确认
            walletDepositMapper.updateById(deposit);
            
            // 增加用户余额
            addAvailableBalance(deposit.getUserId(), deposit.getCoin(), amount);
            
            log.info("充值确认成功: txHash={}, userId={}, coin={}, amount={}", 
                    txHash, deposit.getUserId(), deposit.getCoin(), amount);
        } else {
            walletDepositMapper.updateById(deposit);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processWithdraw(String withdrawNo) {
        WalletWithdraw withdraw = walletWithdrawMapper.selectOne(new LambdaQueryWrapper<WalletWithdraw>()
                .eq(WalletWithdraw::getWithdrawNo, withdrawNo));

        if (withdraw == null) {
            log.warn("未找到提现记录: withdrawNo={}", withdrawNo);
            return;
        }

        if (withdraw.getStatus() != 2) {
            log.warn("提现状态不正确: withdrawNo={}, status={}", withdrawNo, withdraw.getStatus());
            return;
        }

        // TODO: 调用区块链服务发送提现交易
        String txHash = "TEMP_TX_HASH_" + IdUtil.getSnowflakeNextIdStr();
        
        // 更新提现记录
        withdraw.setTxHash(txHash);
        withdraw.setStatus(3); // 已发送
        withdraw.setSendTime(new java.util.Date());
        walletWithdrawMapper.updateById(withdraw);

        log.info("提现处理成功: withdrawNo={}, txHash={}", withdrawNo, txHash);
    }
}
