package com.cex.wallet.service;

import com.cex.wallet.domain.entity.WalletWithdraw;

import java.math.BigDecimal;
import java.util.List;

/**
 * 提现服务接口
 * 
 * @author cex
 */
public interface WalletWithdrawService {

    /**
     * 申请提现
     * 
     * @param userId 用户ID
     * @param coin 币种
     * @param address 提现地址
     * @param amount 提现金额
     * @param tradePassword 交易密码
     * @return 提现单号
     */
    String applyWithdraw(Long userId, String coin, String address, BigDecimal amount, String tradePassword);

    /**
     * 撤销提现
     * 
     * @param userId 用户ID
     * @param withdrawNo 提现单号
     */
    void cancelWithdraw(Long userId, String withdrawNo);

    /**
     * 审核提现（管理后台）
     * 
     * @param withdrawNo 提现单号
     * @param passed 是否通过
     * @param auditorId 审核人ID
     * @param auditor 审核人
     * @param remark 审核备注
     */
    void auditWithdraw(String withdrawNo, Boolean passed, Long auditorId, String auditor, String remark);

    /**
     * 发送提现（调用RPC钱包）
     * 
     * @param withdrawNo 提现单号
     * @return 交易哈希
     */
    String sendWithdraw(String withdrawNo);

    /**
     * 确认提现完成
     * 
     * @param withdrawNo 提现单号
     * @param txHash 交易哈希
     */
    void confirmWithdraw(String withdrawNo, String txHash);

    /**
     * 查询用户提现记录
     * 
     * @param userId 用户ID
     * @return 提现记录列表
     */
    List<WalletWithdraw> getWithdrawList(Long userId);

    /**
     * 查询待审核的提现记录
     * 
     * @return 提现记录列表
     */
    List<WalletWithdraw> getPendingWithdraws();

    /**
     * 根据提现单号查询
     * 
     * @param withdrawNo 提现单号
     * @return 提现记录
     */
    WalletWithdraw getByWithdrawNo(String withdrawNo);
    
    /**
     * 审核通过（简化版）
     * 
     * @param id 提现记录ID
     * @param txHash 交易哈希
     */
    void auditPass(Long id, String txHash);
    
    /**
     * 审核拒绝（简化版）
     * 
     * @param id 提现记录ID
     * @param reason 拒绝原因
     */
    void auditReject(Long id, String reason);
}

