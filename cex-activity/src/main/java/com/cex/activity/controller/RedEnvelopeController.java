package com.cex.activity.controller;

import com.cex.activity.domain.entity.RedEnvelope;
import com.cex.activity.domain.entity.RedEnvelopeDetail;
import com.cex.activity.service.RedEnvelopeService;
import com.cex.common.core.domain.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 红包控制器
 * 
 * @author cex
 */
@Slf4j
@RestController
@RequestMapping("/api/activity/redenvelope")
@RequiredArgsConstructor
public class RedEnvelopeController {
    
    private final RedEnvelopeService redEnvelopeService;
    
    /**
     * 发红包
     */
    @PostMapping("/send")
    public Result<String> sendRedEnvelope(@RequestParam Long userId,
                                           @RequestParam String coin,
                                           @RequestParam BigDecimal totalAmount,
                                           @RequestParam Integer count,
                                           @RequestParam Integer envelopeType) {
        String envelopeNo = redEnvelopeService.sendRedEnvelope(userId, coin, totalAmount, count, envelopeType);
        return Result.success(envelopeNo);
    }
    
    /**
     * 领取红包
     */
    @PostMapping("/receive")
    public Result<BigDecimal> receiveRedEnvelope(@RequestParam Long userId,
                                                   @RequestParam String envelopeNo) {
        BigDecimal amount = redEnvelopeService.receiveRedEnvelope(userId, envelopeNo);
        return Result.success(amount);
    }
    
    /**
     * 查询红包详情
     */
    @GetMapping("/detail/{envelopeNo}")
    public Result<RedEnvelope> getDetail(@PathVariable String envelopeNo) {
        RedEnvelope envelope = redEnvelopeService.getByEnvelopeNo(envelopeNo);
        return Result.success(envelope);
    }
    
    /**
     * 查询红包领取记录
     */
    @GetMapping("/records/{envelopeNo}")
    public Result<List<RedEnvelopeDetail>> getReceiveRecords(@PathVariable String envelopeNo) {
        List<RedEnvelopeDetail> records = redEnvelopeService.getReceiveRecords(envelopeNo);
        return Result.success(records);
    }
    
    /**
     * 查询用户发送的红包
     */
    @GetMapping("/sent")
    public Result<List<RedEnvelope>> getUserSentEnvelopes(@RequestParam Long userId) {
        List<RedEnvelope> envelopes = redEnvelopeService.getUserSentEnvelopes(userId);
        return Result.success(envelopes);
    }
    
    /**
     * 查询用户领取的红包
     */
    @GetMapping("/received")
    public Result<List<RedEnvelopeDetail>> getUserReceivedEnvelopes(@RequestParam Long userId) {
        List<RedEnvelopeDetail> details = redEnvelopeService.getUserReceivedEnvelopes(userId);
        return Result.success(details);
    }
}

