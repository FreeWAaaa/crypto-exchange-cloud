package com.cex.activity.service;

import com.cex.activity.domain.entity.RedEnvelope;
import com.cex.activity.domain.entity.RedEnvelopeDetail;

import java.math.BigDecimal;
import java.util.List;

/**
 * 红包服务接口
 * 
 * @author cex
 */
public interface RedEnvelopeService {
    
    /**
     * 发红包
     */
    String sendRedEnvelope(Long userId, String coin, BigDecimal totalAmount, 
                          Integer count, Integer envelopeType);
    
    /**
     * 领取红包
     */
    BigDecimal receiveRedEnvelope(Long userId, String envelopeNo);
    
    /**
     * 查询红包详情
     */
    RedEnvelope getByEnvelopeNo(String envelopeNo);
    
    /**
     * 查询红包领取记录
     */
    List<RedEnvelopeDetail> getReceiveRecords(String envelopeNo);
    
    /**
     * 查询用户发送的红包
     */
    List<RedEnvelope> getUserSentEnvelopes(Long userId);
    
    /**
     * 查询用户领取的红包
     */
    List<RedEnvelopeDetail> getUserReceivedEnvelopes(Long userId);
}

