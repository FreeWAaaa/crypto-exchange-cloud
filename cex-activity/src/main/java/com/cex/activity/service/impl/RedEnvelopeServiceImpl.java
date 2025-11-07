package com.cex.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cex.activity.domain.entity.RedEnvelope;
import com.cex.activity.domain.entity.RedEnvelopeDetail;
import com.cex.activity.mapper.RedEnvelopeDetailMapper;
import com.cex.activity.mapper.RedEnvelopeMapper;
import com.cex.activity.service.RedEnvelopeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 红包服务实现
 * 
 * @author cex
 */
@Slf4j
@Service
public class RedEnvelopeServiceImpl implements RedEnvelopeService {
    
    @Autowired
    private RedEnvelopeMapper envelopeMapper;
    
    @Autowired
    private RedEnvelopeDetailMapper detailMapper;
    
    // TODO: 注入 Wallet Feign Client
    // @Autowired
    // private WalletFeignClient walletClient;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String sendRedEnvelope(Long userId, String coin, BigDecimal totalAmount, 
                                  Integer count, Integer envelopeType) {
        log.info("用户发红包：userId={}, coin={}, totalAmount={}, count={}", 
                 userId, coin, totalAmount, count);
        
        // 1. 校验参数
        if (totalAmount.compareTo(BigDecimal.ZERO) <= 0 || count <= 0) {
            throw new RuntimeException("参数错误");
        }
        
        // 2. 冻结用户余额
        // TODO: 调用 Wallet 服务冻结余额
        // walletClient.freezeBalance(userId, coin, totalAmount, null, "发红包");
        
        // 3. 创建红包
        String envelopeNo = "RE" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        
        RedEnvelope envelope = new RedEnvelope();
        envelope.setEnvelopeNo(envelopeNo);
        envelope.setUserId(userId);
        envelope.setEnvelopeType(envelopeType);
        envelope.setTotalAmount(totalAmount);
        envelope.setReceivedAmount(BigDecimal.ZERO);
        envelope.setCount(count);
        envelope.setReceivedCount(0);
        envelope.setCoin(coin);
        envelope.setTitle("来自用户的红包");
        envelope.setStatus(0);  // 领取中
        envelope.setExpiredHours(24);
        envelope.setIsInvite(0);
        envelope.setIsPlatform(0);
        
        if (envelopeType == 0) {  // 随机红包
            envelope.setMaxRandom(totalAmount.divide(BigDecimal.valueOf(count), 8, RoundingMode.DOWN).multiply(BigDecimal.valueOf(2)));
        }
        
        envelopeMapper.insert(envelope);
        
        log.info("红包创建成功：envelopeNo={}", envelopeNo);
        return envelopeNo;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BigDecimal receiveRedEnvelope(Long userId, String envelopeNo) {
        log.info("用户领红包：userId={}, envelopeNo={}", userId, envelopeNo);
        
        // 1. 查询红包
        RedEnvelope envelope = envelopeMapper.selectByEnvelopeNo(envelopeNo);
        if (envelope == null) {
            throw new RuntimeException("红包不存在");
        }
        
        // 2. 校验红包状态
        if (envelope.getStatus() != 0) {
            throw new RuntimeException("红包已过期或已领完");
        }
        
        // 3. 检查是否已领取
        if (detailMapper.countUserReceived(envelope.getId(), userId) > 0) {
            throw new RuntimeException("您已领取过此红包");
        }
        
        // 4. 检查红包是否还有余额
        if (envelope.getReceivedCount() >= envelope.getCount()) {
            envelope.setStatus(1);  // 已领完
            envelopeMapper.updateById(envelope);
            throw new RuntimeException("红包已被抢完");
        }
        
        // 5. 计算领取金额
        BigDecimal amount;
        if (envelope.getEnvelopeType() == 1) {  // 定额红包
            amount = envelope.getTotalAmount().divide(BigDecimal.valueOf(envelope.getCount()), 8, RoundingMode.DOWN);
        } else {  // 随机红包
            if (envelope.getReceivedCount() == envelope.getCount() - 1) {
                // 最后一个红包，领取剩余所有
                amount = envelope.getTotalAmount().subtract(envelope.getReceivedAmount());
            } else {
                // 随机金额
                BigDecimal remaining = envelope.getTotalAmount().subtract(envelope.getReceivedAmount());
                int remainingCount = envelope.getCount() - envelope.getReceivedCount();
                BigDecimal maxAmount = remaining.divide(BigDecimal.valueOf(remainingCount), 8, RoundingMode.DOWN).multiply(BigDecimal.valueOf(2));
                amount = BigDecimal.valueOf(Math.random()).multiply(maxAmount).setScale(8, RoundingMode.DOWN);
                if (amount.compareTo(BigDecimal.ZERO) == 0) {
                    amount = new BigDecimal("0.00000001");
                }
            }
        }
        
        // 6. 创建领取记录
        RedEnvelopeDetail detail = new RedEnvelopeDetail();
        detail.setEnvelopeId(envelope.getId());
        detail.setEnvelopeNo(envelopeNo);
        detail.setUserId(userId);
        detail.setAmount(amount);
        detail.setCoin(envelope.getCoin());
        detailMapper.insert(detail);
        
        // 7. 更新红包信息
        envelope.setReceivedAmount(envelope.getReceivedAmount().add(amount));
        envelope.setReceivedCount(envelope.getReceivedCount() + 1);
        
        if (envelope.getReceivedCount() >= envelope.getCount()) {
            envelope.setStatus(1);  // 已领完
        }
        
        envelopeMapper.updateById(envelope);
        
        // 8. 增加用户余额
        // TODO: 调用 Wallet 服务增加余额
        // walletClient.increaseBalance(userId, envelope.getCoin(), amount, "领取红包");
        
        log.info("红包领取成功：userId={}, amount={} {}", userId, amount, envelope.getCoin());
        
        return amount;
    }
    
    @Override
    public RedEnvelope getByEnvelopeNo(String envelopeNo) {
        return envelopeMapper.selectByEnvelopeNo(envelopeNo);
    }
    
    @Override
    public List<RedEnvelopeDetail> getReceiveRecords(String envelopeNo) {
        return detailMapper.selectByEnvelopeNo(envelopeNo);
    }
    
    @Override
    public List<RedEnvelope> getUserSentEnvelopes(Long userId) {
        LambdaQueryWrapper<RedEnvelope> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RedEnvelope::getUserId, userId);
        wrapper.orderByDesc(RedEnvelope::getCreateTime);
        
        return envelopeMapper.selectList(wrapper);
    }
    
    @Override
    public List<RedEnvelopeDetail> getUserReceivedEnvelopes(Long userId) {
        LambdaQueryWrapper<RedEnvelopeDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RedEnvelopeDetail::getUserId, userId);
        wrapper.orderByDesc(RedEnvelopeDetail::getCreateTime);
        
        return detailMapper.selectList(wrapper);
    }
}

