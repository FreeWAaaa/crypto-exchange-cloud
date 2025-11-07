package com.cex.wallet.service.impl;

import com.cex.wallet.domain.entity.WalletWithdraw;
import com.cex.wallet.mapper.WalletWithdrawMapper;
import com.cex.wallet.service.WalletBalanceService;
import com.cex.wallet.service.WalletWithdrawService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 提现服务实现
 * 
 * @author cex
 */
@Slf4j
@Service
public class WalletWithdrawServiceImpl implements WalletWithdrawService {

    @Autowired
    private WalletWithdrawMapper withdrawMapper;

    @Autowired
    private WalletBalanceService balanceService;

    // 默认提现手续费率
    private static final BigDecimal DEFAULT_FEE_RATE = new BigDecimal("0.001");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String applyWithdraw(Long userId, String coin, String address, BigDecimal amount, String tradePassword) {
        log.info("用户申请提现：userId={}, coin={}, address={}, amount={}", userId, coin, address, amount);

        // 1. TODO: 验证交易密码
        // userService.verifyTradePassword(userId, tradePassword);

        // 2. 计算手续费
        BigDecimal fee = amount.multiply(DEFAULT_FEE_RATE);
        BigDecimal actualAmount = amount.subtract(fee);

        // 3. 冻结余额（包含手续费）
        balanceService.freezeBalance(userId, coin, amount, null, "提现申请");

        // 4. 创建提现记录
        WalletWithdraw withdraw = new WalletWithdraw();
        withdraw.setWithdrawNo("W" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        withdraw.setUserId(userId);
        withdraw.setCoin(coin);
        withdraw.setAddress(address);
        withdraw.setAmount(amount);
        withdraw.setFee(fee);
        withdraw.setActualAmount(actualAmount);
        withdraw.setStatus(0); // 待审核
        withdrawMapper.insert(withdraw);

        log.info("提现申请成功：withdrawNo={}", withdraw.getWithdrawNo());

        // 5. TODO: 发送提现申请通知给管理员
        // sendAuditNotification(withdraw);

        return withdraw.getWithdrawNo();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelWithdraw(Long userId, String withdrawNo) {
        WalletWithdraw withdraw = withdrawMapper.selectByWithdrawNo(withdrawNo);
        if (withdraw == null) {
            throw new RuntimeException("提现记录不存在");
        }

        if (!withdraw.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此提现记录");
        }

        if (withdraw.getStatus() != 0) {
            throw new RuntimeException("只能撤销待审核的提现");
        }

        // 解冻余额
        balanceService.unfreezeBalance(userId, withdraw.getCoin(), withdraw.getAmount(), withdrawNo, "提现撤销");

        // 更新状态
        withdraw.setStatus(5); // 已撤销
        withdrawMapper.updateById(withdraw);

        log.info("提现已撤销：withdrawNo={}", withdrawNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditWithdraw(String withdrawNo, Boolean passed, Long auditorId, String auditor, String remark) {
        WalletWithdraw withdraw = withdrawMapper.selectByWithdrawNo(withdrawNo);
        if (withdraw == null) {
            throw new RuntimeException("提现记录不存在");
        }

        if (withdraw.getStatus() != 0) {
            throw new RuntimeException("该提现申请已被审核");
        }

        withdraw.setAuditorId(auditorId);
        withdraw.setAuditor(auditor);
        withdraw.setAuditTime(new Date());
        withdraw.setAuditRemark(remark);

        if (passed) {
            // 审核通过
            withdraw.setStatus(1);
            log.info("提现审核通过：withdrawNo={}", withdrawNo);
            
            // TODO: 自动发送到区块链（或等待手动发送）
            // sendWithdraw(withdrawNo);
        } else {
            // 审核拒绝，解冻余额
            balanceService.unfreezeBalance(withdraw.getUserId(), withdraw.getCoin(), 
                                          withdraw.getAmount(), withdrawNo, "提现审核拒绝");
            withdraw.setStatus(4); // 已拒绝
            log.info("提现审核拒绝：withdrawNo={}, reason={}", withdrawNo, remark);
        }

        withdrawMapper.updateById(withdraw);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String sendWithdraw(String withdrawNo) {
        WalletWithdraw withdraw = withdrawMapper.selectByWithdrawNo(withdrawNo);
        if (withdraw == null) {
            throw new RuntimeException("提现记录不存在");
        }

        if (withdraw.getStatus() != 1) {
            throw new RuntimeException("只能发送审核通过的提现");
        }

        // TODO: 调用RPC钱包服务发送交易
        // String txHash = rpcWalletService.sendTransaction(withdraw.getCoin(), withdraw.getAddress(), withdraw.getActualAmount());
        String txHash = "0x" + UUID.randomUUID().toString().replace("-", "");

        withdraw.setTxHash(txHash);
        withdraw.setStatus(2); // 已发送
        withdraw.setSendTime(new Date());
        withdrawMapper.updateById(withdraw);

        log.info("提现已发送到区块链：withdrawNo={}, txHash={}", withdrawNo, txHash);

        // TODO: 等待区块链确认后调用 confirmWithdraw
        return txHash;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmWithdraw(String withdrawNo, String txHash) {
        WalletWithdraw withdraw = withdrawMapper.selectByWithdrawNo(withdrawNo);
        if (withdraw == null || withdraw.getStatus() != 2) {
            return;
        }

        // 扣减冻结余额
        balanceService.decreaseFrozen(withdraw.getUserId(), withdraw.getCoin(), 
                                     withdraw.getAmount(), withdrawNo, "提现完成");

        // 更新状态
        withdraw.setStatus(3); // 已完成
        withdraw.setCompleteTime(new Date());
        withdrawMapper.updateById(withdraw);

        log.info("提现完成：withdrawNo={}", withdrawNo);
    }

    @Override
    public List<WalletWithdraw> getWithdrawList(Long userId) {
        return withdrawMapper.selectByUserId(userId);
    }

    @Override
    public List<WalletWithdraw> getPendingWithdraws() {
        return withdrawMapper.selectPendingWithdraws();
    }

    @Override
    public WalletWithdraw getByWithdrawNo(String withdrawNo) {
        return withdrawMapper.selectByWithdrawNo(withdrawNo);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditPass(Long id, String txHash) {
        WalletWithdraw withdraw = withdrawMapper.selectById(id);
        if (withdraw == null) {
            throw new RuntimeException("提现记录不存在");
        }
        
        if (withdraw.getStatus() != 0) {
            throw new RuntimeException("该提现申请已被审核");
        }
        
        // 审核通过
        withdraw.setStatus(1);
        withdraw.setAuditTime(new Date());
        
        // 如果提供了 txHash，直接设置为已发送
        if (txHash != null && !txHash.isEmpty()) {
            withdraw.setTxHash(txHash);
            withdraw.setStatus(2);  // 已发送
            withdraw.setSendTime(new Date());
        }
        
        withdrawMapper.updateById(withdraw);
        log.info("提现审核通过：id={}, txHash={}", id, txHash);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditReject(Long id, String reason) {
        WalletWithdraw withdraw = withdrawMapper.selectById(id);
        if (withdraw == null) {
            throw new RuntimeException("提现记录不存在");
        }
        
        if (withdraw.getStatus() != 0) {
            throw new RuntimeException("该提现申请已被审核");
        }
        
        // 审核拒绝，解冻余额
        balanceService.unfreezeBalance(withdraw.getUserId(), withdraw.getCoin(), 
                                      withdraw.getAmount(), withdraw.getWithdrawNo(), "提现审核拒绝");
        
        withdraw.setStatus(4);  // 已拒绝
        withdraw.setAuditTime(new Date());
        withdraw.setAuditRemark(reason);
        withdrawMapper.updateById(withdraw);
        
        log.info("提现审核拒绝：id={}, reason={}", id, reason);
    }
}


